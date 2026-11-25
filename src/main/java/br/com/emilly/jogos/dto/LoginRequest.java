package br.com.emilly.jogos.dto;

import br.com.emilly.jogos.config.SchemaPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(
    description =
        "Credenciais de login. A política de senha é verificada pelo AuthenticationManager; nunca"
            + " registrada.")
public record LoginRequest(
    @NotBlank
        @Size(max = 80)
        @Schema(
            description = "Login cadastrado, sem diferença de caixa",
            example = "operador.jogos",
            pattern = SchemaPatterns.NAO_BRANCO,
            maxLength = 80)
        String login,
    @NotBlank
        @Schema(
            description =
                "Mínimo 12 caracteres Unicode, máximo 72 bytes UTF-8, sem U+0000. Não há"
                    + " normalização da senha. Credencial inválida retorna 401.",
            accessMode = Schema.AccessMode.WRITE_ONLY,
            format = "password",
            minLength = 12,
            maxLength = 72,
            pattern = "^(?=[^\\u0000]*(?![\\s\\S]))" + SchemaPatterns.NAO_BRANCO + "(?![\\s\\S])")
        String senha) {
  @Override
  public String toString() {
    return "LoginRequest[credenciais=OMITIDAS]";
  }
}
