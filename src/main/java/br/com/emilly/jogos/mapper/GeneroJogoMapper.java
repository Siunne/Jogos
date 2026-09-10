package br.com.emilly.jogos.mapper;

import br.com.emilly.jogos.domain.GeneroJogo;
import br.com.emilly.jogos.dto.GeneroJogoRequest;
import br.com.emilly.jogos.dto.GeneroJogoResponse;
import org.springframework.stereotype.Component;

@Component
public class GeneroJogoMapper {

    public GeneroJogo toEntity(GeneroJogoRequest request) {
        return new GeneroJogo(request.nome());
    }

    public GeneroJogoResponse toResponse(GeneroJogo genero) {
        return new GeneroJogoResponse(
                genero.getId(),
                genero.getNome()
        );
    }
}