package br.com.emilly.jogos.security;

import java.time.*;
import java.util.*;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {
  @Bean
  Clock jwtClock() {
    return Clock.systemUTC();
  }

  @Bean
  SecretKey jwtSecretKey(JwtProperties properties) {
    byte[] bytes;
    try {
      bytes = Base64.getDecoder().decode(properties.secret());
    } catch (RuntimeException invalid) {
      throw new IllegalStateException("JWT_SECRET obrigatória e codificada em Base64");
    }
    if (bytes.length < 32) throw new IllegalStateException("JWT_SECRET requer pelo menos 256 bits");
    if (properties.issuer() == null
        || !properties.issuer().startsWith("https://")
        || !Duration.ofMinutes(30).equals(properties.expiration()))
      throw new IllegalStateException("JWT requer emissor HTTPS e duração PT30M");
    return new SecretKeySpec(bytes, "HmacSHA256");
  }

  @Bean
  JwtEncoder jwtEncoder(SecretKey key) {
    return NimbusJwtEncoder.withSecretKey(key).algorithm(MacAlgorithm.HS256).build();
  }

  @Bean
  JwtDecoder jwtDecoder(SecretKey key, JwtProperties properties, Clock clock) {
    var decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    var timestamps = new JwtTimestampValidator(Duration.ZERO);
    timestamps.setClock(clock);
    OAuth2TokenValidator<Jwt> required =
        jwt -> {
          // exp é absoluto, sem tolerância além dos 1800 segundos contratados.
          boolean valid =
              jwt.getExpiresAt() != null
                  && jwt.getIssuedAt() != null
                  && clock.instant().isBefore(jwt.getExpiresAt())
                  && !jwt.getIssuedAt().isAfter(clock.instant())
                  && Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())
                      .equals(properties.expiration())
                  && jwt.getSubject() != null
                  && !jwt.getSubject().isBlank()
                  && jwt.getId() != null
                  && !jwt.getId().isBlank()
                  && jwt.getClaims().get("perfis") instanceof List<?> roles
                  && !roles.isEmpty()
                  && roles.stream()
                      .allMatch(
                          r ->
                              r instanceof String role
                                  && Set.of("OPERADOR", "ADMINISTRADOR").contains(role));
          return valid
              ? OAuth2TokenValidatorResult.success()
              : OAuth2TokenValidatorResult.failure(
                  new OAuth2Error("invalid_token", "Token inválido", null));
        };
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(
            timestamps, new JwtIssuerValidator(properties.issuer()), required));
    return new ClaimsOriginaisJwtDecoder(decoder, clock, properties.expiration());
  }
}
