package br.com.emilly.jogos.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.emilly.jogos.application.*;
import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.dto.*;
import br.com.emilly.jogos.repository.ContaAcessoRepository;
import br.com.emilly.jogos.support.DocumentosTeste;
import com.fasterxml.jackson.databind.*;
import jakarta.persistence.EntityManager;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

/** Filtros ativos, sem WithMockUser, sem transação externa. Credenciais fictícias aleatórias. */
@SpringBootTest
@org.junit.jupiter.api.extension.ExtendWith(
    org.springframework.boot.test.system.OutputCaptureExtension.class)
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@ActiveProfiles("test")
class ContaAcessoSecurityTest {
  @Autowired MockMvc mvc;
  @Autowired PessoaService pessoas;
  @Autowired ContaAcessoService contas;
  @Autowired ContaAcessoRepository repository;
  @Autowired ContaAcessoUserDetailsService details;
  @Autowired PasswordEncoder encoder;
  @Autowired JdbcTemplate jdbc;
  @Autowired TransactionTemplate tx;
  @Autowired EntityManager em;
  @Autowired jakarta.validation.Validator validator;
  ObjectMapper json = new ObjectMapper().findAndRegisterModules();
  Long pessoaId;
  String login, senha;
  List<Long> ids = new ArrayList<>();

  @BeforeEach
  void preparar() {
    assertEquals("jogos_test", jdbc.queryForObject("SELECT current_database()", String.class));
    pessoaId = pessoa();
    login = "op-" + UUID.randomUUID().toString().replace("-", "");
    senha = "Teste-" + UUID.randomUUID();
  }

  Long pessoa() {
    var p =
        pessoas.cadastrar(
            "Pessoa fictícia",
            "a12-" + UUID.randomUUID() + "@example.invalid",
            DocumentosTeste.cpf());
    ids.add(p.getId());
    return p.getId();
  }

  @AfterEach
  void limpar() {
    tx.executeWithoutResult(
        s -> {
          for (Long id : ids) {
            jdbc.update("DELETE FROM conta_acesso WHERE pessoa_id=?", id);
            jdbc.update("DELETE FROM pessoa WHERE id=?", id);
          }
        });
  }

  String payload(Long id, String l, String s) throws Exception {
    return json.writeValueAsString(Map.of("pessoaId", id, "login", l, "senha", s));
  }

