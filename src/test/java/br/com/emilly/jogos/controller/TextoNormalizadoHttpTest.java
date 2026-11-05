package br.com.emilly.jogos.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.repository.*;
import br.com.emilly.jogos.support.*;
import com.fasterxml.jackson.databind.*;
import jakarta.persistence.EntityManager;
import java.util.*;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Requisições reais MockMvc; casos negativos sem transação externa, leitura posterior independente.
 */
@org.springframework.security.test.context.support.WithMockUser(username="operador-teste", roles="OPERADOR")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TextoNormalizadoHttpTest {
  @Autowired MockMvc mvc;
  @Autowired PessoaRepository pessoas;
  @Autowired ColaboradorRepository colaboradores;
  @Autowired TransactionTemplate tx;
  @Autowired EntityManager em;
  @Autowired jakarta.validation.Validator validator;
  ObjectMapper json = new ObjectMapper();

  @Test
  void emailUnicodeQueCresceNaNormalizacaoRetorna400SemPersistir() throws Exception {
    String email = "\u0130".repeat(63) + "@" + ("a".repeat(36) + ".").repeat(3) + "com";
    String cpf = DocumentosTeste.cpf();
    assertTrue(
        validator
            .validate(new br.com.emilly.jogos.dto.PessoaRequest("Nome", email, cpf))
            .isEmpty());
    chamar(post("/api/pessoas").content(pessoaBody("Nome", email, cpf)), 400);
    assertFalse(lerPersistido(() -> pessoas.existsByCpf(cpf)));
    var p = pessoa("Nome anterior");
    long id = p.path("id").asLong();
    chamar(
        put("/api/pessoas/{id}", id).content(pessoaBody("Outro", email, p.path("cpf").asText())),
        400);
    assertEquals(
        p.path("email").asText(),
        lerPersistido(() -> pessoas.findById(id).orElseThrow().getEmail()));
    assertEquals(
        "Nome anterior", lerPersistido(() -> pessoas.findById(id).orElseThrow().getNome()));
  }

  JsonNode chamar(MockHttpServletRequestBuilder request, int code) throws Exception {
    var response =
        mvc.perform(request.contentType("application/json"))
            .andExpect(status().is(code))
            .andReturn()
            .getResponse();
    var body = json.readTree(response.getContentAsString());
    if (code == 400) {
      assertEquals(400, body.path("status").asInt());
      assertEquals("Bad Request", body.path("error").asText());
      assertFalse(body.path("message").asText().isBlank());
      assertTrue(body.path("fields").isObject());
      assertFalse(body.path("timestamp").asText().isBlank());
      assertTrue(body.path("path").asText().startsWith("/api/"));
    }
    return body;
  }

  String pessoaBody(String nome, String email, String cpf) throws Exception {
    var b = json.createObjectNode();
    b.put("nome", nome);
    b.put("email", email);
    b.put("cpf", cpf);
    return b.toString();
  }

  JsonNode pessoa(String nome) throws Exception {
    return chamar(
        post("/api/pessoas")
            .content(pessoaBody(nome, UUID.randomUUID() + "@example.com", DocumentosTeste.cpf())),
        201);
  }

  String colaboradorBody(long pessoaId, String matricula) {
    var b = json.createObjectNode();
    b.put("pessoaId", pessoaId);
    b.put("matricula", matricula);
    b.put("funcao", "VENDEDOR");
    b.put("dataAdmissao", "2026-11-05");
    return b.toString();
  }

  <T> T lerPersistido(Supplier<T> query) {
    var read = new TransactionTemplate(tx.getTransactionManager());
    read.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    read.setReadOnly(true);
    return read.execute(
        status -> {
          em.clear();
          return query.get();
        });
  }

  @Test
  void criacaoPessoaRejeitaControlesSemPersistirRegistro() throws Exception {
    String cpf = DocumentosTeste.cpf(), email = UUID.randomUUID() + "@example.com";
    for (String text : TextosTeste.invalidos()) {
      chamar(post("/api/pessoas").content(pessoaBody(text, email, cpf)), 400);
      assertFalse(lerPersistido(() -> pessoas.existsByCpf(cpf)));
    }
  }

  @Test
  void atualizacaoPessoaRejeitaControlesEPreservaDadosPersistidos() throws Exception {
    var p = pessoa("Nome anterior");
    long id = p.path("id").asLong();
    for (String text : TextosTeste.invalidos()) {
      chamar(
          put("/api/pessoas/{id}", id)
              .content(pessoaBody(text, UUID.randomUUID() + "@example.com", DocumentosTeste.cpf())),
          400);
      var current = lerPersistido(() -> pessoas.findById(id).orElseThrow());
      assertEquals(p.path("nome").asText(), current.getNome());
      assertEquals(p.path("email").asText(), current.getEmail());
      assertEquals(p.path("cpf").asText(), current.getCpf());
      assertEquals(
          "Nome anterior", chamar(get("/api/pessoas/{id}", id), 200).path("nome").asText());
    }
  }

  @Test
  void criacaoColaboradorRejeitaMatriculaSemPersistirPapel() throws Exception {
    long id = pessoa("Pessoa sem papel").path("id").asLong();
    for (String text : TextosTeste.invalidos()) {
      chamar(post("/api/colaboradores").content(colaboradorBody(id, text)), 400);
      assertFalse(lerPersistido(() -> colaboradores.existsByPessoaId(id)));
    }
  }

  @Test
  void atualizacaoColaboradorPreservaMatriculaEFuncaoNoBanco() throws Exception {
    long pessoaId = pessoa("Colaborador").path("id").asLong();
    String anterior = "V-" + UUID.randomUUID().toString().substring(0, 20);
    var c = chamar(post("/api/colaboradores").content(colaboradorBody(pessoaId, anterior)), 201);
    long id = c.path("id").asLong();
    for (String text : TextosTeste.invalidos()) {
      var b = json.createObjectNode();
      b.put("matricula", text);
      b.put("funcao", "GERENTE");
      chamar(put("/api/colaboradores/{id}", id).content(b.toString()), 400);
      var current = lerPersistido(() -> colaboradores.findOneById(id).orElseThrow());
      assertEquals(anterior, current.getMatricula());
      assertEquals(FuncaoColaborador.VENDEDOR, current.getFuncao());
      assertEquals(
          anterior, chamar(get("/api/colaboradores/{id}", id), 200).path("matricula").asText());
    }
  }

  @Test
  void textosValidosSaoNormalizadosEPersistidos() throws Exception {
    var p = pessoa("\u0000 Pessoa válida \u001b");
    long id = p.path("id").asLong();
    assertEquals(
        "Pessoa válida", lerPersistido(() -> pessoas.findById(id).orElseThrow().getNome()));
    String matricula = "V-" + UUID.randomUUID().toString().substring(0, 20);
    var c =
        chamar(
            post("/api/colaboradores")
                .content(colaboradorBody(id, "\u0000 " + matricula + " \u001b")),
            201);
    assertEquals(
        matricula,
        lerPersistido(
            () -> colaboradores.findOneById(c.path("id").asLong()).orElseThrow().getMatricula()));
    var updated =
        chamar(
            put("/api/pessoas/{id}", id)
                .content(
                    pessoaBody(
                        " Nome alterado ", p.path("email").asText(), p.path("cpf").asText())),
            200);
    assertEquals("Nome alterado", updated.path("nome").asText());
  }

  @Test
  @Transactional
  void nbspContinuaAceitoNoHttpENoPostgres() throws Exception {
    // Rollback da transação de teste evita consumir uma matrícula única igual a NBSP.
    var p = pessoa("\u00a0");
    long id = p.path("id").asLong();
    var c = chamar(post("/api/colaboradores").content(colaboradorBody(id, "\u00a0")), 201);
    em.flush();
    em.clear();
    assertEquals("\u00a0", pessoas.findById(id).orElseThrow().getNome());
    assertEquals(
        "\u00a0", colaboradores.findOneById(c.path("id").asLong()).orElseThrow().getMatricula());
  }

  @Test
  void outrosCadastrosRejeitamTextosQueNormalizamParaVazio() throws Exception {
    String cnpj = DocumentosTeste.cnpj();
    var genero =
        chamar(
            post("/api/generos")
                .content(
                    json.writeValueAsString(Map.of("nome", "Normalização " + UUID.randomUUID()))),
            201);
    for (String text : TextosTeste.invalidos()) {
      var body = json.createObjectNode();
      body.put("nome", text);
      chamar(post("/api/generos").content(body.toString()), 400);
      chamar(put("/api/generos/{id}", genero.path("id").asLong()).content(body.toString()), 400);
      var supplier = json.createObjectNode();
      supplier.put("razaoSocial", text);
      supplier.put("cnpj", cnpj);
      chamar(post("/api/fornecedores").content(supplier.toString()), 400);
      var jogo = json.createObjectNode();
      jogo.put("codigo", text);
      jogo.put("nome", "Reino");
      jogo.put("preco", 100);
      jogo.put("unidadesDisponiveis", 1);
      jogo.put("estoqueMinimo", 0);
      jogo.put("generoId", genero.path("id").asLong());
      chamar(post("/api/jogos").content(jogo.toString()), 400);
      jogo.put("codigo", "COD-" + UUID.randomUUID());
      jogo.put("nome", text);
      chamar(post("/api/jogos").content(jogo.toString()), 400);
    }
    assertEquals(
        genero.path("nome"),
        chamar(get("/api/generos/{id}", genero.path("id").asLong()), 200).path("nome"));
  }
}
