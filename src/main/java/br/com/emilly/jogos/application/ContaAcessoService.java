package br.com.emilly.jogos.application;

import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.dto.ContaAcessoResponse;
import br.com.emilly.jogos.exception.*;
import br.com.emilly.jogos.mapper.ContaAcessoMapper;
import br.com.emilly.jogos.repository.ContaAcessoRepository;
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

  public ContaAcessoService(
      ContaAcessoRepository repository,
      PessoaService pessoas,
      PasswordEncoder encoder,
      ContaAcessoMapper mapper) {
    this.repository = repository;
    this.pessoas = pessoas;
    this.encoder = encoder;
    this.mapper = mapper;
  }

  @Transactional
  public ContaAcessoResponse cadastrar(Long pessoaId, String login, String senha) {
    var normalizado = ContaAcesso.normalizarLogin(login);
    PoliticaSenha.validar(senha);
    if (repository.existeLogin(normalizado))
      throw new RecursoDuplicadoException("Login já cadastrado");
    if (repository.existsByPessoaId(pessoaId))
      throw new RecursoDuplicadoException("Pessoa já possui conta de acesso");
    var conta = new ContaAcesso(pessoas.buscar(pessoaId), normalizado, encoder.encode(senha));
    return mapper.toResponse(repository.saveAndFlush(conta));
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
