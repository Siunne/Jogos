package br.com.emilly.jogos.dto;

import br.com.emilly.jogos.config.SchemaPatterns;

import br.com.emilly.jogos.api.validation.*;
import br.com.emilly.jogos.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Schema(description = "Atualiza matrícula e função; preserva pessoa e admissão.")
public record ColaboradorAtualizacaoRequest(
    @NotBlank
        @Size(max = 30)
        @Schema(
            description = "Matrícula única",
            example = "VEN-002",
            minLength = 1,
            pattern = SchemaPatterns.NORMALIZADO_NAO_BRANCO,
            requiredMode = Schema.RequiredMode.REQUIRED)
        String matricula,
    @NotNull
        @Schema(
            description = "Função profissional",
            example = "GERENTE",
            requiredMode = Schema.RequiredMode.REQUIRED)
        FuncaoColaborador funcao) {}
