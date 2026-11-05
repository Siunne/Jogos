package br.com.emilly.jogos.dto;

import br.com.emilly.jogos.api.validation.*;
import br.com.emilly.jogos.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Schema(description = "Abre venda sem baixar estoque; participantes devem estar aptos.")
public record VendaRequest(
    @NotNull
        @Positive
        @Schema(
            description = "Cliente ativo",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
        Long clienteId,
    @NotNull
        @Positive
        @Schema(
            description = "Colaborador VENDEDOR/GERENTE ativo",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
        Long vendedorId) {}
