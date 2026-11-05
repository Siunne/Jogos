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
import br.com.emilly.jogos.domain.Status;
import br.com.emilly.jogos.repository.specification.Filtros;
import org.springframework.data.domain.*;
import java.util.Set;

@Service
public class JogoService {

    private final JogoRepository jogoRepository;
    private final GeneroJogoRepository generoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final BloqueioEstoque bloqueio;

    public JogoService(
            JogoRepository jogoRepository,
            GeneroJogoRepository generoRepository,
            FornecedorRepository fornecedorRepository,
            BloqueioEstoque bloqueio) {

        this.jogoRepository = jogoRepository;
        this.generoRepository = generoRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.bloqueio = bloqueio;
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
    public Jogo alterar(Long id, String nome, BigDecimal preco, BigDecimal estoqueMinimo,
                        Long generoId, Long fornecedorId) {
        var jogo = buscarPorId(id);
        var genero = generoRepository.findById(generoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Gênero de jogo não encontrado"));
        var fornecedor = fornecedorId == null ? null : fornecedorRepository.findById(fornecedorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor não encontrado"));
        jogo.alterarNome(nome);
        jogo.alterarPreco(preco);
        jogo.alterarEstoqueMinimo(estoqueMinimo);
        jogo.alterarGenero(genero);
        jogo.associarFornecedor(fornecedor);
        return jogo;
    }

    @Transactional
    public Jogo alterarStatus(Long id, Status status) {
        var jogo = buscarPorId(id);
        if (status == Status.ATIVO) jogo.ativar(); else jogo.inativar();
        return jogo;
    }

    @Transactional
    public Jogo entrada(Long id, BigDecimal quantidade) {
        var jogo = buscarParaMovimento(id);
        jogo.receberUnidades(quantidade);
        return jogo;
    }

    @Transactional
    public Jogo saida(Long id, BigDecimal quantidade) {
        var jogo = buscarParaMovimento(id);
        jogo.retirarUnidades(quantidade);
        return jogo;
    }

    @Transactional
    public void excluir(Long id) {
        var jogo = buscarPorId(id);
        jogoRepository.delete(jogo);
        jogoRepository.flush();
    }

    @Transactional(readOnly = true)
    public Page<Jogo> pesquisar(String nome, Status status, Long generoId, Long fornecedorId,
                               Boolean abaixoEstoqueMinimo, Pageable pageable) {
        Paginacao.validar(pageable, Set.of("id", "nome", "codigo", "preco", "estoqueMinimo",
                "unidadesDisponiveis", "dataCadastro", "status"));
        return jogoRepository.findAll(Filtros.jogos(nome, status, generoId, fornecedorId,
                abaixoEstoqueMinimo), pageable);
    }

    @Transactional
    public void receberUnidades(
            Long jogoId,
            BigDecimal quantidade) {

        entrada(jogoId, quantidade);
    }

    private Jogo buscarParaMovimento(Long id) {
        var jogo = buscarPorId(id);
        bloqueio.adquirir(jogo);
        // Refresh pode recolocar proxies lazy. A resposta continua utilizável com OSIV=false.
        org.hibernate.Hibernate.initialize(jogo.getGenero());
        org.hibernate.Hibernate.initialize(jogo.getFornecedor());
        return jogo;
    }
}
