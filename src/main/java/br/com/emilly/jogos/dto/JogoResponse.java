package br.com.emilly.jogos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Representação completa de jogo, incluindo campos protegidos e associações.")
public record JogoResponse(
        @Schema(description = "Identificador gerado pelo servidor", example = "1", accessMode = Schema.AccessMode.READ_ONLY, requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,
        @Schema(description = "Código único, imutável depois do cadastro", example = "JOGO-ESTRELAS-001", requiredMode = Schema.RequiredMode.REQUIRED)
        String codigo,
        @Schema(description = "Nome comercial do jogo", example = "Reino das Estrelas", requiredMode = Schema.RequiredMode.REQUIRED)
        String nome,
        @Schema(description = "Preço por unidade, não negativo", example = "149.90", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal preco,
        @Schema(description = "Quantidade mínima usada para indicar reposição", example = "3.000", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal estoqueMinimo,
        @Schema(description = "Identificador de um gênero existente", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long generoId,
        @Schema(description = "Nome do gênero associado", example = "Aventura", requiredMode = Schema.RequiredMode.REQUIRED)
        String generoNome,
        @Schema(description = "Fornecedor opcional; null ou ausência remove/dispensa a associação", example = "1", types = {"integer", "null"}, format = "int64", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Long fornecedorId,
        @Schema(description = "Razão social opcional; null quando não há fornecedor", example = "Distribuidora Exemplo de Jogos", types = {"string", "null"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String fornecedorRazaoSocial,
        @Schema(description = "Saldo atual; modificado somente por entrada ou saída", example = "10.000", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal unidadesDisponiveis,
        @Schema(description = "Data local gerada pelo servidor; preservada em todas as atualizações", example = "2026-11-04", type = "string", format = "date", accessMode = Schema.AccessMode.READ_ONLY, requiredMode = Schema.RequiredMode.REQUIRED)
        java.time.LocalDate dataCadastro,
        @Schema(description = "Situação do cadastro; alterada somente pela operação de status", example = "ATIVO", allowableValues = {"ATIVO", "INATIVO"}, requiredMode = Schema.RequiredMode.REQUIRED)
        br.com.emilly.jogos.domain.Status status
) {
}
