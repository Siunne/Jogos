package br.com.emilly.jogos.dto;

import br.com.emilly.jogos.api.validation.*;
import br.com.emilly.jogos.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Schema(description = "Identidade civil; sem credenciais.")
public record PessoaResponse(
    @Schema(description = "Identificador", example = "1") Long id,
    @Schema(description = "Nome", example = "Pessoa Exemplo") String nome,
    @Schema(description = "E-mail normalizado", example = "pessoa@example.com") String email,
    @Schema(description = "CPF para este caso de uso cadastral", example = "52998224725")
        String cpf,
    @Schema(description = "Data gerada pelo servidor", example = "2026-11-05")
        LocalDate dataCadastro,
    @Schema(description = "Estado da pessoa", example = "ATIVO") Status status) {}
