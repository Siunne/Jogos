package br.com.emilly.jogos.dto;

public record GeneroJogoResponse(
        Long id,
        String nome,
        br.com.emilly.jogos.domain.Status status
) {
}
