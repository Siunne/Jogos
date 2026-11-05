package br.com.emilly.jogos.dto;

import br.com.emilly.jogos.config.SchemaPatterns;

import br.com.emilly.jogos.api.validation.*;
import br.com.emilly.jogos.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Schema(
    description =
        "Criação/alteração da identidade. CPF com DVs válidos e sem pontuação; data/status"
            + " controlados pelo servidor.")
public record PessoaRequest(
    @NotBlank
        @Size(max = 150)
        @Schema(
            description = "Nome civil obrigatório",
            example = "Pessoa Exemplo",
            minLength = 1,
            pattern = SchemaPatterns.NORMALIZADO_NAO_BRANCO,
            requiredMode = Schema.RequiredMode.REQUIRED)
        String nome,
    @NotBlank
        @Email
        @Size(max = 180)
        @Schema(
            description = "E-mail único, normalizado em minúsculas; limite de 180 caracteres também após normalização",
            example = "pessoa@example.com",
            minLength = 1,
            pattern = SchemaPatterns.NORMALIZADO_NAO_BRANCO,
            requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
    @NotBlank
        @CpfValido
        @Schema(
            description =
                "CPF de 11 dígitos, matematicamente válido; não prova existência ou titularidade",
            example = "52998224725",
            minLength = 11,
            maxLength = 11,
            pattern = "[0-9]{11}",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String cpf) {}
