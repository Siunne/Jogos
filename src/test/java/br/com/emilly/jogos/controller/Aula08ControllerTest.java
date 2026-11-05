package br.com.emilly.jogos.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

// Sem @Transactional: as respostas são mapeadas após o encerramento das transações dos services.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Aula08ControllerTest {
  @Autowired MockMvc mvc;
  @Autowired GeneroJogoRepository generos;
  @Autowired JogoRepository jogos;
  @Autowired FornecedorRepository fornecedores;
  @Autowired TransactionTemplate tx;
  Long generoId, outroGeneroId, fornecedorId, jogoId;
  String prefixo;
  List<Long> jogoIds;

  @BeforeEach
  void preparar() {
    prefixo = "A08-" + UUID.randomUUID();
    jogoIds = new ArrayList<>();
    tx.executeWithoutResult(
        s -> {
          generoId = generos.save(new GeneroJogo(prefixo + " RPG")).getId();
          outroGeneroId = generos.save(new GeneroJogo(prefixo + " Aventura")).getId();
          String cnpj = br.com.emilly.jogos.support.DocumentosTeste.cnpj();
          fornecedorId = fornecedores.save(new Fornecedor(prefixo, cnpj)).getId();
          jogoId = criarJogo("Alvo", generoId, fornecedorId, "5", "10", Status.ATIVO);
        });
  }

  private Long criarJogo(
      String nome, Long genero, Long fornecedor, String saldo, String minimo, Status status) {
    var jogo =
        new Jogo(
            UUID.randomUUID().toString(),
            prefixo + " " + nome,
            new BigDecimal(saldo),
            BigDecimal.TEN,
            new BigDecimal(minimo),
            LocalDate.of(2020, 1, 2));
    generos.findById(genero).orElseThrow().adicionarJogo(jogo);
    if (fornecedor != null)
      jogo.associarFornecedor(fornecedores.findById(fornecedor).orElseThrow());
    if (status == Status.INATIVO) jogo.inativar();
    jogos.saveAndFlush(jogo);
    jogoIds.add(jogo.getId());
    return jogo.getId();
  }

  @AfterEach
  void limparSomenteFixtures() {
    tx.executeWithoutResult(
        s -> {
          jogos.deleteAllById(jogoIds);
          jogos.flush();
          generos.deleteAllById(List.of(generoId, outroGeneroId));
          generos.flush();
          fornecedores.deleteById(fornecedorId);
        });
  }

  private String dadosJogo(Long genero, String fornecedor) {
    return "{\"nome\":\"Nome atualizado\",\"preco\":25.50,\"estoqueMinimo\":8,\"generoId\":"
        + genero
        + ",\"fornecedorId\":"
        + fornecedor
        + "}";
  }

  @Test
  void cadastroGeraDataNoServidorSemAceitarDataDoCliente() throws Exception {
    for (String extra : List.of("", ",\"dataCadastro\":\"2000-01-01\"")) {
      String codigo = UUID.randomUUID().toString();
      LocalDate antes = LocalDate.now();
      var resultado =
          mvc.perform(
              post("/api/jogos")
                  .contentType("application/json")
                  .content(
                      "{\"codigo\":\""
                          + codigo
                          + "\",\"nome\":\"Novo cadastro\","
                          + "\"unidadesDisponiveis\":10,\"preco\":99.90,\"estoqueMinimo\":2,"
                          + "\"generoId\":"
                          + generoId
                          + ",\"fornecedorId\":null"
                          + extra
                          + "}"));
      var cadastrado = jogos.findByCodigo(codigo).orElseThrow();
      jogoIds.add(cadastrado.getId());
      assertFalse(cadastrado.getDataCadastro().isBefore(antes));
      assertFalse(cadastrado.getDataCadastro().isAfter(LocalDate.now()));
      resultado
          .andExpect(status().isCreated())
          .andExpect(header().string("Location", "/api/jogos/" + cadastrado.getId()))
          .andExpect(jsonPath("$.dataCadastro").value(cadastrado.getDataCadastro().toString()));
    }
  }

  @Test
  void alterarGenero() throws Exception {
    mvc.perform(
            put("/api/generos/{id}", generoId)
                .contentType("application/json")
                .content("{\"nome\":\"" + prefixo + " Atualizado\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome").value(prefixo + " Atualizado"));
    assertEquals(prefixo + " Atualizado", generos.findById(generoId).orElseThrow().getNome());
  }

  @Test
  void rejeitarNomeInvalido() throws Exception {
    for (String nome : List.of(" ", "a".repeat(121))) {
      mvc.perform(
              put("/api/generos/{id}", generoId)
                  .contentType("application/json")
                  .content("{\"nome\":\"" + nome + "\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.fields.nome").exists());
    }
  }

  @Test
  void atualizarJogoETrocarGenero() throws Exception {
    mvc.perform(
            put("/api/jogos/{id}", jogoId)
                .contentType("application/json")
                .content(dadosJogo(outroGeneroId, fornecedorId.toString())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome").value("Nome atualizado"))
        .andExpect(jsonPath("$.preco").value(25.50))
        .andExpect(jsonPath("$.generoId").value(outroGeneroId));
    tx.executeWithoutResult(
        s -> {
          assertTrue(generos.findById(generoId).orElseThrow().getJogos().isEmpty());
          assertEquals(
              jogoId, generos.findById(outroGeneroId).orElseThrow().getJogos().getFirst().getId());
        });
  }

  @Test
  void removerFornecedor() throws Exception {
    mvc.perform(
            put("/api/jogos/{id}", jogoId)
                .contentType("application/json")
                .content(dadosJogo(generoId, "null")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fornecedorId").isEmpty());
    assertNull(jogos.findById(jogoId).orElseThrow().getFornecedor());
  }

  @Test
  void putPreservaCamposControlados() throws Exception {
    var antes = jogos.findById(jogoId).orElseThrow();
    mvc.perform(
            put("/api/jogos/{id}", jogoId)
                .contentType("application/json")
                .content(
                    dadosJogo(generoId, "null")
                        .replace(
                            "}",
                            ",\"codigo\":\"INVASAO\",\"unidadesDisponiveis\":999,\"dataCadastro\":\"2000-01-01\",\"status\":\"INATIVO\"}")))
        .andExpect(status().isOk());
    var depois = jogos.findById(jogoId).orElseThrow();
    assertEquals(antes.getCodigo(), depois.getCodigo());
    assertEquals(antes.getUnidadesDisponiveis(), depois.getUnidadesDisponiveis());
    assertEquals(antes.getDataCadastro(), depois.getDataCadastro());
    assertEquals(LocalDate.of(2020, 1, 2), depois.getDataCadastro());
    assertEquals(Status.ATIVO, depois.getStatus());
  }

  @Test
  void alterarStatusAmbosRecursos() throws Exception {
    for (String recurso : List.of("generos/" + generoId, "jogos/" + jogoId)) {
      for (String estado : List.of("INATIVO", "ATIVO")) {
        mvc.perform(
                put("/api/" + recurso + "/status")
                    .contentType("application/json")
                    .content("{\"status\":\"" + estado + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(estado));
      }
    }
  }

  @Test
  void entradaEstoque() throws Exception {
    mvc.perform(
            post("/api/jogos/{id}/estoque/entradas", jogoId)
                .contentType("application/json")
                .content("{\"quantidade\":2.5}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.unidadesDisponiveis").value(7.5));
    assertEquals(
        0,
        new BigDecimal("7.5")
            .compareTo(jogos.findById(jogoId).orElseThrow().getUnidadesDisponiveis()));
  }

  @Test
  void saidaEstoque() throws Exception {
    mvc.perform(
            post("/api/jogos/{id}/estoque/saidas", jogoId)
                .contentType("application/json")
                .content("{\"quantidade\":2}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.unidadesDisponiveis").value(3));
    assertEquals(
        0,
        new BigDecimal("3")
            .compareTo(jogos.findById(jogoId).orElseThrow().getUnidadesDisponiveis()));
  }

  @Test
  void saidaMaiorNaoAlteraSaldo() throws Exception {
    mvc.perform(
            post("/api/jogos/{id}/estoque/saidas", jogoId)
                .contentType("application/json")
                .content("{\"quantidade\":6}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
    assertEquals(
        0,
        new BigDecimal("5")
            .compareTo(jogos.findById(jogoId).orElseThrow().getUnidadesDisponiveis()));
  }

  @Test
  void quantidadeInvalida() throws Exception {
    for (String quantidade : List.of("0", "-1", "null", "0.0001")) {
      mvc.perform(
              post("/api/jogos/{id}/estoque/entradas", jogoId)
                  .contentType("application/json")
                  .content("{\"quantidade\":" + quantidade + "}"))
          .andExpect(status().isBadRequest());
    }
  }

  @Test
  void excluirJogo() throws Exception {
    mvc.perform(delete("/api/jogos/{id}", jogoId))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));
    mvc.perform(get("/api/jogos/{id}", jogoId)).andExpect(status().isNotFound());
  }

  @Test
  void excluirGeneroLivre() throws Exception {
    mvc.perform(delete("/api/generos/{id}", outroGeneroId))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));
    mvc.perform(get("/api/generos/{id}", outroGeneroId)).andExpect(status().isNotFound());
  }

  @Test
  void excluirGeneroEmUso() throws Exception {
    mvc.perform(delete("/api/generos/{id}", generoId))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409));
    assertTrue(generos.existsById(generoId));
  }

  @Test
  void atualizarInexistente() throws Exception {
    mvc.perform(
            put("/api/generos/{id}", Long.MAX_VALUE)
                .contentType("application/json")
                .content("{\"nome\":\"Valido\"}"))
        .andExpect(status().isNotFound());
    mvc.perform(
            put("/api/jogos/{id}", Long.MAX_VALUE)
                .contentType("application/json")
                .content(dadosJogo(generoId, "null")))
        .andExpect(status().isNotFound());
  }

  @Test
  void nomeDuplicadoNormalizado() throws Exception {
    mvc.perform(
            put("/api/generos/{id}", outroGeneroId)
                .contentType("application/json")
                .content("{\"nome\":\"  " + (prefixo + " RPG").toUpperCase(Locale.ROOT) + "  \"}"))
        .andExpect(status().isConflict());
    assertEquals(prefixo + " Aventura", generos.findById(outroGeneroId).orElseThrow().getNome());
  }

  @Test
  void filtrosCombinados() throws Exception {
    tx.executeWithoutResult(
        s -> {
          criarJogo("Alvo", generoId, fornecedorId, "5", "10", Status.INATIVO);
          criarJogo("Alvo", outroGeneroId, fornecedorId, "5", "10", Status.ATIVO);
          criarJogo("Alvo", generoId, null, "5", "10", Status.ATIVO);
          criarJogo("Alvo", generoId, fornecedorId, "10", "10", Status.ATIVO);
          criarJogo("Outro", generoId, fornecedorId, "5", "10", Status.ATIVO);
        });
    mvc.perform(
            get("/api/jogos")
                .param("nome", prefixo + " alvo")
                .param("status", "ATIVO")
                .param("generoId", generoId.toString())
                .param("fornecedorId", fornecedorId.toString())
                .param("abaixoEstoqueMinimo", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElementos").value(1))
        .andExpect(jsonPath("$.conteudo[0].id").value(jogoId));
  }

  @Test
  void paginacaoEOrdenacao() throws Exception {
    mvc.perform(
            get("/api/generos")
                .param("nome", prefixo)
                .param("page", "0")
                .param("size", "1")
                .param("sort", "nome,asc"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.conteudo.length()").value(1))
        .andExpect(jsonPath("$.conteudo[0].id").value(outroGeneroId))
        .andExpect(jsonPath("$.pagina").value(0))
        .andExpect(jsonPath("$.tamanho").value(1))
        .andExpect(jsonPath("$.totalElementos").value(2))
        .andExpect(jsonPath("$.totalPaginas").value(2))
        .andExpect(jsonPath("$.primeira").value(true))
        .andExpect(jsonPath("$.ultima").value(false));
    mvc.perform(
            get("/api/generos")
                .param("nome", prefixo)
                .param("page", "1")
                .param("size", "1")
                .param("sort", "nome,asc"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.conteudo[0].id").value(generoId))
        .andExpect(jsonPath("$.primeira").value(false))
        .andExpect(jsonPath("$.ultima").value(true));
  }

  @Test
  void paginacaoDeJogosComprovaConteudoOrdenacaoEMetadadosNasDuasPaginas() throws Exception {
    Long beta = tx.execute(s -> criarJogo("Beta", generoId, fornecedorId, "5", "10", Status.ATIVO));
    Long gama = tx.execute(s -> criarJogo("Gama", generoId, fornecedorId, "5", "10", Status.ATIVO));
    Long zeta = tx.execute(s -> criarJogo("Zeta", generoId, fornecedorId, "5", "10", Status.ATIVO));
    var paginas = List.of(List.of(jogoId, beta), List.of(gama, zeta));
    for (int pagina = 0; pagina < 2; pagina++) {
      mvc.perform(
              get("/api/jogos")
                  .param("nome", prefixo)
                  .param("page", Integer.toString(pagina))
                  .param("size", "2")
                  .param("sort", "nome,asc"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", org.hamcrest.Matchers.aMapWithSize(7)))
          .andExpect(jsonPath("$.conteudo.length()").value(2))
          .andExpect(jsonPath("$.conteudo[0].id").value(paginas.get(pagina).get(0)))
          .andExpect(jsonPath("$.conteudo[1].id").value(paginas.get(pagina).get(1)))
          .andExpect(
              jsonPath("$.conteudo[0].nome").value(prefixo + (pagina == 0 ? " Alvo" : " Gama")))
          .andExpect(
              jsonPath("$.conteudo[1].nome").value(prefixo + (pagina == 0 ? " Beta" : " Zeta")))
          .andExpect(jsonPath("$.pagina").value(pagina))
          .andExpect(jsonPath("$.tamanho").value(2))
          .andExpect(jsonPath("$.totalElementos").value(4))
          .andExpect(jsonPath("$.totalPaginas").value(2))
          .andExpect(jsonPath("$.primeira").value(pagina == 0))
          .andExpect(jsonPath("$.ultima").value(pagina == 1));
    }
    mvc.perform(
            get("/api/jogos").param("nome", prefixo).param("size", "2").param("sort", "nome,desc"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pagina").value(0))
        .andExpect(jsonPath("$.conteudo[0].id").value(zeta))
        .andExpect(jsonPath("$.conteudo[1].id").value(gama));
    mvc.perform(get("/api/jogos").param("nome", prefixo))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pagina").value(0))
        .andExpect(jsonPath("$.tamanho").value(20))
        .andExpect(jsonPath("$.conteudo.length()").value(4));
    mvc.perform(get("/api/jogos").param("nome", prefixo).param("size", "100"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tamanho").value(100))
        .andExpect(jsonPath("$.conteudo.length()").value(4));
    for (String tamanho : List.of("0", "101", "-1")) {
      mvc.perform(get("/api/jogos").param("size", tamanho)).andExpect(status().isBadRequest());
    }
  }

  @Test
  void repetirPutPreservaEstadoFinalEDataHistorica() throws Exception {
    var antes = jogos.findById(jogoId).orElseThrow();
    for (int repeticao = 0; repeticao < 2; repeticao++) {
      mvc.perform(
              put("/api/jogos/{id}", jogoId)
                  .contentType("application/json")
                  .content(dadosJogo(outroGeneroId, "null")))
          .andExpect(status().isOk());
      var depois = jogos.findById(jogoId).orElseThrow();
      assertEquals("Nome atualizado", depois.getNome());
      assertEquals(0, new BigDecimal("25.50").compareTo(depois.getPreco()));
      assertEquals(0, new BigDecimal("8").compareTo(depois.getEstoqueMinimo()));
      assertEquals(outroGeneroId, depois.getGenero().getId());
      assertNull(depois.getFornecedor());
      assertEquals(antes.getDataCadastro(), depois.getDataCadastro());
      assertEquals(antes.getCodigo(), depois.getCodigo());
      assertEquals(antes.getUnidadesDisponiveis(), depois.getUnidadesDisponiveis());
      assertEquals(antes.getStatus(), depois.getStatus());
    }
  }

  @Test
  void filtrosGeneroEPadroesPagina() throws Exception {
    Long acima =
        tx.execute(s -> criarJogo("Acima", generoId, fornecedorId, "11", "10", Status.ATIVO));
    Long igual =
        tx.execute(s -> criarJogo("Igual", generoId, fornecedorId, "10", "10", Status.ATIVO));
    mvc.perform(
            put("/api/generos/{id}/status", generoId)
                .contentType("application/json")
                .content("{\"status\":\"INATIVO\"}"))
        .andExpect(status().isOk());
    mvc.perform(
            get("/api/generos")
                .param("nome", prefixo.toUpperCase(Locale.ROOT))
                .param("status", "INATIVO"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElementos").value(1))
        .andExpect(jsonPath("$.pagina").value(0))
        .andExpect(jsonPath("$.tamanho").value(20));
    mvc.perform(get("/api/jogos").param("nome", prefixo).param("abaixoEstoqueMinimo", "false"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElementos").value(3))
        .andExpect(
            jsonPath(
                "$.conteudo[*].id",
                org.hamcrest.Matchers.containsInAnyOrder(
                    jogoId.intValue(), acima.intValue(), igual.intValue())))
        .andExpect(jsonPath("$.conteudo[0].generoNome").exists())
        .andExpect(jsonPath("$.conteudo[0].fornecedorRazaoSocial").exists());
    mvc.perform(get("/api/jogos").param("nome", prefixo))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElementos").value(3));
    mvc.perform(get("/api/jogos").param("nome", prefixo).param("abaixoEstoqueMinimo", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElementos").value(1))
        .andExpect(jsonPath("$.conteudo[0].id").value(jogoId));
  }

  @Test
  void ordenacaoInvalida() throws Exception {
    for (String rota : List.of("/api/generos", "/api/jogos")) {
      for (String sort : List.of("inexistente,asc", "genero.nome,asc", "nome,errado")) {
        mvc.perform(get(rota).param("sort", sort))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
      }
    }
  }

  @Test
  void limitesPagina() throws Exception {
    for (String rota : List.of("/api/generos", "/api/jogos")) {
      for (String size : List.of("0", "101", "abc", "-1"))
        mvc.perform(get(rota).param("size", size)).andExpect(status().isBadRequest());
      mvc.perform(get(rota).param("page", "-1")).andExpect(status().isBadRequest());
      mvc.perform(get(rota).param("size", "100")).andExpect(status().isOk());
    }
  }

  @Test
  void statusInvalido() throws Exception {
    mvc.perform(
            put("/api/jogos/{id}/status", jogoId)
                .contentType("application/json")
                .content("{\"status\":\"OUTRO\"}"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/jogos").param("status", "OUTRO"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }
}
