package br.com.emilly.jogos.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.repository.ContaAcessoRepository;
import br.com.emilly.jogos.support.DocumentosTeste;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@ActiveProfiles("test")
class AutorizacaoTest {
  record Identidade(long id, long pessoa, String login, String senha) {}

  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;
  @Autowired TransactionTemplate tx;
  @Autowired PessoaService pessoas;
  @Autowired ContaAcessoService contas;
  @Autowired ContaAcessoRepository repository;
  @Autowired AdministradorInicialService bootstrap;
  @Autowired AdministracaoContas administracao;
  @Autowired PasswordEncoder encoder;
  @Autowired org.springframework.context.ApplicationContext context;
  private final ObjectMapper json = new ObjectMapper().findAndRegisterModules();
  private final List<Long> criadas = new ArrayList<>();

  @BeforeEach
  void banco() {
    assertEquals("jogos_test", jdbc.queryForObject("select current_database()", String.class));
  }

  @AfterEach
  void limpar() {
    tx.executeWithoutResult(status -> {
      for (Long id : criadas) {
        jdbc.update("delete from conta_acesso where pessoa_id=?", id);
        jdbc.update("delete from pessoa where id=?", id);
      }
    });
  }

  long pessoa() {
    var p = pessoas.cadastrar("Pessoa ficticia Aula 14", UUID.randomUUID() + "@example.invalid",
        DocumentosTeste.cpf());
    criadas.add(p.getId());
    return p.getId();
  }

  Identidade conta(boolean admin) {
    long pessoa = pessoa();
    String login = "a14-" + UUID.randomUUID();
    String senha = "Teste-" + UUID.randomUUID();
    var conta = new ContaAcesso(pessoas.buscar(pessoa), login, encoder.encode(senha));
    if (admin) conta.concederPerfil(PerfilAcesso.ADMINISTRADOR);
    return new Identidade(repository.saveAndFlush(conta).getId(), pessoa, login, senha);
  }

  String login(Identidade conta, int code) throws Exception {
    var response = mvc.perform(post("/api/autenticacao/login").contentType("application/json")
        .content(json.writeValueAsString(Map.of("login", conta.login(), "senha", conta.senha()))))
        .andExpect(status().is(code)).andReturn().getResponse();
    return json.readTree(response.getContentAsString()).path("accessToken").asText();
  }

  MockHttpServletRequestBuilder bearer(MockHttpServletRequestBuilder request, String token) {
    return request.header("Authorization", "Bearer " + token);
  }

  void contaPut(long id, String campo, String corpo, String token, int code) throws Exception {
    mvc.perform(bearer(put("/api/contas-acesso/{id}/{campo}", id, campo), token)
        .contentType("application/json").content(corpo)).andExpect(status().is(code));
  }

  void contaPost(String token, long pessoa, int code) throws Exception {
    mvc.perform(bearer(post("/api/contas-acesso"), token).contentType("application/json")
        .content(json.writeValueAsString(Map.of("pessoaId", pessoa, "login", "novo-" + UUID.randomUUID(),
            "senha", "Teste-" + UUID.randomUUID(), "perfis", List.of("ADMINISTRADOR")))))
        .andExpect(status().is(code))
        .andExpect(result -> {
          if (code == 201) jsonPath("$.perfis[0]").value("OPERADOR").match(result);
        });
  }

