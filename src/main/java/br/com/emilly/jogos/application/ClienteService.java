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
public class ClienteService {
  private final ClienteRepository repository;
  private final PessoaService pessoas;

  public ClienteService(ClienteRepository repository, PessoaService pessoas) {
    this.repository = repository;
    this.pessoas = pessoas;
  }

  public Cliente buscar(Long id) {
    return repository
        .findOneById(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
  }

  @Transactional
  public Cliente cadastrar(Long pessoaId, String telefone) {
    var pessoa = pessoas.buscar(pessoaId);
    if (repository.existsByPessoaId(pessoaId))
      throw new RecursoDuplicadoException("Pessoa já possui cadastro de cliente");
    return repository.save(new Cliente(pessoa, telefone));
  }

  @Transactional
  public Cliente alterar(Long id, String telefone) {
    var c = buscar(id);
    c.alterarDados(telefone);
    return c;
  }

  public Page<Cliente> pesquisar(String nome, Status status, Long pessoaId, Pageable p) {
    Paginacao.validar(p, Set.of("id", "telefone"));
    return repository.findAll(FiltrosComerciais.papeis(nome, status, pessoaId), p);
  }
}
