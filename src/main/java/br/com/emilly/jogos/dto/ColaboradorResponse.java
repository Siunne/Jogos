package br.com.emilly.jogos.dto;

import br.com.emilly.jogos.api.validation.*;
import br.com.emilly.jogos.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Schema(description = "Vínculo profissional, sem perfil de acesso.")
public record ColaboradorResponse(
    @Schema(description = "ID do colaborador", example = "1") Long id,
    @Schema(description = "Identidade central", example = "1") Long pessoaId,
    @Schema(description = "Nome da pessoa", example = "Pessoa Exemplo") String nome,
    @Schema(description = "Estado da pessoa", example = "ATIVO") Status status,
    @Schema(description = "Matrícula única", example = "VEN-001") String matricula,
    @Schema(description = "Função profissional", example = "VENDEDOR") FuncaoColaborador funcao,
    @Schema(description = "Data do vínculo", example = "2026-11-05") LocalDate dataAdmissao) {}
