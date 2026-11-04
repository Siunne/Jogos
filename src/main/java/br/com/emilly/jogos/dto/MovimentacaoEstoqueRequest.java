package br.com.emilly.jogos.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record MovimentacaoEstoqueRequest(
        @NotNull @Positive @Digits(integer = 15, fraction = 3) BigDecimal quantidade) {}
