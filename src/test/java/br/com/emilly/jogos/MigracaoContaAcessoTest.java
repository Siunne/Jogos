package br.com.emilly.jogos;

import static org.junit.jupiter.api.Assertions.*;

import br.com.emilly.jogos.support.DocumentosTeste;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import liquibase.*;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MigracaoContaAcessoTest {
  @Autowired DataSource source;
  @Autowired PasswordEncoder encoder;

  Liquibase lb(Connection c, String schema, String file) throws Exception {
    var db = new PostgresDatabase();
    db.setConnection(new JdbcConnection(c));
    db.setDefaultSchemaName(schema);
    db.setLiquibaseSchemaName(schema);
    return new Liquibase(file, new ClassLoaderResourceAccessor(), db);
  }

  long scalar(Statement s, String sql) throws Exception {
    try (var r = s.executeQuery(sql)) {
      assertTrue(r.next());
      return r.getLong(1);
    }
  }

  void falha(Statement s, String sql, String state, String constraint) {
    var e = assertThrows(org.postgresql.util.PSQLException.class, () -> s.execute(sql));
    assertEquals(state, e.getSQLState());
    assertEquals(constraint, e.getServerErrorMessage().getConstraint());
  }

  long pessoa(Connection c) throws Exception {
    try (var p =
        c.prepareStatement(
            "INSERT INTO pessoa(nome,email,cpf,data_cadastro,status)"
                + " VALUES('Fictícia',?,?,current_date,'ATIVO') RETURNING id")) {
      p.setString(1, UUID.randomUUID() + "@example.invalid");
      p.setString(2, DocumentosTeste.cpf());
      try (var r = p.executeQuery()) {
        r.next();
        return r.getLong(1);
      }
    }
  }

  void verificar(boolean incremental) throws Exception {
    String schema = "aula12_" + UUID.randomUUID().toString().replace("-", "");
    try (var c = source.getConnection();
        var s = c.createStatement()) {
      assertEquals("jogos_test", c.getCatalog());
      s.execute("CREATE SCHEMA " + schema);
      c.setSchema(schema);
      try {
        if (incremental) {
          lb(c, schema, "db/changelog/db.changelog-ate-005.yaml")
              .update(new Contexts(), new LabelExpression());
          assertEquals(42, scalar(s, "SELECT count(*) FROM databasechangelog"));
        }
        var liquibase = lb(c, schema, "db/changelog/db.changelog-master.yaml");
        liquibase.update(new Contexts(), new LabelExpression());
        c.setAutoCommit(true);
        assertEquals(49, scalar(s, "SELECT count(*) FROM databasechangelog"));
        assertEquals(7, scalar(s, "SELECT count(*) FROM databasechangelog WHERE id LIKE '006-%'"));
        long p1 = pessoa(c), p2 = pessoa(c);
        String h = encoder.encode("Ficticia-" + UUID.randomUUID());
        s.execute(
            "INSERT INTO conta_acesso(pessoa_id,login,senha_hash,status) VALUES("
                + p1
                + ",' Operador ', '"
                + h
                + "','ATIVA')");
        long id = scalar(s, "SELECT id FROM conta_acesso WHERE pessoa_id=" + p1);
        falha(
            s,
            "INSERT INTO conta_acesso(pessoa_id,login,senha_hash,status) VALUES("
                + p2
                + ",'operador','"
                + h
                + "','ATIVA')",
            "23505",
            "uk_conta_acesso_login_lower");
        falha(
            s,
            "INSERT INTO conta_acesso(pessoa_id,login,senha_hash,status) VALUES("
                + p1
                + ",'outro','"
                + h
                + "','ATIVA')",
            "23505",
            "uk_conta_acesso_pessoa");
        falha(
            s,
            "INSERT INTO conta_acesso(pessoa_id,login,senha_hash,status)"
                + " VALUES(9223372036854775807,'orfao','"
                + h
                + "','ATIVA')",
            "23503",
            "fk_conta_acesso_pessoa");
        falha(
            s,
            "UPDATE conta_acesso SET status='INVALIDA' WHERE id=" + id,
            "23514",
            "ck_conta_acesso_status");
        falha(
            s,
            "UPDATE conta_acesso SET login='a b' WHERE id=" + id,
            "23514",
            "ck_conta_acesso_login");
        falha(
            s,
            "UPDATE conta_acesso SET senha_hash='nao-hash' WHERE id=" + id,
            "23514",
            "ck_conta_acesso_hash");
        s.execute("INSERT INTO conta_acesso_perfil VALUES(" + id + ",'OPERADOR')");
        falha(
            s,
            "INSERT INTO conta_acesso_perfil VALUES(" + id + ",'OPERADOR')",
            "23505",
            "pk_conta_acesso_perfil");
        falha(
            s,
            "INSERT INTO conta_acesso_perfil VALUES(" + id + ",'GERENTE')",
            "23514",
            "ck_conta_acesso_perfil");
        falha(
            s,
            "INSERT INTO conta_acesso_perfil VALUES(9223372036854775807,'OPERADOR')",
            "23503",
            "fk_conta_acesso_perfil");
        falha(s, "DELETE FROM pessoa WHERE id=" + p1, "23503", "fk_conta_acesso_pessoa");
        s.execute("DELETE FROM conta_acesso WHERE id=" + id);
        assertEquals(0, scalar(s, "SELECT count(*) FROM conta_acesso_perfil"));
        liquibase.rollback(7, new Contexts(), new LabelExpression());
        assertEquals(42, scalar(s, "SELECT count(*) FROM databasechangelog"));
        assertEquals(2, scalar(s, "SELECT count(*) FROM pessoa"));
        assertEquals(
            0,
            scalar(
                s,
                "SELECT count(*) FROM information_schema.tables WHERE table_schema='"
                    + schema
                    + "' AND table_name IN ('conta_acesso','conta_acesso_perfil')"));
      } finally {
        if (!c.getAutoCommit()) {
          c.rollback();
          c.setAutoCommit(true);
        }
        c.setSchema("public");
        s.execute("DROP SCHEMA " + schema + " CASCADE");
      }
    }
  }

  @Test
  void criacaoLimpaRestricoesERollback() throws Exception {
    verificar(false);
  }

  @Test
  void aplicacaoIncrementalPreservaMigraçõesERollback() throws Exception {
    verificar(true);
  }
}
