package br.com.emilly.jogos.dto;

import br.com.emilly.jogos.api.validation.SenhaValida;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(
    description =
        "Exige senha atual correta e nova senha diferente. Altera somente a conta autenticada.")
public record AlteracaoSenhaRequest(
    @NotBlank
        @SenhaValida
        @Schema(
            description = "Senha atual; U+0000 é proibido",
            pattern = "^[^\\u0000]*(?![\\s\\S])",
            minLength = 12,
            maxLength = 72,
            format = "password",
            accessMode = Schema.AccessMode.WRITE_ONLY)
        String senhaAtual,
    @NotBlank
        @SenhaValida
        @Schema(
            description =
                "Nova senha: mínimo 12 caracteres e máximo 72 bytes UTF-8; não pode repetir a"
                    + " atual. U+0000 é proibido.",
            pattern = "^[^\\u0000]*(?![\\s\\S])",
            minLength = 12,
            maxLength = 72,
            format = "password",
            accessMode = Schema.AccessMode.WRITE_ONLY)
        String novaSenha) {
  @Override
  public String toString() {
    return "AlteracaoSenhaRequest[credenciais omitidas]";
  }
}
