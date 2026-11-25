package br.com.emilly.jogos.security;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

class JwtConfigurationTest {
  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"invalid-secret!", "Y3VydGE="})
  void chaveAusenteMalformadaOuCurtaFalhaSemFallback(String secret) {
    var config = new JwtConfig();
    var ex =
        assertThrows(
            IllegalStateException.class,
            () ->
                config.jwtSecretKey(
                    new JwtProperties("https://jogos.local", Duration.ofMinutes(30), secret)));
    assertNull(ex.getCause());
    if (secret != null && !secret.isEmpty()) assertFalse(ex.getMessage().contains(secret));
    var runner =
        new org.springframework.boot.test.context.runner.ApplicationContextRunner()
            .withUserConfiguration(JwtConfig.class)
            .withPropertyValues(
                "app.security.jwt.issuer=https://jogos.local", "app.security.jwt.expiration=PT30M");
    if (secret != null) runner = runner.withPropertyValues("app.security.jwt.secret=" + secret);
    runner.run(
        context -> {
          assertNotNull(context.getStartupFailure(), "Configuração insegura não pode iniciar");
          assertTrue(context.getStartupFailure().getMessage().contains("jwtSecretKey"));
        });
  }

  @Test
  void chaveDe256BitsAceita() {
    var p =
        new JwtProperties(
            "https://jogos.local",
            Duration.ofMinutes(30),
            Base64.getEncoder().encodeToString(new byte[32]));
    assertEquals(32, new JwtConfig().jwtSecretKey(p).getEncoded().length);
    assertFalse(p.toString().contains(p.secret()));
  }

  @Test
  void duracaoOuEmissorInvalidoFalham() {
    String test = Base64.getEncoder().encodeToString(new byte[32]);
    assertThrows(
        IllegalStateException.class,
        () ->
            new JwtConfig()
                .jwtSecretKey(
                    new JwtProperties("http://jogos.local", Duration.ofMinutes(30), test)));
    assertThrows(
        IllegalStateException.class,
        () ->
            new JwtConfig()
                .jwtSecretKey(new JwtProperties("https://jogos.local", Duration.ofHours(1), test)));
  }
}
