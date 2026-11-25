package br.com.emilly.jogos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record TokenResponse(
    @Schema(
            description = "JWT assinado HS256; segredo de acesso, não publicar",
            example = "TOKEN_OMITIDO")
        String accessToken,
    @Schema(allowableValues = "Bearer", example = "Bearer") String tokenType,
    @Schema(description = "Validade absoluta em segundos; uso não prolonga", example = "1800")
        long expiresIn,
    @Schema(description = "Instante UTC de expiração", example = "2026-11-05T20:30:00Z")
        Instant expiresAt) {
  @Override
  public String toString() {
    return "TokenResponse[accessToken=OMITIDO]";
  }
}
