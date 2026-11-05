package br.com.emilly.jogos.dto;

import br.com.emilly.jogos.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "Dados públicos da própria conta; não contém senha ou hash.")
public record ContaAcessoResponse(
    @Schema(description = "ID da conta", example = "1") Long id,
    @Schema(description = "ID da Pessoa", example = "1") Long pessoaId,
    @Schema(description = "Nome da Pessoa", example = "Pessoa Exemplo") String nomePessoa,
    @Schema(description = "Login normalizado", example = "operador.jogos") String login,
    @Schema(description = "Estado da conta") StatusContaAcesso status,
    @Schema(
            description =
                "Perfis de acesso; cadastro inicial somente OPERADOR, sem autorização diferenciada"
                    + " nesta aula")
        Set<PerfilAcesso> perfis) {}
