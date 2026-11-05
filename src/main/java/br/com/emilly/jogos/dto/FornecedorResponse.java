package br.com.emilly.jogos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação pública de fornecedor.")
public record FornecedorResponse(
        @Schema(description = "Identificador gerado pelo servidor", example = "1", accessMode = Schema.AccessMode.READ_ONLY, requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
        @Schema(description = "Razão social do fornecedor fictício", example = "Distribuidora Exemplo de Jogos", requiredMode = Schema.RequiredMode.REQUIRED)
        String razaoSocial,
        @Schema(description = "Identificador do fornecedor fictício", example = "00000000000000", pattern = "\\d{14}", requiredMode = Schema.RequiredMode.REQUIRED)
        String cnpj,
        @Schema(description = "Situação do cadastro; alterada somente pela operação de status", example = "ATIVO", allowableValues = {"ATIVO", "INATIVO"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String status
) {
}