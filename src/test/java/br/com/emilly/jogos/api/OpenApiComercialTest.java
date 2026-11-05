package br.com.emilly.jogos.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.emilly.jogos.dto.*;
import com.fasterxml.jackson.databind.*;
import io.swagger.v3.core.util.Yaml;
import jakarta.validation.Validator;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@org.springframework.security.test.context.support.WithMockUser(username="operador-teste", roles="OPERADOR")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiComercialTest {
  @Autowired MockMvc mvc;
  @Autowired Validator validator;
  ObjectMapper json = new ObjectMapper().findAndRegisterModules();

  JsonNode doc() throws Exception {
    return json.readTree(
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());
  }

  Set<String> keys(JsonNode n) {
    var s = new HashSet<String>();
    n.fieldNames().forEachRemaining(s::add);
    return s;
  }

  @Test
  void novosDtosDocumentadosComExemplosValidos() throws Exception {
    var schemas = doc().at("/components/schemas");
    for (Class<?> tipo :
        List.of(
            PessoaRequest.class,
            ClienteRequest.class,
            ClienteAtualizacaoRequest.class,
            ColaboradorRequest.class,
            ColaboradorAtualizacaoRequest.class,
            VendaRequest.class,
            ItemVendaRequest.class)) {
      var schema = schemas.path(tipo.getSimpleName());
      assertFalse(schema.path("description").asText().isBlank());
      var example = json.createObjectNode();
      schema
          .path("properties")
          .fields()
          .forEachRemaining(
              e -> {
                assertFalse(e.getValue().path("description").asText().isBlank());
                assertTrue(e.getValue().has("example"));
                example.set(e.getKey(), e.getValue().path("example"));
              });
      var dto = json.treeToValue(example, tipo);
      assertTrue(validator.validate(dto).isEmpty(), tipo.getSimpleName() + validator.validate(dto));
    }
    assertEquals(
        Set.of("nome", "email", "cpf"), keys(schemas.path("PessoaRequest").path("properties")));
    assertEquals(
        Set.of("jogoId", "quantidade"), keys(schemas.path("ItemVendaRequest").path("properties")));
    assertEquals(
        Set.of("matricula", "funcao"),
        keys(schemas.path("ColaboradorAtualizacaoRequest").path("properties")));
  }

  @Test
  void notBlankComercialEquivaleAoContratoNasFronteirasUnicode() throws Exception {
    var schemas = doc().at("/components/schemas");
    for (String text :
        List.of(
            "", " ", "\t\n", "\u001c", "\u1680", "\u2003", "\u2028", "\u3000", "\u00a0", "\u2007",
            "\u202f", " Nome ")) {
      var inputs =
          List.of(
              new PessoaRequest(text, "pessoa@example.com", "52998224725"),
              new ColaboradorRequest(
                  1L,
                  text,
                  br.com.emilly.jogos.domain.FuncaoColaborador.VENDEDOR,
                  java.time.LocalDate.of(2026, 11, 5)),
              new ColaboradorAtualizacaoRequest(
                  text, br.com.emilly.jogos.domain.FuncaoColaborador.GERENTE));
      for (Object input : inputs) {
        String field = input instanceof PessoaRequest ? "nome" : "matricula";
        var property =
            schemas.path(input.getClass().getSimpleName()).path("properties").path(field);
        assertEquals(1, property.path("minLength").asInt());
        assertEquals(
            validator.validate(input).isEmpty(),
            java.util.regex.Pattern.compile(property.path("pattern").asText()).matcher(text).find(),
            input.getClass().getSimpleName() + " unicode=" + text.codePoints().boxed().toList());
      }
    }
    var supplier = schemas.path("FornecedorResponse").path("properties");
    assertTrue(
        br.com.emilly.jogos.domain.DocumentoFiscal.cnpjValido(
            supplier.path("cnpj").path("example").asText()));
    assertTrue(supplier.path("status").path("description").asText().contains("não oferece"));
  }

  @Test
  void contratoRejeitaTextosVaziosDepoisDaNormalizacaoComoODominio() throws Exception {
    var schemas = doc().at("/components/schemas");
    for (String type :
        List.of(
            "PessoaRequest",
            "ColaboradorRequest",
            "ColaboradorAtualizacaoRequest",
            "GeneroJogoRequest",
            "GeneroJogoAtualizacaoRequest",
            "FornecedorRequest",
            "JogoRequest",
            "JogoAtualizacaoRequest")) {
      for (String field : List.of("nome", "matricula", "codigo", "razaoSocial")) {
        var property = schemas.path(type).path("properties").path(field);
        if (property.isMissingNode()) continue;
        var pattern = java.util.regex.Pattern.compile(property.path("pattern").asText());
        for (String text : br.com.emilly.jogos.support.TextosTeste.invalidos())
          if (text != null) assertFalse(pattern.matcher(text).find(), type + "." + field);
        assertTrue(pattern.matcher("\u00a0").find());
        assertTrue(pattern.matcher("\u0000 Nome \u001b").find());
      }
    }
  }

  @Test
  void resumoNaoExpoeItensETotalEDetalhePreservaSnapshot() throws Exception {
    var schemas = doc().at("/components/schemas");
    var resumo = schemas.path("VendaResumoResponse").path("properties");
    assertFalse(resumo.has("itens"));
    assertFalse(resumo.has("total"));
    assertTrue(schemas.path("VendaResponse").path("properties").has("itens"));
    assertTrue(schemas.path("ItemVendaResponse").path("properties").has("valorUnitario"));
    assertEquals(
        Set.of(
            "conteudo",
            "pagina",
            "tamanho",
            "totalElementos",
            "totalPaginas",
            "primeira",
            "ultima"),
        keys(schemas.path("PaginaResponseVendaResumoResponse").path("properties")));
  }

  @Test
  void filtrosEPaginacaoDasQuatroListagensSaoPublicados() throws Exception {
    var d = doc();
    for (String route : List.of("pessoas", "clientes", "colaboradores", "vendas")) {
      var params = d.path("paths").path("/api/" + route).path("get").path("parameters");
      Map<String, JsonNode> names = new HashMap<>();
      params.forEach(
          p -> {
            names.put(p.path("name").asText(), p);
            assertEquals("query", p.path("in").asText());
            assertFalse(p.path("required").asBoolean());
          });
      assertTrue(names.keySet().containsAll(List.of("page", "size", "sort")));
      assertEquals(0, names.get("page").at("/schema/default").asInt());
      assertEquals(20, names.get("size").at("/schema/default").asInt());
      assertEquals(100, names.get("size").at("/schema/maximum").asInt());
      if (route.equals("vendas"))
        assertEquals(
            Set.of(
                "page",
                "size",
                "sort",
                "status",
                "clienteId",
                "vendedorId",
                "dataInicial",
                "dataFinal"),
            names.keySet());
    }
  }

  @Test
  void exemplosDeErrosComerciaisConferemComRespostasReais() throws Exception {
    var d = doc();
    String input = "{\"nome\":\"Pessoa\",\"email\":\"a@example.com\",\"cpf\":\"52998224724\"}";
    var atual =
        json.readTree(
            mvc.perform(post("/api/pessoas").contentType("application/json").content(input))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString());
    var exemplo =
        d.at(
            "/paths/~1api~1pessoas/post/responses/400/content/application~1json/examples/Erro400/value");
    for (String field : List.of("status", "error", "message", "path", "fields"))
      assertEquals(atual.path(field), exemplo.path(field), field);
    var filtro =
        json.readTree(
            mvc.perform(get("/api/vendas").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString());
    var esperado =
        d.at(
            "/paths/~1api~1vendas/get/responses/400/content/application~1json/examples/Erro400/value");
    for (String field : List.of("status", "error", "message", "path", "fields"))
      assertEquals(filtro.path(field), esperado.path(field), field);
  }

  @Test
  void contratoAtualEquivaleAoYamlEHistoricoAula09PermaneceIdentificavel() throws Exception {
    var d = doc();
    var atual = Yaml.mapper().readTree(Files.readAllBytes(Path.of("docs/aula-12-openapi.yaml")));
    for (String field : List.of("info", "paths", "components", "tags", "externalDocs", "openapi"))
      assertEquals(d.path(field), atual.path(field), field);
    var antigo = Yaml.mapper().readTree(Files.readAllBytes(Path.of("docs/aula-09-openapi.yaml")));
    assertFalse(antigo.path("paths").has("/api/pessoas"));
    assertEquals(11, antigo.path("paths").size());
    assertEquals(27, d.path("paths").size());
    assertEquals(
        "0.001",
        d.at("/components/schemas/ItemVendaRequest/properties/quantidade/multipleOf").asText());
  }
}
