package br.com.emilly.jogos.mapper;

import br.com.emilly.jogos.domain.Jogo;
import br.com.emilly.jogos.dto.JogoRequest;
import br.com.emilly.jogos.dto.JogoResponse;
import org.springframework.stereotype.Component;

@Component
public class JogoMapper {

    public Jogo toEntity(JogoRequest request) {
        return new Jogo(
                request.codigo(),
                request.nome(),
                request.unidadesDisponiveis(),
                request.preco(),
                request.estoqueMinimo(),
                request.dataCadastro()
        );
    }

    public JogoResponse toResponse(Jogo jogo) {
        return new JogoResponse(
                jogo.getId(),
                jogo.getCodigo(),
                jogo.getNome(),
                jogo.getPreco(),
                jogo.getEstoqueMinimo(),
                jogo.getGenero().getId(),
                jogo.getGenero().getNome(),
                jogo.getFornecedor() != null
                        ? jogo.getFornecedor().getId()
                        : null,
                jogo.getFornecedor() != null
                        ? jogo.getFornecedor().getRazaoSocial()
                        : null
        );
    }
}