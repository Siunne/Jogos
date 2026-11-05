package br.com.emilly.jogos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Cadastro de fornecedor ativo; os dados do exemplo são fictícios.")
public record FornecedorRequest(

        @NotBlank(message = "A razão social é obrigatória")
        @Size(max = 150, message = "A razão social deve ter no máximo 150 caracteres")
        @Schema(description = "Razão social do fornecedor fictício", example = "Distribuidora Exemplo de Jogos", maxLength = 150, requiredMode = Schema.RequiredMode.REQUIRED)
        String razaoSocial,

        @NotBlank(message = "O CNPJ é obrigatório")
        @Pattern(
                regexp = "\\d{14}",
                message = "O CNPJ deve conter exatamente 14 dígitos"
        )
        @Schema(description = "Identificador de 14 dígitos, único; exemplo deliberadamente fictício", example = "00000000000000", pattern = "\\d{14}", minLength = 14, maxLength = 14, requiredMode = Schema.RequiredMode.REQUIRED)
        String cnpj

) {
}