  String cadastro() throws Exception {
    var r =
        mvc.perform(
                post("/api/contas-acesso")
                    .contentType("application/json")
                    .content(payload(pessoaId, login, senha)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/contas-acesso/me"))
            .andExpect(jsonPath("$.perfis[0]").value("OPERADOR"))
            .andExpect(jsonPath("$.status").value("ATIVA"))
            .andExpect(jsonPath("$.senha").doesNotExist())
            .andExpect(jsonPath("$.senhaHash").doesNotExist())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertFalse(r.contains(senha));
    return r;
  }

  String hash() {
    return jdbc.queryForObject(
        "SELECT senha_hash FROM conta_acesso WHERE pessoa_id=?", String.class, pessoaId);
  }

  void autentica(String pass, int status) throws Exception {
    mvc.perform(get("/api/contas-acesso/me").with(httpBasic(login, pass)))
        .andExpect(status().is(status));
  }

  @Test
  void cadastroRejeitaNulEmQualquerPosicaoSemPersistirNemVazar(
      org.springframework.boot.test.system.CapturedOutput output) throws Exception {
    for (String invalida :
        List.of(
            "\0".repeat(12),
            "\0" + senha,
            senha + "\0",
            senha.substring(0, 10) + "\0" + senha.substring(10))) {
      var erro =
          mvc.perform(
                  post("/api/contas-acesso")
                      .contentType("application/json")
                      .content(payload(pessoaId, login, invalida)))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.fields.senha").exists())
              .andReturn()
              .getResponse()
              .getContentAsString();
      assertFalse(erro.contains(senha));
      assertFalse(erro.contains("{bcrypt}"));
      assertEquals(0, contasPersistidasIndependentemente());
      assertThrows(
          IllegalArgumentException.class, () -> contas.cadastrar(pessoaId, login, invalida));
      assertEquals(0, contasPersistidasIndependentemente());
    }
    assertFalse(output.getAll().contains(senha));
  }

  String hashPersistidoIndependentemente() {
    var leitura = new TransactionTemplate(tx.getTransactionManager());
    leitura.setPropagationBehavior(
        org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    return leitura.execute(
        s ->
            jdbc.queryForObject(
                "SELECT senha_hash FROM conta_acesso WHERE pessoa_id=?", String.class, pessoaId));
  }

  int contasPersistidasIndependentemente() {
    var leitura = new TransactionTemplate(tx.getTransactionManager());
    leitura.setPropagationBehavior(
        org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    return leitura.execute(
        s ->
            jdbc.queryForObject(
                "SELECT count(*) FROM conta_acesso WHERE pessoa_id=?", Integer.class, pessoaId));
  }

  @Test
  void novaSenhaOuSenhaAtualComNulNaoAlteramHashPersistido(
      org.springframework.boot.test.system.CapturedOutput output) throws Exception {
    cadastro();
    String anterior = hashPersistidoIndependentemente();
    for (String invalida : List.of("\0".repeat(12), "\0" + senha, senha + "\0")) {
      for (boolean atualInvalida : List.of(false, true)) {
        var body =
            Map.of(
                "senhaAtual",
                atualInvalida ? invalida : senha,
                "novaSenha",
                atualInvalida ? "Nova-" + UUID.randomUUID() : invalida);
        var erro =
            mvc.perform(
                    put("/api/contas-acesso/me/senha")
                        .with(httpBasic(login, senha))
                        .contentType("application/json")
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(
                    jsonPath("$.fields." + (atualInvalida ? "senhaAtual" : "novaSenha")).exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertFalse(erro.contains(senha));
        assertFalse(erro.contains(anterior));
        assertTrue(anterior.equals(hashPersistidoIndependentemente()));
      }
      assertThrows(
          IllegalArgumentException.class, () -> contas.alterarSenha(login, senha, invalida));
      assertThrows(
          IllegalArgumentException.class,
          () -> contas.alterarSenha(login, invalida, "Nova-" + UUID.randomUUID()));
    }
    assertTrue(anterior.equals(hashPersistidoIndependentemente()));
    autentica(senha, 200);
    assertFalse(output.getAll().contains(senha));
    assertFalse(output.getAll().contains(anterior));
  }

  @Test
  void basicBloqueiaEquivalentesNulDeContaLegadaSemAlterarHash(
      org.springframework.boot.test.system.CapturedOutput output) throws Exception {
    cadastro();
    // Encoder bruto é usado somente para simular a fixture insegura da auditoria.
    var bruto =
        org.springframework.security.crypto.factory.PasswordEncoderFactories
            .createDelegatingPasswordEncoder();
    String legado = bruto.encode("\0".repeat(12));
    jdbc.update("UPDATE conta_acesso SET senha_hash=? WHERE pessoa_id=?", legado, pessoaId);
    for (int n : List.of(12, 13, 20)) {
      var erro =
          mvc.perform(get("/api/contas-acesso/me").with(httpBasic(login, "\0".repeat(n))))
              .andExpect(status().isUnauthorized())
              .andReturn()
              .getResponse()
              .getContentAsString();
      assertFalse(erro.contains(legado));
      mvc.perform(
              put("/api/contas-acesso/me/senha")
                  .with(httpBasic(login, "\0".repeat(n)))
                  .contentType("application/json")
                  .content(
                      json.writeValueAsString(
                          Map.of("senhaAtual", "\0".repeat(13), "novaSenha", senha))))
          .andExpect(status().isUnauthorized());
      assertTrue(legado.equals(hashPersistidoIndependentemente()));
    }
    assertFalse(output.getAll().contains(legado));
  }

  @Test
  void unicodeSemNulContinuaValidoEDiferentesNaoAutenticam() throws Exception {
    String[][] pares = {
      {"é".repeat(12), "e\u0301".repeat(12)},
      {"😀".repeat(12), "😀".repeat(13)},
      {"A".repeat(12), "A".repeat(13)},
      {"\u00a0".repeat(12), "\u00a0".repeat(13)},
      {"Frase-\u001b-" + UUID.randomUUID(), "Frase-\u001b-" + UUID.randomUUID()}
    };
    for (var par : pares) {
      if (contasPersistidasIndependentemente() == 0) contas.cadastrar(pessoaId, login, par[0]);
      else
        tx.executeWithoutResult(
            s ->
                repository
                    .buscarPorLoginNormalizado(login)
                    .orElseThrow()
                    .alterarSenha(encoder.encode(par[0])));
      var h = hashPersistidoIndependentemente();
      assertTrue(encoder.matches(par[0], h));
      assertFalse(encoder.matches(par[1], h));
      autentica(par[0], 200);
      autentica(par[1], 401);
    }
  }

  @Test
  void healthSwaggerEOpenApiPublicos() throws Exception {
    for (String p :
        List.of(
            "/api/health",
            "/v3/api-docs",
            "/v3/api-docs.yaml",
            "/swagger-ui/index.html",
            "/v3/api-docs/swagger-config")) mvc.perform(get(p)).andExpect(status().isOk());
    mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
  }

  @Test
  void bootstrapPessoaEContaSemAutenticacao() throws Exception {
    var r =
        mvc.perform(
                post("/api/pessoas")
                    .contentType("application/json")
                    .content(
                        json.writeValueAsString(
                            Map.of(
                                "nome",
                                "Pessoa bootstrap",
                                "cpf",
                                DocumentosTeste.cpf(),
                                "email",
                                UUID.randomUUID() + "@example.invalid"))))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse();
    Long id = json.readTree(r.getContentAsString()).path("id").asLong();
    ids.add(id);
    mvc.perform(
            post("/api/contas-acesso")
                .contentType("application/json")
                .content(payload(id, login, senha)))
        .andExpect(status().isCreated());
    autentica(senha, 200);
  }

  @Test
  void todasAsRotasProtegidasExigemIdentidade() throws Exception {
    var d =
        json.readTree(
            mvc.perform(get("/v3/api-docs")).andReturn().getResponse().getContentAsString());
    int[] count = {0};
    d.path("paths")
        .fields()
        .forEachRemaining(
            path ->
                path.getValue()
                    .fields()
                    .forEachRemaining(
                        op -> {
                          String metodo = op.getKey(), p = path.getKey();
                          if (!Set.of("get", "post", "put", "delete").contains(metodo)) return;
                          if (metodo.equals("get") && p.equals("/api/health")
                              || metodo.equals("post")
                                  && Set.of("/api/pessoas", "/api/contas-acesso").contains(p))
                            return;
                          try {
                            mvc.perform(
                                    request(
                                        org.springframework.http.HttpMethod.valueOf(
                                            metodo.toUpperCase()),
                                        p.replaceAll("\\{[^}]+}", "1")))
                                .andExpect(status().isUnauthorized());
                            count[0]++;
                          } catch (Exception e) {
                            throw new AssertionError(
                                "Rota protegida falhou: " + metodo + " " + p, e);
                          }
                        }));
    assertEquals(38, count[0]);
  }

  @Test
  void basicCorretoConsultaContaSemExporHash() throws Exception {
    cadastro();
    mvc.perform(get("/api/contas-acesso/me").with(httpBasic(login, senha)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.login").value(login))
        .andExpect(jsonPath("$.senhaHash").doesNotExist())
        .andExpect(jsonPath("$.senha").doesNotExist());
  }

  @Test
  void senhaErradaELoginInexistenteSaoGenericos() throws Exception {
    cadastro();
    var a =
        mvc.perform(get("/api/contas-acesso/me").with(httpBasic(login, "incorreta-ficticia")))
            .andExpect(status().isUnauthorized())
            .andReturn()
            .getResponse();
    var b =
        mvc.perform(get("/api/contas-acesso/me").with(httpBasic("nao-existe-" + login, senha)))
            .andExpect(status().isUnauthorized())
            .andReturn()
            .getResponse();
    assertEquals(a.getContentAsString(), b.getContentAsString());
    assertTrue(a.getHeader("WWW-Authenticate").startsWith("Basic"));
    assertFalse(a.getContentAsString().contains(login));
  }

  @Test
  void contaBloqueadaNaoAutentica() throws Exception {
    cadastro();
    tx.executeWithoutResult(
        s -> repository.buscarPorLoginNormalizado(login).orElseThrow().bloquear());
    autentica(senha, 401);
  }

  @Test
  void pessoaInativaNaoAutentica() throws Exception {
    cadastro();
    pessoas.status(pessoaId, Status.INATIVO);
    autentica(senha, 401);
  }

  @Test
  void pessoaInativaNaoRecebeConta() throws Exception {
    pessoas.status(pessoaId, Status.INATIVO);
    mvc.perform(
            post("/api/contas-acesso")
                .contentType("application/json")
                .content(payload(pessoaId, login, senha)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void clienteNaoEscolheAdministradorOuStatus() throws Exception {
    var p = json.readTree(payload(pessoaId, login, senha));
    ((com.fasterxml.jackson.databind.node.ObjectNode) p).putArray("perfis").add("ADMINISTRADOR");
    ((com.fasterxml.jackson.databind.node.ObjectNode) p).put("status", "BLOQUEADA");
    mvc.perform(
            post("/api/contas-acesso")
                .contentType("application/json")
                .content(json.writeValueAsString(p)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.perfis.length()").value(1))
        .andExpect(jsonPath("$.perfis[0]").value("OPERADOR"))
        .andExpect(jsonPath("$.status").value("ATIVA"));
  }

  @Test
  void somenteHashBcryptPersisteEVerifica() throws Exception {
    cadastro();
    String h = hash();
    assertTrue(h.startsWith("{bcrypt}"));
    assertFalse(h.equals(senha));
    assertTrue(encoder.matches(senha, h));
    assertFalse(encoder.matches("senha-ficticia-incorreta", h));
  }

  @Test
  void saltProduzHashesDiferentesParaMesmaSenha() throws Exception {
    cadastro();
    String h = hash();
    var outra = contas.cadastrar(pessoa(), login + "x", senha);
    String h2 =
        jdbc.queryForObject(
            "SELECT senha_hash FROM conta_acesso WHERE id=?", String.class, outra.id());
    assertFalse(h.equals(h2));
    assertTrue(encoder.matches(senha, h2));
  }

  @Test
  void alterarSenhaInvalidaAnteriorSemSessao() throws Exception {
    cadastro();
    String nova = "Outra-" + UUID.randomUUID();
    mvc.perform(
            put("/api/contas-acesso/me/senha")
                .with(httpBasic(login, senha))
                .contentType("application/json")
                .content(json.writeValueAsString(Map.of("senhaAtual", senha, "novaSenha", nova))))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));
    autentica(senha, 401);
    autentica(nova, 200);
    mvc.perform(get("/api/contas-acesso/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void senhaAtualErradaEReutilizacaoNaoAlteramHash() throws Exception {
    cadastro();
    String h = hash();
    for (String atual : List.of("senha-incorreta-ficticia", senha))
      mvc.perform(
              put("/api/contas-acesso/me/senha")
                  .with(httpBasic(login, senha))
                  .contentType("application/json")
                  .content(
                      json.writeValueAsString(Map.of("senhaAtual", atual, "novaSenha", senha))))
          .andExpect(status().isBadRequest());
    assertTrue(h.equals(hash()));
    autentica(senha, 200);
  }

  @Test
  void loginDuplicadoIgnoraCaixaEEspacosU0020() throws Exception {
    cadastro();
    mvc.perform(
            post("/api/contas-acesso")
                .contentType("application/json")
                .content(payload(pessoa(), "  " + login.toUpperCase(Locale.ROOT) + "  ", senha)))
        .andExpect(status().isConflict());
  }

  @Test
  void pessoaSoRecebeUmaConta() throws Exception {
    cadastro();
    mvc.perform(
            post("/api/contas-acesso")
                .contentType("application/json")
                .content(payload(pessoaId, login + "x", senha)))
        .andExpect(status().isConflict());
  }

  @Test
  void referenciaInexistenteRetorna404() throws Exception {
    mvc.perform(
            post("/api/contas-acesso")
                .contentType("application/json")
                .content(payload(Long.MAX_VALUE, login, senha)))
        .andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"ab", "com espaço", "a\tb", "a\nb", "ábc", "\u00a0abc\u00a0", "\tabc", "abc\n"})
  void loginInvalidoRejeitado(String l) throws Exception {
    mvc.perform(
            post("/api/contas-acesso")
                .contentType("application/json")
                .content(payload(pessoaId, l, senha)))
        .andExpect(status().isBadRequest());
    assertEquals(
        0,
        jdbc.queryForObject(
            "SELECT count(*) FROM conta_acesso WHERE pessoa_id=?", Integer.class, pessoaId));
  }

  @Test
  void loginNormalizadoIgualAoPostgresEAutentica() throws Exception {
    login = "MiX-" + UUID.randomUUID().toString().replace("-", "");
    cadastro();
    String db = jdbc.queryForObject("SELECT lower(btrim(?))", String.class, "  " + login + "  ");
    assertEquals(ContaAcesso.normalizarLogin("  " + login + "  "), db);
    autentica(senha, 200);
  }

  @Test
  void limiteUnicode72BytesSemTruncamento() throws Exception {
    String aceita = "á".repeat(36), rejeita = "á".repeat(37);
    mvc.perform(
            post("/api/contas-acesso")
                .contentType("application/json")
                .content(payload(pessoaId, login, rejeita)))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/contas-acesso")
                .contentType("application/json")
                .content(payload(pessoaId, login, aceita)))
        .andExpect(status().isCreated());
    autentica(aceita, 200);
    autentica(rejeita, 401);
  }

  @Test
  void unicodeAstralContaCaracteresENaoUnidadesUtf16() throws Exception {
    String s = "😀".repeat(12);
    mvc.perform(
            post("/api/contas-acesso")
                .contentType("application/json")
                .content(payload(pessoaId, login, s)))
        .andExpect(status().isCreated());
    autentica(s, 200);
    assertFalse(PoliticaSenha.valida("😀".repeat(6)));
  }

  @Test
  void senhaCurtaNaoPersisteNemVazaEmErro() throws Exception {
    String curta = "curta";
    var r =
        mvc.perform(
                post("/api/contas-acesso")
                    .contentType("application/json")
                    .content(payload(pessoaId, login, curta)))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertFalse(r.contains(curta));
    assertEquals(
        0,
        jdbc.queryForObject(
            "SELECT count(*) FROM conta_acesso WHERE pessoa_id=?", Integer.class, pessoaId));
  }

  @Test
  void toStringsESerializacaoNaoExpoemSegredos() throws Exception {
    cadastro();
    var c = tx.execute(s -> repository.buscarPorLoginNormalizado(login).orElseThrow());
    assertFalse(c.toString().contains(hash()));
    assertFalse(new ContaAcessoRequest(pessoaId, login, senha).toString().contains(senha));
    assertFalse(new AlteracaoSenhaRequest(senha, senha).toString().contains(senha));
    assertFalse(json.writeValueAsString(c).contains("senhaHash"));
  }

  @Test
  void indicePostgresContratoEDominioConcordamNoLogin() throws Exception {
    for (String valor :
        List.of(
            "  I11  ",
            "MiXeD._-09",
            "a".repeat(80),
            " " + "A".repeat(80) + " ",
            "\u00a0abc",
            "abc\t",
            "\u001babc",
            "a b",
            "ábc",
            "ab",
            "a".repeat(81))) {
      boolean aceita;
      try {
        var normalizado = ContaAcesso.normalizarLogin(valor);
        assertEquals(
            normalizado, jdbc.queryForObject("SELECT lower(btrim(?))", String.class, valor));
        aceita = true;
      } catch (IllegalArgumentException e) {
        aceita = false;
      }
      assertEquals(
          aceita, validator.validate(new ContaAcessoRequest(pessoaId, valor, senha)).isEmpty());
      var d =
          json.readTree(
              mvc.perform(get("/v3/api-docs")).andReturn().getResponse().getContentAsString());
      var pattern =
          d.at("/components/schemas/ContaAcessoRequest/properties/login/pattern").asText();
      assertEquals(aceita, java.util.regex.Pattern.compile(pattern).matcher(valor).find());
    }
  }

  @Test
  void violacaoRealDeContaNaoExpoeSenhaHashOuLoginNosLogs(
      org.springframework.boot.test.system.CapturedOutput output) throws Exception {
    cadastro();
    Long outraPessoa = pessoa();
    String novoHash = encoder.encode(senha);
    assertThrows(
        org.springframework.dao.DataIntegrityViolationException.class,
        () ->
            tx.executeWithoutResult(
                s ->
                    repository.saveAndFlush(
                        new ContaAcesso(em.find(Pessoa.class, outraPessoa), login, novoHash))));
    assertTrue(output.getAll().contains("23505"));
    assertTrue(output.getAll().contains("uk_conta_acesso_login_lower"));
    assertFalse(output.getAll().contains(novoHash));
    assertFalse(output.getAll().contains(senha));
    assertFalse(output.getAll().contains(hash()));
    assertFalse(output.getAll().contains(login));
  }

  @Test
  void hashCorrompidoFalhaGenericamenteSemExporValor() throws Exception {
    cadastro();
    String corrompido = hash().replace("$10$", "$00$");
    assertFalse(corrompido.equals(hash()));
    jdbc.update("UPDATE conta_acesso SET senha_hash=? WHERE pessoa_id=?", corrompido, pessoaId);
    var r =
        mvc.perform(get("/api/contas-acesso/me").with(httpBasic(login, senha)))
            .andExpect(status().isUnauthorized())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertFalse(r.contains(corrompido));
    assertFalse(r.contains(senha));
  }

  @Test
  void autoridadesSaoDeAcessoNaoFuncaoProfissional() throws Exception {
    cadastro();
    assertEquals(
        Set.of("ROLE_OPERADOR"),
        details.loadUserByUsername(login).getAuthorities().stream()
            .map(Object::toString)
            .collect(java.util.stream.Collectors.toSet()));
  }

  @Test
  void papelAdministradorAindaNaoMudaAutorizacao() throws Exception {
    cadastro();
    jdbc.update(
        "INSERT INTO conta_acesso_perfil SELECT id,'ADMINISTRADOR' FROM conta_acesso WHERE"
            + " pessoa_id=?",
        pessoaId);
    mvc.perform(get("/api/jogos").with(httpBasic(login, senha))).andExpect(status().isOk());
    assertEquals(2, details.loadUserByUsername(login).getAuthorities().size());
  }

  @Test
  void privacidadeDeLogsEmFalhasDeAutenticacaoEPersistencia(
      org.springframework.boot.test.system.CapturedOutput output) throws Exception {
    cadastro();
    autentica("invalida-" + senha, 401);
    assertFalse(output.getAll().contains(senha));
    assertFalse(output.getAll().contains(hash()));
  }
}
