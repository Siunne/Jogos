package br.com.emilly.jogos.application;

import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.exception.*;
import br.com.emilly.jogos.repository.*;
import br.com.emilly.jogos.repository.specification.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PessoaService {
  private final PessoaRepository repository;

  public PessoaService(PessoaRepository repository) {
    this.repository = repository;
  }

  public Pessoa buscar(Long id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada"));
  }

  @Transactional
  public Pessoa cadastrar(String nome, String email, String cpf) {
    var p = new Pessoa(nome, email, cpf);
    validar(p, null);
    return repository.save(p);
  }

  @Transactional
  public Pessoa alterar(Long id, String nome, String email, String cpf) {
    var p = buscar(id);
    var candidato = new Pessoa(nome, email, cpf);
    validar(candidato, id);
    p.alterarDados(nome, email, cpf);
    return p;
  }

  private void validar(Pessoa p, Long id) {
    boolean cpf =
        id == null
            ? repository.existsByCpf(p.getCpf())
            : repository.existsByCpfAndIdNot(p.getCpf(), id);
    boolean email =
        id == null
            ? repository.existsByEmailIgnoreCase(p.getEmail())
            : repository.existsByEmailIgnoreCaseAndIdNot(p.getEmail(), id);
    if (cpf) throw new RecursoDuplicadoException("CPF já cadastrado");
    if (email) throw new RecursoDuplicadoException("E-mail já cadastrado");
  }

  @Transactional
  public Pessoa status(Long id, Status status) {
    var p = buscar(id);
    if (status == null) throw new IllegalArgumentException("Status obrigatório");
    if (status == Status.ATIVO) p.ativar();
    else p.inativar();
    return p;
  }

  public Page<Pessoa> pesquisar(String nome, Status status, Pageable pageable) {
    Paginacao.validar(pageable, Set.of("id", "nome", "email", "cpf", "dataCadastro", "status"));
    return repository.findAll(Filtros.nomeEStatus(nome, status), pageable);
  }
}
