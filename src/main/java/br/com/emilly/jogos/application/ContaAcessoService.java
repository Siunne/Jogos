package br.com.emilly.jogos.application;

import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.dto.ContaAcessoResponse;
import br.com.emilly.jogos.exception.*;
import br.com.emilly.jogos.mapper.ContaAcessoMapper;
import br.com.emilly.jogos.repository.ContaAcessoRepository;
import java.util.Set;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ContaAcessoService {
  private final ContaAcessoRepository repository;
  private final PessoaService pessoas;
  private final PasswordEncoder encoder;
  private final ContaAcessoMapper mapper;
  private final AdministracaoContas administracao;

  public ContaAcessoService(
      ContaAcessoRepository repository,
      PessoaService pessoas,
      PasswordEncoder encoder,
      ContaAcessoMapper mapper, AdministracaoContas administracao) {
    this.repository = repository;
    this.pessoas = pessoas;
    this.encoder = encoder;
    this.mapper = mapper;
    this.administracao = administracao;
  }

  @Transactional
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ContaAcessoResponse cadastrar(Long pessoaId, String login, String senha) {
    administracao.serializar();
    var normalizado = ContaAcesso.normalizarLogin(login);
    PoliticaSenha.validar(senha);
    if (repository.existeLogin(normalizado))
      throw new RecursoDuplicadoException("Login já cadastrado");
    if (repository.existsByPessoaId(pessoaId))
      throw new RecursoDuplicadoException("Pessoa já possui conta de acesso");
    var conta = new ContaAcesso(pessoas.buscar(pessoaId), normalizado, encoder.encode(senha));
    return mapper.toResponse(repository.saveAndFlush(conta));
  }

  @Transactional
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ContaAcessoResponse alterarPerfis(Long id, Set<PerfilAcesso> perfis) {
    administracao.serializar();
    var conta = buscar(id);
    if (perfis == null || !perfis.contains(PerfilAcesso.ADMINISTRADOR))
      administracao.exigirOutraConta(conta);
    conta.substituirPerfis(perfis);
    return mapper.toResponse(conta);
  }

  @Transactional
  @PreAuthorize("hasRole('ADMINISTRADOR')")
  public ContaAcessoResponse alterarStatus(Long id, StatusContaAcesso status) {
    if (status == null) throw new IllegalArgumentException("Status obrigatório");
    administracao.serializar();
    var conta = buscar(id);
    if (status == StatusContaAcesso.BLOQUEADA) {
      administracao.exigirOutraConta(conta);
      conta.bloquear();
    } else {
      conta.ativar();
    }
    return mapper.toResponse(conta);
  }

  private ContaAcesso buscar(Long id) {
    if (id == null || id <= 0) throw new IllegalArgumentException("Id inválido");
    return repository.findById(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada"));
  }

  private ContaAcesso buscar(String login) {
    return repository
        .buscarPorLoginNormalizado(ContaAcesso.normalizarLogin(login))
        .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada"));
  }

  public ContaAcessoResponse propria(String login) {
    return mapper.toResponse(buscar(login));
  }

  @Transactional
  public void alterarSenha(String login, String atual, String nova) {
    administracao.serializar();
    PoliticaSenha.validar(atual);
    PoliticaSenha.validar(nova);
    var conta = buscar(login);
    if (!encoder.matches(atual, conta.getSenhaHash()))
      throw new IllegalArgumentException("Senha atual inválida");
    if (encoder.matches(nova, conta.getSenhaHash()))
      throw new IllegalArgumentException("Nova senha deve ser diferente");
    conta.alterarSenha(encoder.encode(nova));
  }
}
