package br.com.emilly.jogos.application;

import br.com.emilly.jogos.domain.Fornecedor;
import br.com.emilly.jogos.domain.GeneroJogo;
import br.com.emilly.jogos.domain.Jogo;
import br.com.emilly.jogos.exception.RecursoDuplicadoException;
import br.com.emilly.jogos.exception.RecursoNaoEncontradoException;
import br.com.emilly.jogos.repository.FornecedorRepository;
import br.com.emilly.jogos.repository.GeneroJogoRepository;
import br.com.emilly.jogos.repository.JogoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class JogoService {

    private final JogoRepository jogoRepository;
    private final GeneroJogoRepository generoRepository;
    private final FornecedorRepository fornecedorRepository;

    public JogoService(
            JogoRepository jogoRepository,
            GeneroJogoRepository generoRepository,
            FornecedorRepository fornecedorRepository) {

        this.jogoRepository = jogoRepository;
        this.generoRepository = generoRepository;
        this.fornecedorRepository = fornecedorRepository;
    }

    @Transactional
    public Jogo cadastrar(Jogo jogo, Long generoId) {
        return cadastrar(jogo, generoId, null);
    }

    @Transactional
    public Jogo cadastrar(
            Jogo jogo,
            Long generoId,
            Long fornecedorId) {

        if (jogoRepository.existsByCodigo(jogo.getCodigo())) {
            throw new RecursoDuplicadoException(
                    "Código do jogo já cadastrado");
        }

        GeneroJogo genero = generoRepository.findById(generoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Gênero de jogo não encontrado"));

        genero.adicionarJogo(jogo);

        if (fornecedorId != null) {
            Fornecedor fornecedor = fornecedorRepository
                    .findById(fornecedorId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Fornecedor não encontrado"));

            jogo.associarFornecedor(fornecedor);
        }

        return jogoRepository.save(jogo);
    }

    @Transactional(readOnly = true)
    public Jogo buscarPorId(Long id) {
        return jogoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Jogo não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Jogo> listar() {
        return jogoRepository.findAll();
    }

    @Transactional
    public void receberUnidades(
            Long jogoId,
            BigDecimal quantidade) {

        Jogo jogo = jogoRepository.findById(jogoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Jogo não encontrado"));

        jogo.receberUnidades(quantidade);
    }
}