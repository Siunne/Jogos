package br.com.emilly.jogos.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.security.jwt")
public record JwtProperties(String issuer, Duration expiration, String secret) {
  @Override
  public String toString() {
    return "JwtProperties[secret=OMITIDO]";
  }
}
