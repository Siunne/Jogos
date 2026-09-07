package br.com.emilly.jogos.application;

import br.com.emilly.jogos.domain.GeneroJogo;
import br.com.emilly.jogos.domain.Jogo;
import br.com.emilly.jogos.exception.RecursoDuplicadoException;
import br.com.emilly.jogos.exception.RecursoNaoEncontradoException;
import br.com.emilly.jogos.repository.GeneroJogoRepository;
import br.com.emilly.jogos.repository.JogoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class JogoService {

    private final JogoRepository jogoRepository;
    private final GeneroJogoRepository generoRepository;

    public JogoService(
            JogoRepository jogoRepository,
            GeneroJogoRepository generoRepository) {
        this.jogoRepository = jogoRepository;
        this.generoRepository = generoRepository;
    }

    @Transactional
    public Jogo cadastrar(Jogo jogo, Long generoId) {

        if (jogoRepository.existsByCodigo(jogo.getCodigo())) {
            throw new RecursoDuplicadoException(
                    "Código do jogo já cadastrado");
        }

        GeneroJogo genero = generoRepository.findById(generoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Gênero de jogo não encontrado"));

        genero.adicionarJogo(jogo);

        return jogoRepository.save(jogo);
    }

    @Transactional
    public void receberUnidades(Long jogoId, BigDecimal quantidade) {

        Jogo jogo = jogoRepository.findById(jogoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Jogo não encontrado"));

        jogo.receberUnidades(quantidade);
    }
}