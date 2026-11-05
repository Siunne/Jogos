package br.com.emilly.jogos.security;

import br.com.emilly.jogos.domain.ContaAcesso;
import br.com.emilly.jogos.repository.ContaAcessoRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContaAcessoUserDetailsService implements UserDetailsService {
  private final ContaAcessoRepository repository;

  public ContaAcessoUserDetailsService(ContaAcessoRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String login) {
    final String normalizado;
    try {
      normalizado = ContaAcesso.normalizarLogin(login);
    } catch (IllegalArgumentException e) {
      throw new UsernameNotFoundException("Login ou senha inválidos");
    }
    var conta =
        repository
            .buscarPorLoginNormalizado(normalizado)
            .orElseThrow(() -> new UsernameNotFoundException("Login ou senha inválidos"));
    return User.withUsername(conta.getLogin())
        .password(conta.getSenhaHash())
        .authorities(conta.getPerfis().stream().map(p -> "ROLE_" + p.name()).toArray(String[]::new))
        .disabled(!conta.podeAutenticar())
        .build();
  }
}
