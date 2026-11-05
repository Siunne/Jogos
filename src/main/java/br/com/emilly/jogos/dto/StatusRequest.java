package br.com.emilly.jogos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import br.com.emilly.jogos.domain.Status;
import jakarta.validation.constraints.NotNull;
@Schema(description = "Ativação ou inativação; mantém o registro e seus relacionamentos.")
public record StatusRequest(@NotNull @Schema(description = "Situação do cadastro; alterada somente pela operação de status", example = "ATIVO", allowableValues = {"ATIVO", "INATIVO"}, requiredMode = Schema.RequiredMode.REQUIRED)
        Status status) {}
