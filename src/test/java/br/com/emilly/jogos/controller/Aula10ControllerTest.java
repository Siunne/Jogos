package br.com.emilly.jogos.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.emilly.jogos.application.*;
import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.support.*;
import com.fasterxml.jackson.databind.*;
import jakarta.persistence.EntityManager;
import java.math.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.support.TransactionTemplate;

/** Sem transação externa: mapeamento também é exercitado com OSIV desabilitado. */
@org.springframework.security.test.context.support.WithMockUser(username="operador-teste", roles="OPERADOR")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Aula10ControllerTest {
  @Autowired MockMvc mvc;
  @Autowired EntityManager em;
  @Autowired TransactionTemplate tx;
  @Autowired VendaService vendas;
  @Autowired PessoaService pessoas;
  @Autowired ColaboradorService colaboradores;
  final ObjectMapper json = new ObjectMapper();
  ComercialFixture.Dados f;

  @BeforeEach
  void preparar() {
    f = ComercialFixture.criar(em, tx);
  }

  JsonNode chamar(MockHttpServletRequestBuilder req, int statusEsperado) throws Exception {
    var r =
        mvc.perform(req.contentType("application/json").locale(Locale.forLanguageTag("pt-BR")))
            .andExpect(status().is(statusEsperado))
            .andReturn()
            .getResponse();
    return r.getContentAsString().isBlank()
        ? json.nullNode()
        : json.readTree(r.getContentAsString());
  }

  String pessoa(String nome, String email, String cpf) {
    return "{\"nome\":\"" + nome + "\",\"email\":\"" + email + "\",\"cpf\":\"" + cpf + "\"}";
  }

  Long abrir() throws Exception {
    return chamar(
            post("/api/vendas")
                .content("{\"clienteId\":" + f.cliente() + ",\"vendedorId\":" + f.vendedor() + "}"),
            201)
        .path("id")
        .asLong();
  }

  JsonNode adicionar(Long venda, Long jogo, String quantidade) throws Exception {
    return chamar(
        post("/api/vendas/{id}/itens", venda)
            .content("{\"jogoId\":" + jogo + ",\"quantidade\":" + quantidade + "}"),
        200);
  }

  @Test
  void cadastroPessoaNormalizaRetornaLocationEDateDoServidor() throws Exception {
    String cpf = DocumentosTeste.cpf(), email = UUID.randomUUID() + "@EXAMPLE.COM";
    var r =
        mvc.perform(
                post("/api/pessoas")
                    .contentType("application/json")
                    .content(pessoa("Pessoa API", email, cpf)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse();
    var p = json.readTree(r.getContentAsString());
    assertEquals("/api/pessoas/" + p.path("id").asLong(), r.getHeader("Location"));
    assertEquals(email.toLowerCase(Locale.ROOT), p.path("email").asText());
    assertEquals(LocalDate.now().toString(), p.path("dataCadastro").asText());
    assertEquals("ATIVO", p.path("status").asText());
    chamar(get("/api/pessoas/{id}", p.path("id").asLong()), 200);
  }

  @Test
  void rejeitaCpfCnpjInvalidosComApiError() throws Exception {
    var a =
        chamar(post("/api/pessoas").content(pessoa("Pessoa", "a@example.com", "52998224724")), 400);
    assertEquals("CPF inválido", a.at("/fields/cpf").asText());
    var b =
        chamar(
            post("/api/fornecedores")
                .content("{\"razaoSocial\":\"Empresa\",\"cnpj\":\"11222333000182\"}"),
            400);
    assertEquals("CNPJ inválido", b.at("/fields/cnpj").asText());
  }

  @Test
  void rejeitaCpfEEmailDuplicadosSemBloquearProprioId() throws Exception {
    var p = pessoas.buscar(f.pessoa());
    chamar(
        post("/api/pessoas")
            .content(pessoa("Duplicada", UUID.randomUUID() + "@example.com", p.getCpf())),
        409);
    chamar(
        post("/api/pessoas")
            .content(
                pessoa("Duplicada", p.getEmail().toUpperCase(Locale.ROOT), DocumentosTeste.cpf())),
        409);
    chamar(
        put("/api/pessoas/{id}", p.getId())
            .content(pessoa("Nome alterado", p.getEmail(), p.getCpf())),
        200);
  }

  @Test
  void pessoaAlteraStatusSemExcluirPapeis() throws Exception {
    chamar(put("/api/pessoas/{id}/status", f.pessoa()).content("{\"status\":\"INATIVO\"}"), 200);
    chamar(get("/api/clientes/{id}", f.cliente()), 200);
    chamar(get("/api/colaboradores/{id}", f.vendedor()), 200);
    chamar(
        post("/api/vendas")
            .content("{\"clienteId\":" + f.cliente() + ",\"vendedorId\":" + f.vendedor() + "}"),
        400);
    chamar(put("/api/pessoas/{id}/status", f.pessoa()).content("{\"status\":\"ATIVO\"}"), 200);
  }

  @Test
  void criaPapeisSimultaneosComLocationEImpedeDuplicidade() throws Exception {
    var p =
        pessoas.cadastrar("Papéis API", UUID.randomUUID() + "@example.com", DocumentosTeste.cpf());
    String cliente = "{\"pessoaId\":" + p.getId() + ",\"telefone\":null}";
    var c = chamar(post("/api/clientes").content(cliente), 201);
    String colaborador =
        "{\"pessoaId\":"
            + p.getId()
            + ",\"matricula\":\""
            + UUID.randomUUID().toString().substring(0, 25)
            + "\",\"funcao\":\"GERENTE\",\"dataAdmissao\":\"2026-11-05\"}";
    var v = chamar(post("/api/colaboradores").content(colaborador), 201);
    assertEquals(c.path("pessoaId"), v.path("pessoaId"));
    chamar(post("/api/clientes").content(cliente), 409);
    chamar(post("/api/colaboradores").content(colaborador), 409);
    chamar(put("/api/clientes/{id}", c.path("id").asLong()).content("{\"telefone\":\"123\"}"), 200);
    var resultado =
        chamar(
            put("/api/clientes/{id}", c.path("id").asLong()).content("{\"telefone\":null}"), 200);
    assertTrue(resultado.path("telefone").isNull());
    var alterado =
        chamar(
            put("/api/colaboradores/{id}", v.path("id").asLong())
                .content("{\"matricula\":\"NOVO-" + p.getId() + "\",\"funcao\":\"VENDEDOR\"}"),
            200);
    assertEquals(v.path("dataAdmissao"), alterado.path("dataAdmissao"));
  }

  @Test
  void matriculaDuplicadaEFuncaoInvalida() throws Exception {
    var p = pessoas.cadastrar("Outra", UUID.randomUUID() + "@example.com", DocumentosTeste.cpf());
    var atual = colaboradores.buscar(f.vendedor());
    chamar(
        post("/api/colaboradores")
            .content(
                "{\"pessoaId\":"
                    + p.getId()
                    + ",\"matricula\":\""
                    + atual.getMatricula()
                    + "\",\"funcao\":\"VENDEDOR\",\"dataAdmissao\":\"2026-11-05\"}"),
        409);
    chamar(
        put("/api/colaboradores/{id}", f.vendedor())
            .content("{\"matricula\":\"V-TESTE\",\"funcao\":\"INVALIDA\"}"),
        400);
  }

  @Test
  void papelExigePessoaAtiva() throws Exception {
    var p = pessoas.cadastrar("Inativa", UUID.randomUUID() + "@example.com", DocumentosTeste.cpf());
    pessoas.status(p.getId(), Status.INATIVO);
    chamar(post("/api/clientes").content("{\"pessoaId\":" + p.getId() + "}"), 400);
    chamar(
        post("/api/colaboradores")
            .content(
                "{\"pessoaId\":"
                    + p.getId()
                    + ",\"matricula\":\"INATIVO\",\"funcao\":\"VENDEDOR\",\"dataAdmissao\":\"2026-11-05\"}"),
        400);
  }

  @Test
  void pesquisaOsTresCadastrosComFiltrosEPagina() throws Exception {
    var p = pessoas.buscar(f.pessoa());
    var a =
        chamar(
            get("/api/pessoas")
                .param("nome", p.getNome())
                .param("status", "ATIVO")
                .param("page", "0")
                .param("size", "1")
                .param("sort", "nome,asc"),
            200);
    assertEquals(f.pessoa().longValue(), a.at("/conteudo/0/id").asLong());
    assertEquals(1, a.path("totalElementos").asLong());
    var c = chamar(get("/api/clientes").param("pessoaId", f.pessoa().toString()), 200);
    assertEquals(f.cliente().longValue(), c.at("/conteudo/0/id").asLong());
    var v =
        chamar(
            get("/api/colaboradores")
                .param("pessoaId", f.pessoa().toString())
                .param("funcao", "VENDEDOR"),
            200);
    assertEquals(f.vendedor().longValue(), v.at("/conteudo/0/id").asLong());
  }

  @Test
  void vendaAdicionaRemoveReincluiEValidaSnapshot() throws Exception {
    Long id = abrir();
    var v = adicionar(id, f.jogo1(), "2");
    Long item = v.at("/itens/0/id").asLong();
    mvc.perform(delete("/api/vendas/{v}/itens/{i}", id, item))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));
    v = adicionar(id, f.jogo1(), "2");
    assertEquals(200, v.path("total").asInt());
    tx.executeWithoutResult(
        s -> em.find(Jogo.class, f.jogo1()).alterarPreco(new BigDecimal("120.00")));
    v = chamar(get("/api/vendas/{id}", id), 200);
    assertEquals(100, v.at("/itens/0/valorUnitario").asInt());
    assertEquals(200, v.path("total").asInt());
    chamar(post("/api/vendas/{id}/finalizacao", id), 200);
    assertEquals(
        new BigDecimal("8.000"),
        tx.execute(s -> em.find(Jogo.class, f.jogo1()).getUnidadesDisponiveis()));
    var erro = chamar(delete("/api/vendas/{v}/itens/{i}", id, v.at("/itens/0/id").asLong()), 400);
    assertEquals("Somente vendas abertas podem ser alteradas", erro.path("message").asText());
    chamar(post("/api/vendas/{id}/finalizacao", id), 400);
    chamar(post("/api/vendas/{id}/cancelamento", id), 400);
  }

  @Test
  void rejeitaDuplicadoEQuantidadeZeroSemBaixar() throws Exception {
    Long id = abrir();
    adicionar(id, f.jogo1(), "1");
    chamar(
        post("/api/vendas/{id}/itens", id)
            .content("{\"jogoId\":" + f.jogo1() + ",\"quantidade\":1}"),
        400);
    var erro =
        chamar(
            post("/api/vendas/{id}/itens", id)
                .content("{\"jogoId\":" + f.jogo2() + ",\"quantidade\":0}"),
            400);
    assertEquals("Quantidade mínima é 0.001", erro.at("/fields/quantidade").asText());
  }

  @Test
  void vaziaNaoFinalizaECancelamentoNaoBaixa() throws Exception {
    Long id = abrir();
    chamar(post("/api/vendas/{id}/finalizacao", id), 400);
    adicionar(id, f.jogo1(), "1");
    var v = chamar(post("/api/vendas/{id}/cancelamento", id), 200);
    assertEquals("CANCELADA", v.path("status").asText());
    assertEquals(
        new BigDecimal("10.000"),
        tx.execute(s -> em.find(Jogo.class, f.jogo1()).getUnidadesDisponiveis()));
  }

  @Test
  void filtrosVendasCombinadosSemColecaoEPaginacaoReal() throws Exception {
    Long a = abrir(), b = abrir();
    vendas.cancelar(b);
    var v =
        chamar(
            get("/api/vendas")
                .param("clienteId", f.cliente().toString())
                .param("vendedorId", f.vendedor().toString())
                .param("dataInicial", LocalDate.now().toString())
                .param("dataFinal", LocalDate.now().toString())
                .param("page", "0")
                .param("size", "1")
                .param("sort", "id,asc"),
            200);
    assertEquals(a.longValue(), v.at("/conteudo/0/id").asLong());
    assertEquals(2, v.path("totalElementos").asInt());
    assertEquals(2, v.path("totalPaginas").asInt());
    assertTrue(v.path("primeira").asBoolean());
    assertFalse(v.path("ultima").asBoolean());
    assertFalse(v.at("/conteudo/0").has("itens"));
    assertFalse(v.at("/conteudo/0").has("total"));
    var page2 =
        chamar(
            get("/api/vendas")
                .param("clienteId", f.cliente().toString())
                .param("page", "1")
                .param("size", "1")
                .param("sort", "id,asc"),
            200);
    assertEquals(b.longValue(), page2.at("/conteudo/0/id").asLong());
    assertTrue(page2.path("ultima").asBoolean());
    var filtrada =
        chamar(
            get("/api/vendas")
                .param("clienteId", f.cliente().toString())
                .param("status", "CANCELADA"),
            200);
    assertEquals(1, filtrada.path("totalElementos").asInt());
    assertEquals(b.longValue(), filtrada.at("/conteudo/0/id").asLong());
  }

  @Test
  void rejeitaPeriodoInvertidoOrdenacaoETamanhoInvalidos() throws Exception {
    chamar(
        get("/api/vendas").param("dataInicial", "2026-11-06").param("dataFinal", "2026-11-05"),
        400);
    chamar(get("/api/vendas").param("sort", "itens,asc"), 400);
    chamar(get("/api/vendas").param("size", "101"), 400);
    chamar(get("/api/pessoas").param("page", "-1"), 400);
    chamar(get("/api/colaboradores").param("status", "INVALIDO"), 400);
  }

  @Test
  void referenciasInexistentesRetornam404() throws Exception {
    for (String recurso : List.of("pessoas", "clientes", "colaboradores", "vendas"))
      chamar(get("/api/" + recurso + "/" + Long.MAX_VALUE), 404);
    chamar(post("/api/clientes").content("{\"pessoaId\":" + Long.MAX_VALUE + "}"), 404);
    Long id = abrir();
    chamar(
        post("/api/vendas/{id}/itens", id)
            .content("{\"jogoId\":" + Long.MAX_VALUE + ",\"quantidade\":1}"),
        404);
    chamar(delete("/api/vendas/{id}/itens/{item}", id, Long.MAX_VALUE), 404);
  }

  @Test
  void jogoVendidoNaoPodeSerExcluidoEHistoricoSobreviveInativacao() throws Exception {
    Long id = abrir();
    adicionar(id, f.jogo1(), "1");
    vendas.finalizar(id);
    chamar(delete("/api/jogos/{id}", f.jogo1()), 409);
    pessoas.status(f.pessoa(), Status.INATIVO);
    var v = chamar(get("/api/vendas/{id}", id), 200);
    assertEquals("FINALIZADA", v.path("status").asText());
    assertEquals(1, v.path("itens").size());
  }
}
