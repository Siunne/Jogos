package br.com.emilly.jogos.dto;

import br.com.emilly.jogos.api.validation.SenhaValida;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Bootstrap didático: somente OPERADOR e ATIVA, sem escolha de privilégios.")
public record ContaAcessoRequest(
    @NotNull @Positive @Schema(description = "Pessoa existente e ativa", example = "1")
        Long pessoaId,
    @NotBlank
        @Pattern(regexp = "^ *[A-Za-z0-9._-]{3,80} *(?![\\s\\S])", message = "Login inválido")
        @Schema(
            description =
                "3 a 80 caracteres ASCII após remover somente espaços U+0020 externos; normalizado"
                    + " em minúsculas. Permitidos letras, números, ponto, hífen e sublinhado.",
            example = "operador.jogos")
        String login,
    @NotBlank
        @SenhaValida
        @Schema(
            description =
                "Mínimo 12 caracteres Unicode e máximo 72 bytes UTF-8; nunca devolvida. Não é"
                    + " normalizada. U+0000 é proibido.",
            pattern = "^[^\\u0000]*(?![\\s\\S])",
            minLength = 12,
            maxLength = 72,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            format = "password")
        String senha) {
  @Override
  public String toString() {
    return "ContaAcessoRequest[credenciais omitidas]";
  }
}
