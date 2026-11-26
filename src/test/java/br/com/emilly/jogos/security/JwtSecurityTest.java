package br.com.emilly.jogos.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.emilly.jogos.application.*;
import br.com.emilly.jogos.domain.Status;
import br.com.emilly.jogos.dto.*;
import br.com.emilly.jogos.repository.ContaAcessoRepository;
import br.com.emilly.jogos.support.DocumentosTeste;
import com.fasterxml.jackson.databind.*;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.*;
import java.math.*;
import java.time.*;
import java.util.*;
import java.util.stream.Stream;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.system.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * HTTP real com filtros ativos, contas persistidas e relógio controlado; sem identidade simulada.
 */
@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@ActiveProfiles("test")
@org.junit.jupiter.api.extension.ExtendWith(OutputCaptureExtension.class)
class JwtSecurityTest {
  @TestConfiguration
  static class TimeConfig {
    @Bean
    @Primary
    MutableClock controlledClock() {
      return new MutableClock();
    }
  }

  static class MutableClock extends Clock {
    Instant value = Instant.now();

    @Override
    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return value;
    }
  }

  @Autowired MockMvc mvc;
  @Autowired PessoaService pessoas;
  @Autowired org.springframework.security.crypto.password.PasswordEncoder encoder;
  @Autowired ContaAcessoRepository repository;
  @Autowired JdbcTemplate jdbc;
  @Autowired TransactionTemplate tx;
  @Autowired MutableClock clock;
  @Autowired JwtDecoder decoder;
  @Autowired SecretKey key;
  @Autowired JwtProperties properties;
  ObjectMapper json = new ObjectMapper().findAndRegisterModules();
  long pessoa;
  String login, senha;

  @BeforeEach
  void setup() {
    assertEquals("jogos_test", jdbc.queryForObject("select current_database()", String.class));
    clock.value = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    login = "jwt-" + UUID.randomUUID().toString().replace("-", "");
    senha = "Ficticia-" + UUID.randomUUID();
    pessoa =
        pessoas
            .cadastrar("Pessoa fictícia JWT", login + "@example.invalid", DocumentosTeste.cpf())
            .getId();
    repository.saveAndFlush(new br.com.emilly.jogos.domain.ContaAcesso(pessoas.buscar(pessoa), login, encoder.encode(senha)));
  }

  @AfterEach
  void cleanup() {
    tx.executeWithoutResult(
        s -> {
          jdbc.update("delete from conta_acesso where pessoa_id=?", pessoa);
          jdbc.update("delete from pessoa where id=?", pessoa);
        });
  }

  org.springframework.mock.web.MockHttpServletResponse login(String l, String pass, int status)
      throws Exception {
    return mvc.perform(
            post("/api/autenticacao/login")
                .contentType("application/json")
                .content(json.writeValueAsString(Map.of("login", l, "senha", pass))))
        .andExpect(status().is(status))
        .andReturn()
        .getResponse();
  }

  String token() throws Exception {
    return json.readTree(login(login, senha, 200).getContentAsString())
        .path("accessToken")
        .asText();
  }

  void acesso(String token, int expected) throws Exception {
    mvc.perform(get("/api/contas-acesso/me").header("Authorization", "Bearer " + token))
        .andExpect(status().is(expected));
  }

  String assinar(Map<String, Object> changes, JWSAlgorithm algorithm) throws Exception {
    var claims = new LinkedHashMap<String, Object>();
    claims.put("iss", properties.issuer());
    claims.put("sub", login);
    claims.put("iat", Date.from(clock.instant()));
    claims.put("exp", Date.from(clock.instant().plusSeconds(1800)));
    claims.put("jti", UUID.randomUUID().toString());
    claims.put("perfis", List.of("OPERADOR"));
    claims.putAll(changes);
    claims.replaceAll(
        (name, value) -> value instanceof Date date ? date.toInstant().getEpochSecond() : value);
    var jwt = new SignedJWT(new JWSHeader(algorithm), JWTClaimsSet.parse(claims));
    jwt.sign(new MACSigner(key.getEncoded()));
    return jwt.serialize();
  }

  // Não usar JWTClaimsSet.parse neste fixture: ele próprio converteria NumericDate
  // antes de assinar, escondendo justamente o defeito que a regressão deve detectar.
  String assinarOriginais(Map<String, Object> changes, JWSAlgorithm algorithm) throws Exception {
    var claims = new LinkedHashMap<String, Object>();
    claims.put("iss", properties.issuer());
    claims.put("sub", login);
    claims.put("iat", clock.instant().getEpochSecond());
    claims.put("exp", clock.instant().plusSeconds(1800).getEpochSecond());
    claims.put("jti", UUID.randomUUID().toString());
    claims.put("perfis", List.of("OPERADOR"));
    claims.putAll(changes);
    var jws = new JWSObject(new JWSHeader(algorithm), new Payload(json.writeValueAsString(claims)));
    jws.sign(new MACSigner(key.getEncoded()));
    return jws.serialize();
  }

  static Stream<Arguments> claimsTemporaisInvalidos() {
    Object[] values = {
      null,
      "0",
      "1e300",
      true,
      List.of(),
      Map.of("valor", 1),
      new BigDecimal("1e300"),
      new BigDecimal("-1e300"),
      Long.MAX_VALUE,
      Long.MIN_VALUE,
      BigInteger.valueOf(Long.MAX_VALUE / 1000).add(BigInteger.ONE),
      BigInteger.valueOf(Long.MIN_VALUE / 1000).subtract(BigInteger.ONE)
    };
    return Stream.of("iat", "exp", "nbf")
        .flatMap(field -> Arrays.stream(values).map(value -> Arguments.of(field, value)));
  }

  @ParameterizedTest
  @MethodSource("claimsTemporaisInvalidos")
  void claimsTemporaisOriginaisInvalidosSaoRejeitados(String field, Object value) throws Exception {
    var changes = new HashMap<String, Object>();
    changes.put(field, value);
    acesso(assinarOriginais(changes, JWSAlgorithm.HS256), 401);
  }

  @ParameterizedTest
  @ValueSource(longs = {2305843009213693952L, -2305843009213693952L})
  void iatEExpExtremosCoordenadosNaoPodemVirarTokenAtual(long offset) throws Exception {
    long now = clock.instant().getEpochSecond();
    acesso(
        assinarOriginais(
            Map.of("iat", now + offset, "exp", now + 1800 + offset), JWSAlgorithm.HS256),
        401);
  }

  static Stream<Arguments> subjectsInvalidos() {
    return Stream.of(0L, 123L, null, true, List.of(), List.of("login"), Map.of("login", "texto"))
        .map(value -> Arguments.of(value));
  }

  @ParameterizedTest
  @MethodSource("subjectsInvalidos")
  void subOriginalDeveSerString(Object value) throws Exception {
    var changes = new HashMap<String, Object>();
    changes.put("sub", value);
    acesso(assinarOriginais(changes, JWSAlgorithm.HS256), 401);
  }

  @Test
  void stringDeLoginENbfNumericoValidoContinuamAceitos() throws Exception {
    for (Number nbf : List.of(-9223372036854775L, -1L, 0L, new BigDecimal("0.5"))) {
      acesso(assinarOriginais(Map.of("sub", login, "nbf", nbf), JWSAlgorithm.HS256), 200);
    }
    acesso(assinarOriginais(Map.of(), JWSAlgorithm.HS256), 200);
    acesso(token(), 200);
  }

  @Test
  void nbfOriginalFuturoContinuaRejeitado() throws Exception {
    acesso(
        assinarOriginais(
            Map.of("nbf", clock.instant().plusSeconds(60).getEpochSecond()), JWSAlgorithm.HS256),
        401);
  }

  static Stream<Arguments> instantesFracionariosInvalidos() {
    return Stream.of("iat", "nbf", "exp")
        .flatMap(
            field ->
                Stream.of("0.5", "0.0000001")
                    .map(fraction -> Arguments.of(field, new BigDecimal(fraction))));
  }

  @ParameterizedTest
  @MethodSource("instantesFracionariosInvalidos")
  void instantesOriginaisNaoPodemSerTruncadosParaValidarToken(String field, BigDecimal fraction)
      throws Exception {
    var now = BigDecimal.valueOf(clock.instant().getEpochSecond());
    var changes = new HashMap<String, Object>();
    if (field.equals("iat")) {
      changes.put("iat", now.add(fraction));
      changes.put("exp", now.add(BigDecimal.valueOf(1800)).add(fraction));
    } else if (field.equals("nbf")) {
      changes.put("nbf", now.add(fraction));
    } else {
      changes.put("exp", now.add(BigDecimal.valueOf(1800)).add(fraction));
    }
    acesso(assinarOriginais(changes, JWSAlgorithm.HS256), 401);
  }

  @ParameterizedTest
  @ValueSource(strings = {"HS384", "HS512"})
  void claimsValidosNaoPermitemAlgoritmoDiferenteDeHs256(String algorithm) throws Exception {
    acesso(assinarOriginais(Map.of(), JWSAlgorithm.parse(algorithm)), 401);
  }

  @Test
  void rejeicaoDeClaimsOriginaisNaoExpoePayloadTokenOuChave(CapturedOutput output)
      throws Exception {
    String marker = "MARCADOR_FICTICIO_" + UUID.randomUUID();
    String t =
        assinarOriginais(
            Map.of("nbf", new BigDecimal("1e300"), "marcador", marker), JWSAlgorithm.HS256);
    String body =
        mvc.perform(get("/api/jogos").header("Authorization", "Bearer " + t))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andReturn()
            .getResponse()
            .getContentAsString();
    for (String sensitive : List.of(t, marker, properties.secret())) {
      assertFalse(body.contains(sensitive));
      assertFalse(output.getAll().contains(sensitive));
    }
  }

  @Test
  void loginEmiteClaimsExatosSemDadosPessoais() throws Exception {
    var body = json.readTree(login(login, senha, 200).getContentAsString());
    String token = body.path("accessToken").asText();
    assertEquals(3, token.split("\\.").length);
    var jwt = decoder.decode(token);
    assertEquals("HS256", jwt.getHeaders().get("alg"));
    assertEquals(properties.issuer(), jwt.getIssuer().toString());
    assertEquals(login, jwt.getSubject());
    assertEquals(clock.instant(), jwt.getIssuedAt());
    assertEquals(1800, Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()).toSeconds());
    assertEquals(Set.of("iss", "sub", "iat", "exp", "jti", "perfis"), jwt.getClaims().keySet());
    assertEquals(List.of("OPERADOR"), jwt.getClaimAsStringList("perfis"));
    assertEquals("Bearer", body.path("tokenType").asText());
    assertEquals(1800, body.path("expiresIn").asLong());
    assertEquals(jwt.getExpiresAt(), Instant.parse(body.path("expiresAt").asText()));
    assertNotEquals(jwt.getId(), decoder.decode(token()).getId());
    acesso(token, 200);
  }

  @Test
  void basicEAnonimoNaoLiberamRota() throws Exception {
    mvc.perform(get("/api/jogos")).andExpect(status().isUnauthorized());
    mvc.perform(
            get("/api/jogos")
                .with(
                    org.springframework.security.test.web.servlet.request
                        .SecurityMockMvcRequestPostProcessors.httpBasic(login, senha)))
        .andExpect(status().isUnauthorized())
        .andExpect(header().string("WWW-Authenticate", "Bearer realm=\"Jogos\""));
  }

  @Test
  void credenciaisErradasTemMesmoErroGenerico() throws Exception {
    var a = json.readTree(login(login, "senha-incorreta-ficticia", 401).getContentAsString());
    var b = json.readTree(login("inexistente-" + login, senha, 401).getContentAsString());
    assertEquals("Login ou senha inválidos", a.path("message").asText());
    for (String field : List.of("status", "message", "error", "path", "fields"))
      assertEquals(a.path(field), b.path(field));
    assertEquals("/api/autenticacao/login", a.path("path").asText());
  }

  @Test
  void nulEBcryptEquivalenteNaoAutenticam(CapturedOutput output) throws Exception {
    String old =
        jdbc.queryForObject(
            "select senha_hash from conta_acesso where pessoa_id=?", String.class, pessoa);
    for (int n : List.of(12, 13, 20)) login(login, "\0".repeat(n), 401);
    var independent = new TransactionTemplate(tx.getTransactionManager());
    independent.setPropagationBehavior(
        org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    assertEquals(
        old,
        independent.execute(
            s ->
                jdbc.queryForObject(
                    "select senha_hash from conta_acesso where pessoa_id=?",
                    String.class,
                    pessoa)));
    assertFalse(output.getAll().contains(old));
  }

  @Test
  void bloqueioImpedeNovoLoginMasNaoRevogaJWT() throws Exception {
    String t = token();
    tx.executeWithoutResult(
        s -> repository.buscarPorLoginNormalizado(login).orElseThrow().bloquear());
    login(login, senha, 401);
    acesso(t, 200);
  }

  @Test
  void pessoaInativaImpedeNovoLoginMasNaoRevogaJWT() throws Exception {
    String t = token();
    jdbc.update("UPDATE pessoa SET status='INATIVO' WHERE id=?", pessoa);
    login(login, senha, 401);
    acesso(t, 200);
  }

  @Test
  void trocarSenhaNaoRevogaTokenEmitido() throws Exception {
    String t = token(), nova = "Unicode-😀-" + UUID.randomUUID();
    mvc.perform(
            put("/api/contas-acesso/me/senha")
                .header("Authorization", "Bearer " + t)
                .contentType("application/json")
                .content(json.writeValueAsString(Map.of("senhaAtual", senha, "novaSenha", nova))))
        .andExpect(status().isNoContent());
    login(login, senha, 401);
    login(login, nova, 200);
    acesso(t, 200);
  }

  @Test
  void expiraExatamenteEm1800SegundosSemRenovacaoPorUso() throws Exception {
    String t = token();
    Instant issued = clock.instant();
    clock.value = issued.plusSeconds(1799);
    acesso(t, 200);
    assertEquals(issued.plusSeconds(1800), decoder.decode(t).getExpiresAt());
    clock.value = issued.plusSeconds(1800);
    acesso(t, 401);
  }

  @Test
  void assinaturaAlteradaERejeitada() throws Exception {
    String t = token();
    String[] parts = t.split("\\.");
    byte[] signature = Base64.getUrlDecoder().decode(parts[2]);
    signature[0] ^= 1;
    acesso(
        parts[0]
            + "."
            + parts[1]
            + "."
            + Base64.getUrlEncoder().withoutPadding().encodeToString(signature),
        401);
  }

  @Test
  void payloadAlteradoERejeitado() throws Exception {
    String[] parts = token().split("\\.");
    var claims = json.readTree(Base64.getUrlDecoder().decode(parts[1]));
    ((com.fasterxml.jackson.databind.node.ObjectNode) claims).put("sub", "outro-login");
    acesso(
        parts[0]
            + "."
            + Base64.getUrlEncoder().withoutPadding().encodeToString(json.writeValueAsBytes(claims))
            + "."
            + parts[2],
        401);
  }

  @Test
  void emissorIncorretoERejeitadoMesmoComAssinaturaValida() throws Exception {
    acesso(assinar(Map.of("iss", "https://outro.example.invalid"), JWSAlgorithm.HS256), 401);
  }

  @Test
  void algoritmoHS384ERejeitadoMesmoComMesmaChave() throws Exception {
    acesso(assinar(Map.of(), JWSAlgorithm.HS384), 401);
  }

  @Test
  void algoritmoNoneNaoEAutenticacao() throws Exception {
    var claims =
        new JWTClaimsSet.Builder()
            .issuer(properties.issuer())
            .subject(login)
            .issueTime(Date.from(clock.instant()))
            .expirationTime(Date.from(clock.instant().plusSeconds(1800)))
            .jwtID(UUID.randomUUID().toString())
            .claim("perfis", List.of("OPERADOR"))
            .build();
    acesso(new PlainJWT(claims).serialize(), 401);
  }

  @Test
  void assinaturaComOutraChaveERejeitada() throws Exception {
    var parsed = SignedJWT.parse(token());
    byte[] unrelated = new byte[64];
    new java.security.SecureRandom().nextBytes(unrelated);
    var forged = new SignedJWT(parsed.getHeader(), parsed.getJWTClaimsSet());
    forged.sign(new MACSigner(unrelated));
    acesso(forged.serialize(), 401);
  }

  @ParameterizedTest
  @ValueSource(strings = {"iss", "sub", "iat", "exp", "jti", "perfis"})
  void claimsObrigatoriosAusentesSaoRejeitados(String field) throws Exception {
    var changes = new HashMap<String, Object>();
    changes.put(field, null);
    acesso(assinar(changes, JWSAlgorithm.HS256), 401);
  }

  @Test
  void perfisInvalidosNaoProduzemErroInterno() throws Exception {
    for (Object invalido : List.of(List.of(), "OPERADOR", Map.of("perfil", "OPERADOR"),
        1, true, List.of("INVENTADO"), List.of("ROLE_ADMINISTRADOR"), List.of("administrador"),
        List.of(" ADMINISTRADOR "), List.of("OPERADOR", "INVENTADO"))) {
      acesso(assinar(Map.of("perfis", invalido), JWSAlgorithm.HS256), 401);
    }
    acesso(assinar(Map.of("perfis", List.of("OPERADOR", "OPERADOR")), JWSAlgorithm.HS256), 200);
    acesso(assinar(Map.of("perfis", Arrays.asList((Object) null)), JWSAlgorithm.HS256), 401);
  }

  @Test
  void tokenExpiradoAssinadoERejeitado() throws Exception {
    acesso(
        assinar(
            Map.of(
                "iat",
                Date.from(clock.instant().minusSeconds(1801)),
                "exp",
                Date.from(clock.instant().minusSeconds(1))),
            JWSAlgorithm.HS256),
        401);
  }

  @Test
  void iatENbfFuturosSaoRejeitados() throws Exception {
    acesso(
        assinar(
            Map.of(
                "iat",
                Date.from(clock.instant().plusSeconds(1)),
                "exp",
                Date.from(clock.instant().plusSeconds(1801))),
            JWSAlgorithm.HS256),
        401);
    acesso(
        assinar(Map.of("nbf", Date.from(clock.instant().plusSeconds(1))), JWSAlgorithm.HS256), 401);
  }

  @ParameterizedTest
  @ValueSource(strings = {"invalido", "a.b.c", "eyJhbGciOiJub25lIn0.e30.", "abc.def"})
  void formatosInvalidosNaoProduzem500(String t) throws Exception {
    acesso(t, 401);
  }

  @Test
  void perfisDeclaradosPeloClienteSaoIgnorados() throws Exception {
    var r =
        mvc.perform(
                post("/api/autenticacao/login")
                    .contentType("application/json")
                    .content(
                        json.writeValueAsString(
                            Map.of(
                                "login",
                                login,
                                "senha",
                                senha,
                                "perfis",
                                List.of("ADMINISTRADOR")))))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
    assertEquals(
        List.of("OPERADOR"),
        decoder
            .decode(json.readTree(r.getContentAsString()).path("accessToken").asText())
            .getClaimAsStringList("perfis"));
  }

  @Test
  void tokenSenhaChaveEHashNaoAparecemEmLogsOuErros(CapturedOutput output) throws Exception {
    String t = token();
    acesso(t + "invalido", 401);
    login(login, senha + "incorreta", 401);
    assertFalse(output.getAll().contains(t));
    assertFalse(output.getAll().contains(senha));
    assertFalse(output.getAll().contains(properties.secret()));
    String hash =
        jdbc.queryForObject(
            "select senha_hash from conta_acesso where pessoa_id=?", String.class, pessoa);
    assertFalse(output.getAll().contains(hash));
    assertFalse(properties.toString().contains(properties.secret()));
    assertFalse(new LoginRequest(login, senha).toString().contains(senha));
    assertFalse(new TokenResponse(t, "Bearer", 1800, clock.instant()).toString().contains(t));
  }

  @Test
  void camposAusentesRetornam400SemValoresRejeitados() throws Exception {
    mvc.perform(post("/api/autenticacao/login").contentType("application/json").content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fields.login").exists())
        .andExpect(jsonPath("$.fields.senha").exists());
    var doc =
        json.readTree(
            mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
    for (String field : List.of("login", "senha")) {
      var pattern =
          java.util.regex.Pattern.compile(
              doc.at("/components/schemas/LoginRequest/properties/" + field + "/pattern").asText());
      for (String blank : List.of("", " ".repeat(12), "\t\n".repeat(12), "\u2003".repeat(12))) {
        assertFalse(pattern.matcher(blank).find());
        mvc.perform(
                post("/api/autenticacao/login")
                    .contentType("application/json")
                    .content(
                        json.writeValueAsString(
                            Map.of(
                                "login",
                                field.equals("login") ? blank : login,
                                "senha",
                                field.equals("senha") ? blank : senha))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fields." + field).exists());
      }
      assertTrue(pattern.matcher("\u00a0".repeat(12)).find());
    }
  }
}
