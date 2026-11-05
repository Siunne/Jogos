package br.com.emilly.jogos.dto;

import br.com.emilly.jogos.api.validation.*;
import br.com.emilly.jogos.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Schema(
    description =
        "Inclusão sem baixa de estoque; preço vem do jogo e não pode ser informado pelo cliente.")
public record ItemVendaRequest(
    @NotNull
        @Positive
        @Schema(
            description = "Jogo ativo existente",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
        Long jogoId,
    @NotNull
        @DecimalMin(value = "0.001", message = "Quantidade mínima é 0.001")
        @Digits(integer = 15, fraction = 3)
        @Schema(
            description = "Quantidade positiva; até 15 inteiros e 3 casas",
            example = "2",
            minimum = "0.001",
            maximum = "999999999999999.999",
            multipleOf = 0.001,
            requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal quantidade) {}
