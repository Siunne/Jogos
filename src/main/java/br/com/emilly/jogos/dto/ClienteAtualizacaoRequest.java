package br.com.emilly.jogos.dto;

import br.com.emilly.jogos.api.validation.*;
import br.com.emilly.jogos.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Schema(description = "Atualiza somente o telefone; pessoa permanece inalterada.")
public record ClienteAtualizacaoRequest(
    @Size(max = 30)
        @Schema(description = "Telefone opcional, até 30 caracteres", example = "(11) 98888-0000")
        String telefone) {}
