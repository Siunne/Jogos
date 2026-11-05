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
public class ColaboradorService {
  private final ColaboradorRepository repository;
  private final PessoaService pessoas;

  public ColaboradorService(ColaboradorRepository repository, PessoaService pessoas) {
    this.repository = repository;
    this.pessoas = pessoas;
  }

  public Colaborador buscar(Long id) {
    return repository
        .findOneById(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Colaborador não encontrado"));
  }

  private void matricula(String m, Long id) {
    String v = Colaborador.normalizarMatricula(m);
    if (id == null ? repository.existsByMatricula(v) : repository.existsByMatriculaAndIdNot(v, id))
      throw new RecursoDuplicadoException("Matrícula já cadastrada");
  }

  @Transactional
  public Colaborador cadastrar(
      Long pessoaId, String matricula, FuncaoColaborador funcao, LocalDate data) {
    var pessoa = pessoas.buscar(pessoaId);
    if (repository.existsByPessoaId(pessoaId))
      throw new RecursoDuplicadoException("Pessoa já possui cadastro de colaborador");
    var c = new Colaborador(pessoa, matricula, funcao, data);
    matricula(c.getMatricula(), null);
    return repository.save(c);
  }

  @Transactional
  public Colaborador alterar(Long id, String m, FuncaoColaborador f) {
    var c = buscar(id);
    matricula(m, id);
    c.alterarDados(m, f);
    return c;
  }

  public Page<Colaborador> pesquisar(
      String nome, Status status, Long pessoaId, FuncaoColaborador funcao, Pageable p) {
    Paginacao.validar(p, Set.of("id", "matricula", "funcao", "dataAdmissao"));
    var filtro = FiltrosComerciais.<Colaborador>papeis(nome, status, pessoaId);
    if (funcao != null) filtro = filtro.and((r, q, c) -> c.equal(r.get("funcao"), funcao));
    return repository.findAll(filtro, p);
  }
}
