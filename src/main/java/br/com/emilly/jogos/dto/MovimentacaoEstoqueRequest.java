package br.com.emilly.jogos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
@Schema(description = "Quantidade positiva para movimento acumulativo, não idempotente.")
public record MovimentacaoEstoqueRequest(
        @NotNull @Positive @Digits(integer = 15, fraction = 3) @Schema(description = "Quantidade maior que zero; até 15 dígitos inteiros e 3 decimais", example = "5.000", exclusiveMinimumValue = 0, maximum = "999999999999999.999", multipleOf = 0.001, requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal quantidade) {}
