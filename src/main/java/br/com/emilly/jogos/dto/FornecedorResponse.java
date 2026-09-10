package br.com.emilly.jogos.dto;

public record FornecedorResponse(
        Long id,
        String razaoSocial,
        String cnpj,
        String status
) {
}