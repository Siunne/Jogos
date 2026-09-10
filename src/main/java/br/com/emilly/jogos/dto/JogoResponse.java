package br.com.emilly.jogos.dto;

import java.math.BigDecimal;

public record JogoResponse(
        Long id,
        String codigo,
        String nome,
        BigDecimal preco,
        BigDecimal estoqueMinimo,
        Long generoId,
        String generoNome,
        Long fornecedorId,
        String fornecedorRazaoSocial
) {
}