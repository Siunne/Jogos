package br.com.emilly.jogos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
@Schema(description = "Atualização do nome do gênero; não altera o status.")
public record GeneroJogoAtualizacaoRequest(@NotBlank @Size(max = 120) @Schema(description = "Nome do gênero; atualização permite até 120 caracteres", example = "Aventura e exploração", maxLength = 120, requiredMode = Schema.RequiredMode.REQUIRED)
        String nome) {}
