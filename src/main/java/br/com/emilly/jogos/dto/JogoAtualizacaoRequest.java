package br.com.emilly.jogos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
@Schema(description = "Representação editável completa. Não altera código, saldo, data de cadastro nem status.")
public record JogoAtualizacaoRequest(
        @NotBlank @Size(max = 150) @Schema(description = "Novo nome do jogo", example = "Reino das Estrelas — Edição Completa", maxLength = 150, requiredMode = Schema.RequiredMode.REQUIRED)
        String nome,
        @NotNull @PositiveOrZero @Digits(integer = 16, fraction = 2) @Schema(description = "Novo preço; até 16 dígitos inteiros e 2 decimais", example = "149.90", minimum = "0", maximum = "9999999999999999.99", multipleOf = 0.01, requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal preco,
        @NotNull @PositiveOrZero @Digits(integer = 15, fraction = 3) @Schema(description = "Novo mínimo; até 15 dígitos inteiros e 3 decimais", example = "3.000", minimum = "0", maximum = "999999999999999.999", multipleOf = 0.001, requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal estoqueMinimo,
        @NotNull @Positive @Schema(description = "Identificador positivo do gênero de destino", example = "1", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long generoId,
        @Positive @Schema(description = "Identificador positivo opcional; null ou ausência remove a associação", example = "null", types = {"integer", "null"}, format = "int64", requiredMode = Schema.RequiredMode.NOT_REQUIRED, minimum = "1")
        Long fornecedorId) {}
