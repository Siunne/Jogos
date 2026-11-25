package br.com.emilly.jogos.application;

import br.com.emilly.jogos.dto.TokenResponse;
import br.com.emilly.jogos.security.JwtProperties;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
  private final JwtEncoder encoder;
  private final JwtProperties properties;
  private final Clock clock;

  public TokenService(JwtEncoder encoder, JwtProperties properties, Clock clock) {
    this.encoder = encoder;
    this.properties = properties;
    this.clock = clock;
  }

  public TokenResponse emitir(Authentication authentication) {
    Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);
    Instant expires = now.plus(properties.expiration());
    var roles =
        authentication.getAuthorities().stream()
            .map(a -> a.getAuthority())
            .filter(a -> a.startsWith("ROLE_"))
            .map(a -> a.substring(5))
            .sorted()
            .toList();
    var claims =
        JwtClaimsSet.builder()
            .issuer(properties.issuer())
            .subject(authentication.getName())
            .issuedAt(now)
            .expiresAt(expires)
            .id(UUID.randomUUID().toString())
            .claim("perfis", roles)
            .build();
    return new TokenResponse(
        encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue(),
        "Bearer",
        properties.expiration().toSeconds(),
        expires);
  }
}
