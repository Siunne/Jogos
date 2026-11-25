package br.com.emilly.jogos.exception;

public class CredenciaisInvalidasException extends RuntimeException {
  public CredenciaisInvalidasException() {
    super("Login ou senha inválidos");
  }
}
