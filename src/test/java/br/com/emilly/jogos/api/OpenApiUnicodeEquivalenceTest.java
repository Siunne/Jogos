package br.com.emilly.jogos.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.support.DocumentosTeste;
import com.fasterxml.jackson.databind.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiUnicodeEquivalenceTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;
  ObjectMapper json = new ObjectMapper();

  JsonNode documento() throws Exception {
    return json.readTree(
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());
  }

  boolean aceita(Runnable regra) {
    try {
      regra.run();
      return true;
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }

  Pessoa pessoa(String text) {
    return new Pessoa(text, "pessoa@example.com", "52998224725");
  }

  Jogo jogo(String codigo, String nome) {
    return Jogo.novoCadastro(codigo, nome, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO);
  }

  List<String> corpus() {
    Set<String> cases = new LinkedHashSet<>();
    String[] alphabet = {"\u0000", "\u001b", " ", "\u2003", "\u2028", "\u00a0", "A", "\n"};
    List<String> level = List.of("");
    cases.add("");
    for (int depth = 1; depth <= 4; depth++) {
      List<String> next = new ArrayList<>();
      for (String prefix : level) for (String symbol : alphabet) next.add(prefix + symbol);
      cases.addAll(next);
      level = next;
    }
    for (int c = 0; c <= Character.MAX_VALUE; c++)
      if (Character.isWhitespace((char) c)) {
        String white = String.valueOf((char) c);
        cases.add(white);
        for (int control = 0; control <= 32; control++) cases.add(white + (char) control + white);
      }
    for (int control = 0; control <= 32; control++) cases.add(String.valueOf((char) control));
    cases.addAll(
        List.of(
            "\u2003\u001b\u2003",
            "\u2003\u001b\u2029",
            "A\u001bB",
            "\u0000Nome\u001b",
            "\u2007",
            "\u202f",
            "Texto comum",
            "😀"));
    return new ArrayList<>(cases);
  }

  @Test
  void padroesExportadosEquivalemAsEntidadesEmCombinacoesUnicode() throws Exception {
    var schemas = documento().at("/components/schemas");
    Map<String, Consumer<String>> rules = new LinkedHashMap<>();
    rules.put("PessoaRequest.nome", this::pessoa);
    Consumer<String> colaborador =
        t -> new Colaborador(pessoa("Nome"), t, FuncaoColaborador.VENDEDOR, LocalDate.now());
    rules.put("ColaboradorRequest.matricula", colaborador);
    rules.put(
        "ColaboradorAtualizacaoRequest.matricula",
        t -> {
          var c =
              new Colaborador(pessoa("Nome"), "VEN", FuncaoColaborador.VENDEDOR, LocalDate.now());
          c.alterarDados(t, FuncaoColaborador.GERENTE);
        });
    rules.put("GeneroJogoRequest.nome", GeneroJogo::new);
    rules.put("GeneroJogoAtualizacaoRequest.nome", t -> new GeneroJogo("Nome").alterarNome(t));
    rules.put("FornecedorRequest.razaoSocial", t -> new Fornecedor(t, "11222333000181"));
    rules.put("JogoRequest.codigo", t -> jogo(t, "Nome"));
    rules.put("JogoRequest.nome", t -> jogo("CODIGO", t));
    rules.put("JogoAtualizacaoRequest.nome", t -> jogo("CODIGO", "Nome").alterarNome(t));
    var patterns = json.createObjectNode();
    for (String key : rules.keySet()) {
      var parts = key.split("\\.");
      var property = schemas.path(parts[0]).path("properties").path(parts[1]);
      assertEquals(1, property.path("minLength").asInt());
      patterns.put(key, property.path("pattern").asText());
    }
    var cases = json.createArrayNode();
    for (String text : corpus()) {
      boolean expected = aceita(() -> pessoa(text));
      for (var entry : rules.entrySet()) {
        assertEquals(expected, aceita(() -> entry.getValue().accept(text)), entry.getKey());
        assertEquals(
            expected,
            Pattern.compile(patterns.path(entry.getKey()).asText()).matcher(text).find(),
            entry.getKey() + " " + text.codePoints().boxed().toList());
      }
      cases.addObject().put("texto", text).put("aceitoPeloDominio", expected);
    }
    // E-mail tem validações adicionais de formato e limite após minúsculas; pattern é só texto
    // obrigatório.
    var email = schemas.path("PessoaRequest").path("properties").path("email");
    assertEquals(patterns.path("PessoaRequest.nome").asText(), email.path("pattern").asText());
    assertEquals("email", email.path("format").asText());
    assertEquals(180, email.path("maxLength").asInt());
    var fixture = json.createObjectNode();
    fixture.set("padroesExportados", patterns);
    fixture.set("casosJava", cases);
    Files.createDirectories(Path.of("target"));
    Files.writeString(
        Path.of("target/aula10-unicode-equivalencia.json"), json.writeValueAsString(fixture));
  }

  @Test
  void combinacoesConferemComPostPutELeituraJdbcIndependente() throws Exception {
    assertEquals(
        "jogos_test",
        jdbc.execute(
            (org.springframework.jdbc.core.ConnectionCallback<String>) c -> c.getCatalog()));
    var schema = documento().at("/components/schemas/PessoaRequest/properties/nome");
    var pattern = Pattern.compile(schema.path("pattern").asText());
    String email = UUID.randomUUID() + "@example.com", cpf = DocumentosTeste.cpf();
    var initial =
        json.createObjectNode().put("nome", "Anterior").put("email", email).put("cpf", cpf);
    var created =
        json.readTree(
            mvc.perform(
                    post("/api/pessoas")
                        .contentType("application/json")
                        .content(initial.toString()))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());
    long id = created.path("id").asLong();
    String previous = "Anterior";
    for (String text :
        List.of(
            "",
            " ",
            "\t\n",
            "\u0000",
            "\u001b",
            "\u2003",
            "\u2003\u001b\u2003",
            "\u2003\u001b\u2028",
            "\u2003\u001b\u2029",
            "\u0000\u2003\u001b",
            "A\u001bB",
            "\u0000A\u001b",
            "\u00a0",
            "Texto comum")) {
      boolean expected = aceita(() -> pessoa(text));
      assertEquals(expected, pattern.matcher(text).find());
      String newCpf = DocumentosTeste.cpf();
      var input =
          json.createObjectNode()
              .put("nome", text)
              .put("email", UUID.randomUUID() + "@example.com")
              .put("cpf", newCpf);
      var post =
          mvc.perform(
                  post("/api/pessoas").contentType("application/json").content(input.toString()))
              .andExpect(status().is(expected ? 201 : 400))
              .andReturn()
              .getResponse();
      if (expected) {
        long newId = json.readTree(post.getContentAsString()).path("id").asLong();
        assertEquals(
            text.trim(),
            jdbc.queryForObject("SELECT nome FROM pessoa WHERE id=?", String.class, newId));
      } else {
        assertEquals(
            0,
            jdbc.queryForObject("SELECT count(*) FROM pessoa WHERE cpf=?", Integer.class, newCpf));
        assertEquals(400, json.readTree(post.getContentAsString()).path("status").asInt());
      }
      input.put("email", email).put("cpf", cpf);
      mvc.perform(
              put("/api/pessoas/{id}", id)
                  .contentType("application/json")
                  .content(input.toString()))
          .andExpect(status().is(expected ? 200 : 400));
      if (expected) previous = text.trim();
      assertEquals(
          previous, jdbc.queryForObject("SELECT nome FROM pessoa WHERE id=?", String.class, id));
    }
  }
}
