package br.com.emilly.jogos.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import java.sql.SQLException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

/** Nunca repassa SQL, argumentos, linhas rejeitadas ou stack traces JDBC aos appenders. */
public class PersistencePrivacyFilter extends TurboFilter {
  public static final String SAFE_LOGGER = "br.com.emilly.jogos.persistence.diagnostic";
  private static final Pattern STATE = Pattern.compile("SQLState[:=]\\s*([0-9A-Z]{5})\\b");
  // Lista fechada: nomes desconhecidos podem conter dados e não são registrados.
  private static final Set<String> CONSTRAINTS =
      Set.of(
          "uk_pessoa_cpf",
          "uk_pessoa_email_normalizado",
          "uk_cliente_pessoa_id",
          "uk_colaborador_pessoa_id",
          "uk_colaborador_matricula",
          "uk_item_venda_venda_id_jogo_id",
          "uk_fornecedor_cnpj",
          "uk_jogo_codigo",
          "uk_genero_jogo_nome_lower",
          "ck_pessoa_cpf_formato",
          "ck_pessoa_email_normalizado",
          "ck_pessoa_status",
          "ck_colaborador_funcao",
          "ck_venda_status",
          "ck_item_venda_quantidade",
          "ck_item_venda_preco",
          "ck_genero_jogo_status",
          "ck_jogo_status",
          "ck_jogo_preco",
          "ck_jogo_unidades_disponiveis",
          "ck_jogo_estoque_minimo_nao_negativo",
          "ck_fornecedor_status",
          "fk_cliente_pessoa_id",
          "fk_colaborador_pessoa_id",
          "fk_venda_cliente_id",
          "fk_venda_vendedor_id",
          "fk_item_venda_venda_id",
          "fk_item_venda_jogo_id",
          "fk_jogo_fornecedor",
          "fk_jogo_genero_jogo");

  @Override
  public FilterReply decide(
      Marker marker,
      Logger logger,
      Level level,
      String format,
      Object[] params,
      Throwable throwable) {
    if (logger.getName().equals(SAFE_LOGGER)) return FilterReply.NEUTRAL;
    String name = logger.getName();
    // Não permitir que uma configuração DEBUG/TRACE exponha parâmetros ou SQL literal.
    if (name.equals("org.hibernate.SQL")
        || name.startsWith("org.hibernate.orm.jdbc.bind")
        || name.startsWith("org.hibernate.orm.jdbc.extract")
        || name.startsWith("org.hibernate.type.descriptor.sql.BasicBinder")
        || name.startsWith("org.hibernate.type.descriptor.sql.BasicExtractor"))
      return FilterReply.DENY;
    // Logback também consulta o filtro em isWarnEnabled/isErrorEnabled, antes da mensagem existir.
    if (format == null && params == null && throwable == null) return FilterReply.NEUTRAL;
    Throwable failure =
        throwable != null ? throwable : MessageFormatter.getThrowableCandidate(params);
    var tuple = MessageFormatter.arrayFormat(format, params, failure);
    SQLException sql = sqlException(failure);
    boolean jdbcDiagnostic =
        name.startsWith("org.hibernate.orm.jdbc.error")
            || name.equals("org.hibernate.engine.jdbc.spi.SqlExceptionHelper");
    if (!jdbcDiagnostic && sql == null) return FilterReply.NEUTRAL;
    if (level.isGreaterOrEqual(Level.WARN)) {
      String detail = tuple.getMessage() == null ? "" : tuple.getMessage();
      if (sql != null && sql.getMessage() != null) detail += " " + sql.getMessage();
      String state = sql == null ? null : sql.getSQLState();
      if (state == null || !state.matches("[0-9A-Z]{5}")) {
        var match = STATE.matcher(detail);
        state = match.find() ? match.group(1) : "indisponivel";
      }
      String constraint =
          CONSTRAINTS.stream()
              .filter(
                  c ->
                      Pattern.compile("(?<![A-Za-z0-9_])" + c + "(?![A-Za-z0-9_])")
                              .matcher(tuple.getMessage() == null ? "" : tuple.getMessage())
                              .find()
                          || (sql != null
                              && sql.getMessage() != null
                              && sql.getMessage().contains("\"" + c + "\"")))
              .sorted()
              .findFirst()
              .orElse("nao_identificada");
      // Usa o próprio contexto, sem repassar argumentos ou throwable do evento original.
      logger
          .getLoggerContext()
          .getLogger(SAFE_LOGGER)
          .warn("Falha de persistencia: SQLState={}, restricao={}", state, constraint);
    }
    return FilterReply.DENY;
  }

  private SQLException sqlException(Throwable error) {
    Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
    while (error != null && visited.add(error)) {
      if (error instanceof SQLException sql) return sql;
      error = error.getCause();
    }
    return null;
  }
}
