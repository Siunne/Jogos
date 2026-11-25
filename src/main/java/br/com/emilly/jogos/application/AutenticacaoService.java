package br.com.emilly.jogos.application;

import br.com.emilly.jogos.dto.TokenResponse;
import br.com.emilly.jogos.exception.CredenciaisInvalidasException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService {
  private final AuthenticationManager manager;
  private final TokenService tokens;

  public AutenticacaoService(AuthenticationManager manager, TokenService tokens) {
    this.manager = manager;
    this.tokens = tokens;
  }

  public TokenResponse autenticar(String login, String senha) {
    try {
      var auth =
          manager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(login, senha));
      return tokens.emitir(auth);
    } catch (AuthenticationException invalid) {
      throw new CredenciaisInvalidasException();
    }
  }
}
