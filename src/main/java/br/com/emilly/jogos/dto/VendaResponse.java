package br.com.emilly.jogos.dto;

import br.com.emilly.jogos.api.validation.*;
import br.com.emilly.jogos.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Schema(description = "Detalhe com itens e total derivado, não persistido em coluna.")
public record VendaResponse(
    @Schema(description = "ID da venda", example = "1") Long id,
    @Schema(description = "Cliente", example = "1") Long clienteId,
    @Schema(description = "Nome do comprador", example = "Pessoa Exemplo") String clienteNome,
    @Schema(description = "Vendedor", example = "1") Long vendedorId,
    @Schema(description = "Nome do vendedor", example = "Pessoa Exemplo") String vendedorNome,
    @Schema(
            description = "Data/hora local do servidor",
            example = "2026-11-05T12:00:00",
            format = "local-date-time")
        LocalDateTime dataVenda,
    @Schema(description = "ABERTA, FINALIZADA ou CANCELADA", example = "ABERTA") StatusVenda status,
    @Schema(description = "Itens; apenas o detalhe carrega esta coleção")
        List<ItemVendaResponse> itens,
    @Schema(description = "Soma dos subtotais históricos, duas casas", example = "200")
        BigDecimal total) {}
