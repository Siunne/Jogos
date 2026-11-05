package br.com.emilly.jogos.dto;

import br.com.emilly.jogos.config.SchemaPatterns;

import br.com.emilly.jogos.api.validation.*;
import br.com.emilly.jogos.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Schema(description = "Vínculo com pessoa ativa. Função profissional não é autorização.")
public record ColaboradorRequest(
    @NotNull
        @Positive
        @Schema(
            description = "Pessoa existente ativa",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
        Long pessoaId,
    @NotBlank
        @Size(max = 30)
        @Schema(
            description = "Matrícula única",
            example = "VEN-001",
            minLength = 1,
            pattern = SchemaPatterns.NORMALIZADO_NAO_BRANCO,
            requiredMode = Schema.RequiredMode.REQUIRED)
        String matricula,
    @NotNull
        @Schema(
            description = "Função profissional; somente VENDEDOR/GERENTE vendem",
            example = "VENDEDOR",
            requiredMode = Schema.RequiredMode.REQUIRED)
        FuncaoColaborador funcao,
    @NotNull
        @Schema(
            description = "Data obrigatória do vínculo",
            example = "2026-11-05",
            requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate dataAdmissao) {}
