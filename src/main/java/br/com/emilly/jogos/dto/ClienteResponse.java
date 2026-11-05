package br.com.emilly.jogos.dto;

import br.com.emilly.jogos.api.validation.*;
import br.com.emilly.jogos.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Schema(description = "Papel comercial de cliente, referenciando Pessoa.")
public record ClienteResponse(
    @Schema(description = "ID do cliente", example = "1") Long id,
    @Schema(description = "Identidade central", example = "1") Long pessoaId,
    @Schema(description = "Nome da pessoa", example = "Pessoa Exemplo") String nome,
    @Schema(description = "Estado da pessoa", example = "ATIVO") Status status,
    @Schema(description = "Contato opcional", example = "(11) 99999-0000") String telefone) {}
