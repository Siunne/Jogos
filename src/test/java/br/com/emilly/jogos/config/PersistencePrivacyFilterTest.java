package br.com.emilly.jogos.config;

import static org.junit.jupiter.api.Assertions.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class PersistencePrivacyFilterTest {
  static class TestContext extends LoggerContext implements AutoCloseable {
    @Override
    public void close() {
      stop();
    }
  }

  @Test
  void diagnosticosHibernateNovosEAntigosNaoRepassamDadosPessoais() {
    try (var context = new TestContext()) {
      var filter = new PersistencePrivacyFilter();
      filter.setContext(context);
      filter.start();
      context.addTurboFilter(filter);
      var appender = new ListAppender<ILoggingEvent>();
      appender.start();
      context.getLogger("ROOT").addAppender(appender);
      for (String name :
          new String[] {
            "org.hibernate.orm.jdbc.error", "org.hibernate.engine.jdbc.spi.SqlExceptionHelper"
          }) {
        context.getLogger(name).warn("SQL Error: 0, SQLState: {}", "23505");
        context
            .getLogger(name)
            .error(
                "constraint \"uk_pessoa_cpf\"; rejected row: {}, {}, {}, {}",
                "52998224725",
                "11222333000181",
                "pessoa@example.com",
                "Nome Ficticio");
      }
      assertEquals(4, appender.list.size());
      String output =
          appender.list.stream()
              .map(ILoggingEvent::getFormattedMessage)
              .reduce("", (a, b) -> a + b);
      assertTrue(output.contains("23505"));
      assertTrue(output.contains("uk_pessoa_cpf"));
      for (String secret :
          new String[] {
            "52998224725", "11222333000181", "pessoa@example.com", "Nome Ficticio", "rejected row"
          }) assertFalse(output.contains(secret));
      assertTrue(appender.list.stream().allMatch(e -> e.getThrowableProxy() == null));
    }
  }

  @Test
  void excecaoJdbcEncadeadaPreservaSomenteMetadadosConhecidos() {
    try (var context = new TestContext()) {
      var filter = new PersistencePrivacyFilter();
      filter.start();
      context.addTurboFilter(filter);
      var appender = new ListAppender<ILoggingEvent>();
      appender.start();
      context.getLogger("ROOT").addAppender(appender);
      var cause =
          new SQLException(
              "constraint \"uk_pessoa_email_normalizado\"; email pessoa@example.com; CPF"
                  + " 52998224725",
              "23505");
      context
          .getLogger("aplicacao")
          .error(
              "Falha com dados pessoais",
              new DataIntegrityViolationException("mensagem sensivel", cause));
      assertEquals(1, appender.list.size());
      var event = appender.list.getFirst();
      assertEquals(
          "Falha de persistencia: SQLState=23505, restricao=uk_pessoa_email_normalizado",
          event.getFormattedMessage());
      assertNull(event.getThrowableProxy());
      context
          .getLogger("aplicacao")
          .error("erro", new SQLException("constraint \"cpf_52998224725\"", "23514"));
      assertEquals(
          "Falha de persistencia: SQLState=23514, restricao=nao_identificada",
          appender.list.getLast().getFormattedMessage());
    }
  }

  @Test
  void bloqueiaSqlEBindMesmoEmTraceEPreservaErrosNaoJdbc() {
    try (var context = new TestContext()) {
      var filter = new PersistencePrivacyFilter();
      filter.start();
      context.addTurboFilter(filter);
      var appender = new ListAppender<ILoggingEvent>();
      appender.start();
      context.getLogger("ROOT").setLevel(Level.TRACE);
      context.getLogger("ROOT").addAppender(appender);
      context.getLogger("org.hibernate.SQL").debug("INSERT ... dados pessoais");
      context.getLogger("org.hibernate.orm.jdbc.bind").trace("52998224725");
      context.getLogger("org.hibernate.orm.jdbc.extract").trace("pessoa@example.com");
      assertTrue(appender.list.isEmpty());
      context
          .getLogger("aplicacao")
          .error("Falha tecnica sem dados pessoais", new IllegalStateException("causa"));
      assertEquals(1, appender.list.size());
      assertNotNull(appender.list.getFirst().getThrowableProxy());
    }
  }
}