  @Test
  void conversorDeduplicaEReconheceSomenteNomesDeDominio() {
    var converter = context.getBean(
        org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter.class);
    for (var invalido : List.of("", "OPERADOR", List.of(), List.of("ROLE_ADMINISTRADOR"),
        List.of(1), List.of("SUPERVISOR"))) {
      var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("fixture")
          .header("alg", "HS256").subject("operador-teste").claim("perfis", invalido).build();
      assertTrue(converter.convert(jwt).getAuthorities().stream()
          .noneMatch(a -> a.getAuthority().startsWith("ROLE_")));
    }
    var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("fixture")
        .header("alg", "HS256").subject("operador-teste")
        .claim("perfis", List.of("OPERADOR", "OPERADOR", "ADMINISTRADOR")).build();
    assertEquals(2, converter.convert(jwt).getAuthorities().stream()
        .filter(a -> a.getAuthority().startsWith("ROLE_")).count());
  }
  @Test
  void politicaHttpECicloDeTokens() throws Exception {
    var admin = conta(true);
    var operador = conta(false);
    var alvo = conta(false);
    String a = login(admin, 200);
    String o = login(operador, 200);
    mvc.perform(get("/api/jogos")).andExpect(status().isUnauthorized());
    mvc.perform(bearer(get("/api/jogos"), o)).andExpect(status().isOk());
    mvc.perform(bearer(get("/api/contas-acesso/me"), o)).andExpect(status().isOk());
    for (String uri : List.of("/api/jogos/1", "/api/generos/1", "/api/vendas/1/itens/1"))
      mvc.perform(bearer(delete(uri), o)).andExpect(status().isForbidden());
    contaPost(o, pessoa(), 403);
    contaPut(alvo.id(), "perfis", "{\"perfis\":[\"OPERADOR\"]}", o, 403);
    contaPut(alvo.id(), "status", "{\"status\":\"BLOQUEADA\"}", o, 403);
    contaPost(a, pessoa(), 201);

    contaPut(operador.id(), "perfis", "{\"perfis\":[\"OPERADOR\",\"ADMINISTRADOR\"]}", a, 200);
    contaPost(o, pessoa(), 403);
    String promovido = login(operador, 200);
    contaPost(promovido, pessoa(), 201);
    contaPut(operador.id(), "perfis", "{\"perfis\":[\"OPERADOR\"]}", a, 200);
    contaPost(promovido, pessoa(), 201);
    contaPost(login(operador, 200), pessoa(), 403);

    contaPut(operador.id(), "status", "{\"status\":\"BLOQUEADA\"}", a, 200);
    login(operador, 401);
    mvc.perform(bearer(get("/api/jogos"), o)).andExpect(status().isOk());
    contaPut(operador.id(), "status", "{\"status\":\"ATIVA\"}", a, 200);
    login(operador, 200);
  }

  @Test
  void autopreservacaoEUltimoAdministradorEfetivo() throws Exception {
    var primeiro = conta(true);
    var segundo = conta(true);
    String a = login(primeiro, 200);
    String b = login(segundo, 200);
    contaPut(primeiro.id(), "status", "{\"status\":\"BLOQUEADA\"}", a, 400);
    contaPut(primeiro.id(), "perfis", "{\"perfis\":[\"OPERADOR\"]}", a, 400);
    contaPut(segundo.id(), "status", "{\"status\":\"BLOQUEADA\"}", a, 200);
    contaPut(primeiro.id(), "status", "{\"status\":\"BLOQUEADA\"}", b, 400);
    contaPut(primeiro.id(), "perfis", "{\"perfis\":[\"OPERADOR\"]}", b, 400);
    mvc.perform(bearer(put("/api/pessoas/{id}/status", primeiro.pessoa()), b)
        .contentType("application/json").content("{\"status\":\"INATIVO\"}"))
        .andExpect(status().isBadRequest());
    assertTrue(tx.<Boolean>execute(status -> repository.findById(primeiro.id()).orElseThrow().podeAutenticar()));
    assertEquals(1L, tx.execute(status -> administracao.ativos()).longValue());
  }

  @ParameterizedTest
  @ValueSource(strings = {"{}", "{\"perfis\":null}", "{\"perfis\":[]}", "{\"perfis\":[null]}",
      "{\"perfis\":[1]}", "{\"perfis\":[\"SUPERVISOR\"]}", "{\"perfis\":\"OPERADOR\"}"})
  void perfisInvalidosSemEfeitos(String json) throws Exception {
    var admin = conta(true);
    var op = conta(false);
    contaPut(op.id(), "perfis", json, login(admin, 200), 400);
    assertEquals(Set.of(PerfilAcesso.OPERADOR),
        tx.execute(status -> Set.copyOf(repository.findById(op.id()).orElseThrow().getPerfis())));
  }

  @Test
  void statusNumericoEContaInexistente() throws Exception {
    var admin = conta(true);
    var op = conta(false);
    String token = login(admin, 200);
    contaPut(op.id(), "status", "{\"status\":1}", token, 400);
    contaPut(Long.MAX_VALUE, "status", "{\"status\":\"BLOQUEADA\"}", token, 404);
    login(op, 200);
  }

