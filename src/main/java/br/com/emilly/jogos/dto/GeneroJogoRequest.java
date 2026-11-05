package br.com.emilly.jogos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Cadastro de gênero ativo com nome único, ignorando caixa e espaços externos.")
public record GeneroJogoRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        @Schema(description = "Nome do gênero; cadastro permite até 100 caracteres", example = "Aventura", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED)
        String nome

) {
}