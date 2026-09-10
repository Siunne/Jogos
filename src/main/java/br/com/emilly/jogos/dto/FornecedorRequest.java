package br.com.emilly.jogos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FornecedorRequest(

        @NotBlank(message = "A razão social é obrigatória")
        @Size(max = 150, message = "A razão social deve ter no máximo 150 caracteres")
        String razaoSocial,

        @NotBlank(message = "O CNPJ é obrigatório")
        @Pattern(
                regexp = "\\d{14}",
                message = "O CNPJ deve conter exatamente 14 dígitos"
        )
        String cnpj

) {
}