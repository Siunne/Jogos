package br.com.emilly.jogos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Schema(description = "Criação de jogo ativo. A data é definida pelo servidor e não é aceita neste DTO. Fornecedor opcional.")
public record JogoRequest(

        @NotBlank(message = "O código é obrigatório")
        @Schema(description = "Código único escolhido no cadastro; imutável depois da criação", example = "JOGO-ESTRELAS-001", maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
        String codigo,

        @NotBlank(message = "O nome é obrigatório")
        @Schema(description = "Nome do jogo", example = "Reino das Estrelas", maxLength = 150, requiredMode = Schema.RequiredMode.REQUIRED)
        String nome,

        @NotNull(message = "As unidades disponíveis são obrigatórias")
        @PositiveOrZero(message = "As unidades disponíveis não podem ser negativas")
        @Schema(description = "Saldo inicial não negativo; depois muda somente por movimentação", example = "10.000", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal unidadesDisponiveis,

        @NotNull(message = "O preço é obrigatório")
        @PositiveOrZero(message = "O preço não pode ser negativo")
        @Schema(description = "Preço por unidade, não negativo", example = "149.90", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal preco,

        @NotNull(message = "O estoque mínimo é obrigatório")
        @PositiveOrZero(message = "O estoque mínimo não pode ser negativo")
        @Schema(description = "Quantidade mínima usada para indicar reposição", example = "3.000", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal estoqueMinimo,

        @NotNull(message = "O gênero é obrigatório")
        @Schema(description = "Identificador de um gênero existente", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long generoId,

        @Schema(description = "Fornecedor opcional; null ou ausência remove/dispensa a associação", example = "null", types = {"integer", "null"}, format = "int64", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Long fornecedorId

) {
}