  private void comPapel(String login, String papel, Runnable acao) {
    var anterior = SecurityContextHolder.getContext();
    var contexto = SecurityContextHolder.createEmptyContext();
    contexto.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(login, null,
        List.of(new SimpleGrantedAuthority("ROLE_" + papel))));
    SecurityContextHolder.setContext(contexto);
    try {
      acao.run();
    } finally {
      SecurityContextHolder.setContext(anterior);
    }
  }

  @Test
  void proxyProtegeChamadasDiretasAntesDaGravacao() {
    var op = conta(false);
    var admin = conta(true);
    long pessoa = pessoa();
    comPapel(op.login(), "OPERADOR", () -> {
      assertThrows(AccessDeniedException.class, () -> contas.cadastrar(pessoa, "fixture", op.senha()));
      assertThrows(AccessDeniedException.class, () -> contas.alterarPerfis(op.id(), Set.of(PerfilAcesso.ADMINISTRADOR)));
      assertThrows(AccessDeniedException.class, () -> contas.alterarStatus(op.id(), StatusContaAcesso.BLOQUEADA));
    });
    assertFalse(repository.existsByPessoaId(pessoa));
    comPapel(admin.login(), "ADMINISTRADOR", () -> contas.cadastrar(pessoa, "direto-" + UUID.randomUUID(), op.senha()));
    assertTrue(repository.existsByPessoaId(pessoa));
  }

  @Test
  void bootstrapPadraoDesligadoEExecucaoIdempotente() throws Exception {
    assertTrue(context.getBeansOfType(AdministradorInicialRunner.class).isEmpty());
    String login = "bootstrap-" + UUID.randomUUID();
    String senha = "Teste-" + UUID.randomUUID();
    var cfg = new AdministradorInicialProperties(true, "Administrador ficticio",
        login + "@example.invalid", DocumentosTeste.cpf(), login, senha);
    assertFalse(cfg.toString().contains(senha));
    assertTrue(bootstrap.criarSeNecessario(cfg));
    var c = tx.execute(status -> {
      var conta = repository.buscarPorLoginNormalizado(login).orElseThrow();
      criadas.add(conta.getPessoa().getId());
      assertEquals(Set.of(PerfilAcesso.ADMINISTRADOR, PerfilAcesso.OPERADOR), conta.getPerfis());
      assertTrue(conta.getSenhaHash().startsWith("{bcrypt}"));
      assertTrue(encoder.matches(senha, conta.getSenhaHash()));
      return new Identidade(conta.getId(), conta.getPessoa().getId(), login, senha);
    });
    assertFalse(bootstrap.criarSeNecessario(cfg));
    login(c, 200);
  }

  @Test
  void bootstrapRejeitaConfiguracaoEDuplicidadesSemPersistir() {
    String login = "bootstrap-" + UUID.randomUUID();
    String senha = "Teste-" + UUID.randomUUID();
    String email = login + "@example.invalid";
    String cpf = DocumentosTeste.cpf();
    long antes = jdbc.queryForObject("select count(*) from pessoa", Long.class);
    assertThrows(IllegalArgumentException.class, () -> bootstrap.criarSeNecessario(
        new AdministradorInicialProperties(true, null, email, cpf, login, senha)));
    assertEquals(antes, jdbc.queryForObject("select count(*) from pessoa", Long.class).longValue());

    var existente = conta(false);
    var pessoa = pessoas.buscar(existente.pessoa());
    for (var cfg : List.of(
        new AdministradorInicialProperties(true, "Pessoa ficticia", email, cpf, existente.login(), senha),
        new AdministradorInicialProperties(true, "Pessoa ficticia", email, pessoa.getCpf(), login, senha),
        new AdministradorInicialProperties(true, "Pessoa ficticia", pessoa.getEmail(), cpf, login, senha))) {
      assertThrows(br.com.emilly.jogos.exception.RecursoDuplicadoException.class,
          () -> bootstrap.criarSeNecessario(cfg));
    }
    assertFalse(repository.existeLogin(login));
    assertEquals(antes + 1, jdbc.queryForObject("select count(*) from pessoa", Long.class).longValue());
  }

  @Test
  void bootstrapParticipaDoRollbackDaCriacaoConjunta() {
    String login = "rollback-" + UUID.randomUUID();
    String email = login + "@example.invalid";
    String cpf = DocumentosTeste.cpf();
    var cfg = new AdministradorInicialProperties(true, "Administrador ficticio", email, cpf,
        login, "Teste-" + UUID.randomUUID());
    tx.executeWithoutResult(status -> {
      assertTrue(bootstrap.criarSeNecessario(cfg));
      assertTrue(repository.existeLogin(login));
      status.setRollbackOnly();
    });
    assertFalse(repository.existeLogin(login));
    assertEquals(0L, jdbc.queryForObject("select count(*) from pessoa where cpf=?", Long.class, cpf).longValue());
  }
  @Test
  void concorrenciaEntreBloqueioEDesativacaoPreservaAdministrador() throws Exception {
    var a = conta(true);
    var b = conta(true);
    var barreira = new CyclicBarrier(2);
    try (var pool = Executors.newFixedThreadPool(2)) {
      var bloqueio = pool.submit(() -> executar(barreira, a, () -> contas.alterarStatus(b.id(), StatusContaAcesso.BLOQUEADA)));
      var pessoa = pool.submit(() -> executar(barreira, b, () -> pessoas.status(a.pessoa(), Status.INATIVO)));
      assertNotEquals(bloqueio.get(10, TimeUnit.SECONDS), pessoa.get(10, TimeUnit.SECONDS));
    }
    assertEquals(1L, tx.execute(status -> administracao.ativos()).longValue());
  }

  @Test
  void falhaNoInsertDeContaDesfazPessoaDoBootstrap() {
    String login = "falha-" + UUID.randomUUID();
    String email = login + "@example.invalid";
    var cfg = new AdministradorInicialProperties(true, "Pessoa ficticia", email,
        DocumentosTeste.cpf(), login, "Teste-" + UUID.randomUUID());
    jdbc.execute("""
        create function aula14_falha_conta() returns trigger language plpgsql as $aula14$
        begin
          if new.login = tg_argv[0] then
            if exists (select 1 from pessoa where id = new.pessoa_id) then
              raise exception using errcode = '23514', message = 'aula14_conta_rejeitada';
            end if;
            raise exception 'aula14_pessoa_ausente';
          end if;
          return new;
        end; $aula14$
        """);
    try {
      jdbc.execute("create trigger aula14_falha_conta before insert on conta_acesso "
          + "for each row execute function aula14_falha_conta('" + login + "')");
      var falha = assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
          () -> bootstrap.criarSeNecessario(cfg));
      assertTrue(falha.getMostSpecificCause().getMessage().contains("aula14_conta_rejeitada"));
      tx.executeWithoutResult(status -> {
        assertFalse(repository.existeLogin(login));
        assertEquals(0L, jdbc.queryForObject(
            "select count(*) from pessoa where email=?", Long.class, email).longValue());
      });
    } finally {
      jdbc.execute("drop trigger if exists aula14_falha_conta on conta_acesso");
      jdbc.execute("drop function aula14_falha_conta()");
    }
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void alteracoesComunsPreservamDecisaoAdministrativaConfirmada(boolean reativar) throws Exception {
    var admin = conta(true);
    var alvo = conta(false);
    var statusConta = reativar ? StatusContaAcesso.ATIVA : StatusContaAcesso.BLOQUEADA;
    var statusPessoa = reativar ? Status.ATIVO : Status.INATIVO;
    if (reativar) comPapel(admin.login(), "ADMINISTRADOR", () -> {
      contas.alterarStatus(alvo.id(), StatusContaAcesso.BLOQUEADA);
      pessoas.status(alvo.pessoa(), Status.INATIVO);
    });

    String senhaNova = "Teste-" + UUID.randomUUID();
    String emailNovo = UUID.randomUUID() + "@example.invalid";
    String cpf = pessoas.buscar(alvo.pessoa()).getCpf();
    var iniciadas = new CountDownLatch(2);
    try (var pool = Executors.newFixedThreadPool(2)) {
      var tarefas = tx.execute(status -> {
        administracao.serializar();
        var senha = pool.submit(() -> {
          iniciadas.countDown();
          contas.alterarSenha(alvo.login(), alvo.senha(), senhaNova);
        });
        var cadastro = pool.submit(() -> {
          iniciadas.countDown();
          pessoas.alterar(alvo.pessoa(), "Pessoa ficticia atualizada", emailNovo, cpf);
        });
        try {
          assertTrue(iniciadas.await(5, TimeUnit.SECONDS));
          long prazo = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
          while (jdbc.queryForObject("""
              select count(*) from pg_locks
              where locktype = 'advisory' and not granted and classid = 741402
                and objid = 1 and objsubid = 2
              """, Long.class) < 2) {
            assertTrue(System.nanoTime() < prazo, "As duas escritas devem aguardar o advisory lock");
            TimeUnit.MILLISECONDS.sleep(10);
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new AssertionError(e);
        }
        comPapel(admin.login(), "ADMINISTRADOR", () -> {
          contas.alterarStatus(alvo.id(), statusConta);
          pessoas.status(alvo.pessoa(), statusPessoa);
          contas.alterarPerfis(alvo.id(), Set.of(PerfilAcesso.ADMINISTRADOR, PerfilAcesso.OPERADOR));
        });
        return List.of(senha, cadastro);
      });
      for (var tarefa : tarefas) tarefa.get(10, TimeUnit.SECONDS);
    }
    tx.executeWithoutResult(status -> {
      var conta = repository.findById(alvo.id()).orElseThrow();
      assertEquals(statusConta, conta.getStatus());
      assertEquals(statusPessoa, conta.getPessoa().getStatus());
      assertEquals(emailNovo, conta.getPessoa().getEmail());
      assertTrue(conta.getPerfis().contains(PerfilAcesso.ADMINISTRADOR));
      assertTrue(encoder.matches(senhaNova, conta.getSenhaHash()));
      assertTrue(administracao.ativos() >= 1);
    });
  }

  @Test
  void matrizHttpPercorreTodasOperacoesComIdentidadesReais() throws Exception {
    var admin = conta(true);
    var operador = conta(false);
    String tokenAdmin = login(admin, 200);
    String tokenOperador = login(operador, 200);
    var doc = json.readTree(mvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    int operacoes = 0;
    var rotas = doc.path("paths").fields();
    while (rotas.hasNext()) {
      var rota = rotas.next();
      var metodos = rota.getValue().fields();
      while (metodos.hasNext()) {
        var metodo = metodos.next();
        if (!metodo.getValue().has("operationId")) continue;
        operacoes++;
        String path = rota.getKey();
        String verbo = metodo.getKey();
        boolean publica = path.equals("/api/health") || path.equals("/api/autenticacao/login");
        boolean administrativa = verbo.equals("delete")
            || verbo.equals("post") && path.equals("/api/contas-acesso")
            || verbo.equals("put") && Set.of("/api/contas-acesso/{id}/perfis",
                "/api/contas-acesso/{id}/status", "/api/pessoas/{id}/status").contains(path);
        String uri = path.replaceAll("\\{[^}]+}", Long.toString(Long.MAX_VALUE));
        var http = org.springframework.http.HttpMethod.valueOf(verbo.toUpperCase(Locale.ROOT));
        for (int identidade = 0; identidade < 3; identidade++) {
          var req = request(http, uri);
          if (identidade != 0) bearer(req, identidade == 1 ? tokenOperador : tokenAdmin);
          var resultado = mvc.perform(req).andReturn();
          String caso = verbo + " " + path + " identidade=" + identidade;
          if (!publica && identidade == 0) {
            assertEquals(401, resultado.getResponse().getStatus(), caso);
            assertNull(resultado.getHandler(), caso);
          } else if (administrativa && identidade == 1) {
            assertEquals(403, resultado.getResponse().getStatus(), caso);
            assertNull(resultado.getHandler(), caso);
          } else {
            assertInstanceOf(org.springframework.web.method.HandlerMethod.class,
                resultado.getHandler(), caso);
            assertTrue(resultado.getResponse().getStatus() < 500, caso);
            assertNotEquals(401, resultado.getResponse().getStatus(), caso);
            assertNotEquals(403, resultado.getResponse().getStatus(), caso);
          }
        }
      }
    }
    assertEquals(44, operacoes);
  }
  private boolean executar(CyclicBarrier barreira, Identidade ator, Runnable acao) throws Exception {
    barreira.await(5, TimeUnit.SECONDS);
    try {
      comPapel(ator.login(), "ADMINISTRADOR", acao);
      return true;
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().contains("administrador ativo"));
      return false;
    }
  }
}
