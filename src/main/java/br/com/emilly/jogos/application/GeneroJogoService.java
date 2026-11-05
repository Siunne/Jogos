package br.com.emilly.jogos.application;

import br.com.emilly.jogos.domain.GeneroJogo;
import br.com.emilly.jogos.domain.Status;
import br.com.emilly.jogos.exception.RecursoDuplicadoException;
import br.com.emilly.jogos.exception.RecursoEmUsoException;
import br.com.emilly.jogos.exception.RecursoNaoEncontradoException;
import br.com.emilly.jogos.repository.GeneroJogoRepository;
import br.com.emilly.jogos.repository.JogoRepository;
import br.com.emilly.jogos.repository.specification.Filtros;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class GeneroJogoService {

    private final GeneroJogoRepository repository;
    private final JogoRepository jogos;

    public GeneroJogoService(GeneroJogoRepository repository, JogoRepository jogos) {
        this.repository = repository;
        this.jogos = jogos;
    }

    @Transactional
    public GeneroJogo cadastrar(String nome) {
        var genero = new GeneroJogo(nome);
        nome = genero.getNome();
        if (repository.existeNomeNormalizado(nome, null)) {
            throw new RecursoDuplicadoException(
                    "Nome do gênero já cadastrado");
        }

        return repository.save(genero);
    }

    @Transactional(readOnly = true)
    public GeneroJogo buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Gênero de jogo não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<GeneroJogo> listar() {
        return repository.findAll();
    }

    @Transactional
    public GeneroJogo alterar(Long id, String nome) {
        var genero = buscarPorId(id);
        String normalizado = new GeneroJogo(nome).getNome();
        if (repository.existeNomeNormalizado(normalizado, id)) {
            throw new RecursoDuplicadoException("Nome do gênero já cadastrado");
        }
        genero.alterarNome(normalizado);
        repository.flush();
        return genero;
    }

    @Transactional
    public GeneroJogo alterarStatus(Long id, Status status) {
        var genero = buscarPorId(id);
        if (status == Status.ATIVO) genero.ativar(); else genero.inativar();
        return genero;
    }

    @Transactional
    public void excluir(Long id) {
        var genero = buscarPorId(id);
        if (jogos.existsByGeneroId(id)) {
            throw new RecursoEmUsoException("Gênero não pode ser excluído porque possui jogos");
        }
        repository.delete(genero);
        repository.flush();
    }

    @Transactional(readOnly = true)
    public Page<GeneroJogo> pesquisar(String nome, Status status, Pageable pageable) {
        Paginacao.validar(pageable, Set.of("id", "nome", "status"));
        return repository.findAll(Filtros.<GeneroJogo>nomeEStatus(nome, status), pageable);
    }
}
