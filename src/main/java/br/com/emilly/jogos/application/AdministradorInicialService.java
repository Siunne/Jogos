package br.com.emilly.jogos.application;

import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.exception.RecursoDuplicadoException;
import br.com.emilly.jogos.repository.ContaAcessoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AdministradorInicialService {
  private final AdministracaoContas administracao;
  private final ContaAcessoRepository contas;
  private final PessoaService pessoas;
  private final PasswordEncoder encoder;

  AdministradorInicialService(AdministracaoContas administracao, ContaAcessoRepository contas,
      PessoaService pessoas, PasswordEncoder encoder) {
    this.administracao = administracao;
    this.contas = contas;
    this.pessoas = pessoas;
    this.encoder = encoder;
  }

  @Transactional
  public boolean criarSeNecessario(AdministradorInicialProperties cfg) {
    if (!cfg.enabled()) return false;
    administracao.serializar();
    if (administracao.ativos() > 0) return false;

    // A validação de domínio não inclui valores fornecidos nas mensagens de falha.
    var login = ContaAcesso.normalizarLogin(cfg.login());
    PoliticaSenha.validar(cfg.password());
    if (contas.existeLogin(login)) throw new RecursoDuplicadoException("Login já cadastrado");
    var pessoa = pessoas.cadastrar(cfg.name(), cfg.email(), cfg.cpf());
    var conta = new ContaAcesso(pessoa, login, encoder.encode(cfg.password()));
    conta.concederPerfil(PerfilAcesso.ADMINISTRADOR);
    contas.saveAndFlush(conta);
    return true;
  }
}
