package br.com.emilly.jogos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record JogoRequest(

        @NotBlank(message = "O código é obrigatório")
        String codigo,

        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotNull(message = "As unidades disponíveis são obrigatórias")
        @PositiveOrZero(message = "As unidades disponíveis não podem ser negativas")
        BigDecimal unidadesDisponiveis,

        @NotNull(message = "O preço é obrigatório")
        @PositiveOrZero(message = "O preço não pode ser negativo")
        BigDecimal preco,

        @NotNull(message = "O estoque mínimo é obrigatório")
        @PositiveOrZero(message = "O estoque mínimo não pode ser negativo")
        BigDecimal estoqueMinimo,

        @NotNull(message = "A data de cadastro é obrigatória")
        LocalDate dataCadastro,

        @NotNull(message = "O gênero é obrigatório")
        Long generoId,

        Long fornecedorId

) {
}