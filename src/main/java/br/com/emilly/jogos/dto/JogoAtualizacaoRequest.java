package br.com.emilly.jogos.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record JogoAtualizacaoRequest(
        @NotBlank @Size(max = 150) String nome,
        @NotNull @PositiveOrZero @Digits(integer = 16, fraction = 2) BigDecimal preco,
        @NotNull @PositiveOrZero @Digits(integer = 15, fraction = 3) BigDecimal estoqueMinimo,
        @NotNull @Positive Long generoId,
        @Positive Long fornecedorId) {}
