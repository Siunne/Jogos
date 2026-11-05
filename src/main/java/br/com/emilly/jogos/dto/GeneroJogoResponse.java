package br.com.emilly.jogos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de gênero usada nas consultas e alterações.")
public record GeneroJogoResponse(
        @Schema(description = "Identificador gerado pelo servidor", example = "1", accessMode = Schema.AccessMode.READ_ONLY, requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
        @Schema(description = "Nome normalizado do gênero", example = "Aventura", requiredMode = Schema.RequiredMode.REQUIRED)
        String nome,
        @Schema(description = "Situação do cadastro; alterada somente pela operação de status", example = "ATIVO", allowableValues = {"ATIVO", "INATIVO"}, requiredMode = Schema.RequiredMode.REQUIRED)
        br.com.emilly.jogos.domain.Status status
) {
}
