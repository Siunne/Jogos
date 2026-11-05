package br.com.emilly.jogos.dto;

import br.com.emilly.jogos.api.validation.*;
import br.com.emilly.jogos.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Schema(description = "Snapshot de preço; valor atual do jogo não altera este item.")
public record ItemVendaResponse(
    @Schema(description = "ID do item", example = "1") Long id,
    @Schema(description = "Jogo vendido", example = "1") Long jogoId,
    @Schema(description = "Nome do jogo", example = "Reino das Estrelas") String jogoNome,
    @Schema(description = "Quantidade", example = "2") BigDecimal quantidade,
    @Schema(description = "Preço histórico copiado na inclusão", example = "100")
        BigDecimal valorUnitario,
    @Schema(description = "Quantidade x preço histórico, HALF_UP em 2 casas", example = "200")
        BigDecimal subtotal) {}
