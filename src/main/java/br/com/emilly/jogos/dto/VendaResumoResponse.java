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
        "Página de vendas sem coleção nem total calculado; evita fetch de itens na paginação.")
public record VendaResumoResponse(
    @Schema(description = "ID da venda", example = "1") Long id,
    @Schema(description = "Cliente", example = "1") Long clienteId,
    @Schema(description = "Comprador", example = "Pessoa Exemplo") String clienteNome,
    @Schema(description = "Vendedor", example = "1") Long vendedorId,
    @Schema(description = "Vendedor", example = "Pessoa Exemplo") String vendedorNome,
    @Schema(
            description = "Data/hora da venda",
            example = "2026-11-05T12:00:00",
            format = "local-date-time")
        LocalDateTime dataVenda,
    @Schema(description = "Estado da venda", example = "ABERTA") StatusVenda status) {}
