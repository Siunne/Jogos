package br.com.emilly.jogos.application;

import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.security.initial-admin", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(AdministradorInicialProperties.class)
class AdministradorInicialRunner implements ApplicationRunner {
  private final AdministradorInicialService service;
  private final AdministradorInicialProperties properties;

  AdministradorInicialRunner(AdministradorInicialService service,
      AdministradorInicialProperties properties) {
    this.service = service;
    this.properties = properties;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (service.criarSeNecessario(properties))
      LoggerFactory.getLogger(getClass()).info("Administrador inicial provisionado");
  }
}
