package br.com.emilly.jogos.dto;

import br.com.emilly.jogos.api.validation.*;
import br.com.emilly.jogos.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Schema(description = "Cria papel para pessoa ativa, preservando identidade única.")
public record ClienteRequest(
    @NotNull
        @Positive
        @Schema(
            description = "Pessoa existente e ativa",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
        Long pessoaId,
    @Size(max = 30)
        @Schema(
            description = "Telefone opcional; vazio remove contato",
            example = "(11) 99999-0000")
        String telefone) {}
