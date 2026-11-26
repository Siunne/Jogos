package br.com.emilly.jogos.application;

import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.repository.ContaAcessoRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
class AdministracaoContas {
  private final JdbcTemplate jdbc;
  private final ContaAcessoRepository contas;

  AdministracaoContas(JdbcTemplate jdbc, ContaAcessoRepository contas) {
    this.jdbc = jdbc;
    this.contas = contas;
  }

  void serializar() {
    // Lock de transação do PostgreSQL, compartilhado pelos caminhos que alteram a disponibilidade.
    jdbc.execute("select pg_advisory_xact_lock(741402, 1)");
  }

  long ativos() {
    return contas.contarAdministradores(StatusContaAcesso.ATIVA, Status.ATIVO, PerfilAcesso.ADMINISTRADOR);
  }

  void exigirOutraConta(ContaAcesso alvo) {
    if (!alvo.getPerfis().contains(PerfilAcesso.ADMINISTRADOR)) return;
    var autenticacao = SecurityContextHolder.getContext().getAuthentication();
    if (autenticacao != null && alvo.getLogin().equals(autenticacao.getName()))
      throw new IllegalArgumentException("O administrador deve preservar o próprio acesso");
    if (alvo.podeAutenticar() && ativos() <= 1)
      throw new IllegalArgumentException("O último administrador ativo deve permanecer disponível");
  }
}
