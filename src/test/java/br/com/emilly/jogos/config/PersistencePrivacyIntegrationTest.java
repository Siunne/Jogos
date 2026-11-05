package br.com.emilly.jogos.config;

import static org.junit.jupiter.api.Assertions.*;

import br.com.emilly.jogos.domain.Pessoa;
import br.com.emilly.jogos.repository.PessoaRepository;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
class PersistencePrivacyIntegrationTest {
  @Autowired PessoaRepository pessoas;
  @Autowired br.com.emilly.jogos.repository.FornecedorRepository fornecedores;
  @Autowired PlatformTransactionManager manager;

  @Test
  void violacaoRealNoPostgresNaoExpoePessoaNemDocumentoNosLogs() {
    var context = (LoggerContext) LoggerFactory.getILoggerFactory();
    assertTrue(
        context.getTurboFilterList().stream().anyMatch(PersistencePrivacyFilter.class::isInstance));
    var root = context.getLogger("ROOT");
    var capture = new ListAppender<ILoggingEvent>();
    capture.start();
    root.addAppender(capture);
    String email = UUID.randomUUID() + "@example.com";
    String cpf1 = br.com.emilly.jogos.support.DocumentosTeste.cpf();
    String cpf2 = br.com.emilly.jogos.support.DocumentosTeste.cpf();
    try {
      assertThrows(
          DataIntegrityViolationException.class,
          () ->
              new TransactionTemplate(manager)
                  .execute(
                      status -> {
                        pessoas.saveAndFlush(
                            new Pessoa("Pessoa Ficticia Privacidade", email, cpf1));
                        pessoas.saveAndFlush(new Pessoa("Outra Pessoa Ficticia", email, cpf2));
                        return null;
                      }));
      String messages =
          capture.list.stream().map(ILoggingEvent::getFormattedMessage).reduce("", (a, b) -> a + b);
      assertTrue(messages.contains("23505"));
      assertTrue(messages.contains("uk_pessoa_email_normalizado"));
      for (String personal :
          new String[] {email, cpf1, cpf2, "Pessoa Ficticia Privacidade", "Outra Pessoa Ficticia"})
        assertFalse(messages.contains(personal));
      assertTrue(capture.list.stream().allMatch(e -> e.getThrowableProxy() == null));
    } finally {
      root.detachAppender(capture);
      capture.stop();
    }
  }

  @Test
  void violacaoRealDeCnpjNaoExpoeDocumentoNemRazaoSocial() {
    var root = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("ROOT");
    var capture = new ListAppender<ILoggingEvent>();
    capture.start();
    root.addAppender(capture);
    String cnpj = br.com.emilly.jogos.support.DocumentosTeste.cnpj();
    String name = "Fornecedor Privacidade " + UUID.randomUUID();
    try {
      assertThrows(
          DataIntegrityViolationException.class,
          () ->
              new TransactionTemplate(manager)
                  .execute(
                      status -> {
                        fornecedores.saveAndFlush(
                            new br.com.emilly.jogos.domain.Fornecedor(name, cnpj));
                        fornecedores.saveAndFlush(
                            new br.com.emilly.jogos.domain.Fornecedor(name, cnpj));
                        return null;
                      }));
      String messages =
          capture.list.stream().map(ILoggingEvent::getFormattedMessage).reduce("", (a, b) -> a + b);
      assertTrue(messages.contains("23505"));
      assertTrue(messages.contains("uk_fornecedor_cnpj"));
      assertFalse(messages.contains(cnpj));
      assertFalse(messages.contains(name));
      assertTrue(capture.list.stream().allMatch(e -> e.getThrowableProxy() == null));
    } finally {
      root.detachAppender(capture);
      capture.stop();
    }
  }
}
