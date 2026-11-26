package br.com.emilly.jogos.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.security.initial-admin")
record AdministradorInicialProperties(boolean enabled, String name, String email, String cpf,
    String login, String password) {
  @Override
  public String toString() {
    return "AdministradorInicialProperties[enabled=" + enabled + "]";
  }
}
