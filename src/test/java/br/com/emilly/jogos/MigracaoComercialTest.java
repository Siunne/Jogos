package br.com.emilly.jogos;

import static org.junit.jupiter.api.Assertions.*;

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
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MigracaoComercialTest {
  @Autowired DataSource source;

  void aplicar(Connection connection, String schema, String changelog) throws Exception {
    var db = new PostgresDatabase();
    db.setConnection(new JdbcConnection(connection));
    db.setDefaultSchemaName(schema);
    db.setLiquibaseSchemaName(schema);
    new Liquibase(changelog, new ClassLoaderResourceAccessor(), db)
        .update(new Contexts(), new LabelExpression());
  }

  int contar(Statement s, String sql) throws Exception {
    try (var rs = s.executeQuery(sql)) {
      assertTrue(rs.next());
      return rs.getInt(1);
    }
  }

  interface VerificacaoSql {
    void executar(Statement statement) throws Exception;
  }

  void verificar(boolean incremental) throws Exception {
    verificar(incremental, statement -> {});
  }

  void verificar(boolean incremental, VerificacaoSql verificacao) throws Exception {
    String schema = "aula10_" + UUID.randomUUID().toString().replace("-", "");
    try (var c = source.getConnection();
        var s = c.createStatement()) {
      assertTrue(c.getMetaData().getURL().split("\\?")[0].endsWith("/jogos_test"));
      s.execute("CREATE SCHEMA " + schema);
      c.setSchema(schema);
      try {
        if (incremental) {
          aplicar(c, schema, "db/changelog/db.changelog-ate-004.yaml");
          s.execute(
              "INSERT INTO fornecedor(razao_social,cnpj,status) VALUES ('Legado"
                  + " isolado','00000000000000','ATIVO')");
        }
        aplicar(c, schema, "db/changelog/db.changelog-master.yaml");
        c.setAutoCommit(true);
        assertEquals(
            5,
            contar(
                s,
                "SELECT count(*) FROM information_schema.tables WHERE table_schema='"
                    + schema
                    + "' AND table_name IN"
                    + " ('pessoa','cliente','colaborador','venda','item_venda')"));
        assertEquals(
            24,
            contar(
                s, "SELECT count(*) FROM " + schema + ".databasechangelog WHERE id LIKE '005-%'"));
        assertEquals(
            7,
            contar(
                s,
                "SELECT count(*) FROM pg_constraint c JOIN pg_namespace n ON n.oid=c.connamespace"
                    + " WHERE n.nspname='"
                    + schema
                    + "' AND c.conname LIKE 'ck_%' AND c.conrelid IN ('"
                    + schema
                    + ".pessoa'::regclass,'"
                    + schema
                    + ".colaborador'::regclass,'"
                    + schema
                    + ".venda'::regclass,'"
                    + schema
                    + ".item_venda'::regclass)"));
        assertEquals(
            6,
            contar(
                s,
                "SELECT count(*) FROM pg_constraint c JOIN pg_namespace n ON n.oid=c.connamespace"
                    + " WHERE n.nspname='"
                    + schema
                    + "' AND c.contype='f' AND c.conrelid IN ('"
                    + schema
                    + ".cliente'::regclass,'"
                    + schema
                    + ".colaborador'::regclass,'"
                    + schema
                    + ".venda'::regclass,'"
                    + schema
                    + ".item_venda'::regclass)"));
        if (incremental)
          assertEquals(1, contar(s, "SELECT count(*) FROM fornecedor WHERE cnpj='00000000000000'"));
        verificacao.executar(s);
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
  void aplicaTodasMigracoesEmSchemaLimpo() throws Exception {
    verificar(false);
  }

  @Test
  void aplica005Sobre001A004SemAlterarFornecedorLegado() throws Exception {
    verificar(true);
  }

  @Test
  void emailNormalizadoDuplicadoViolaExatamenteIndiceUnico() throws Exception {
    verificar(
        false,
        s -> {
          s.execute(
              "INSERT INTO pessoa(nome,email,cpf,data_cadastro,status) VALUES"
                  + " ('Exemplo','pessoa@example.com','52998224725',CURRENT_DATE,'ATIVO')");
          var erro =
              assertThrows(
                  SQLException.class,
                  () ->
                      s.execute(
                          "INSERT INTO pessoa(nome,email,cpf,data_cadastro,status) VALUES"
                              + " ('Outro','pessoa@example.com','11144477735',CURRENT_DATE,'ATIVO')"));
          assertEquals("23505", erro.getSQLState());
          assertTrue(erro.getMessage().contains("uk_pessoa_email_normalizado"));
          assertEquals(1, contar(s, "SELECT count(*) FROM pessoa"));
        });
  }

  @Test
  void emailNaoNormalizadoViolaExatamenteCheck() throws Exception {
    verificar(
        false,
        s -> {
          var erro =
              assertThrows(
                  SQLException.class,
                  () ->
                      s.execute(
                          "INSERT INTO pessoa(nome,email,cpf,data_cadastro,status) VALUES"
                              + " ('Exemplo','PESSOA@example.com','52998224725',CURRENT_DATE,'ATIVO')"));
          assertEquals("23514", erro.getSQLState());
          assertTrue(erro.getMessage().contains("ck_pessoa_email_normalizado"));
          assertEquals(0, contar(s, "SELECT count(*) FROM pessoa"));
        });
  }

  @Test
  void parVendaJogoPossuiMesmoNomeNoJpaEBloqueiaDuplicidadeNoBanco() throws Exception {
    var annotation =
        br.com.emilly.jogos.domain.ItemVenda.class.getAnnotation(jakarta.persistence.Table.class);
    assertEquals("uk_item_venda_venda_id_jogo_id", annotation.uniqueConstraints()[0].name());
    verificar(
        false,
        s -> {
          s.execute(
              "INSERT INTO pessoa(nome,email,cpf,data_cadastro,status) VALUES"
                  + " ('Exemplo','pessoa@example.com','52998224725',CURRENT_DATE,'ATIVO')");
          s.execute("INSERT INTO cliente(pessoa_id) SELECT id FROM pessoa");
          s.execute(
              "INSERT INTO colaborador(pessoa_id,matricula,funcao,data_admissao)"
                  + " SELECT id,'VEN-TESTE','VENDEDOR',CURRENT_DATE FROM pessoa");
          s.execute("INSERT INTO genero_jogo(nome,status) VALUES ('Aventura','ATIVO')");
          s.execute(
              "INSERT INTO"
                  + " jogo(codigo,nome,unidades_disponiveis,preco,data_cadastro,status,genero_jogo_id,estoque_minimo)"
                  + " SELECT 'TESTE','Reino',10,100,CURRENT_DATE,'ATIVO',id,1 FROM genero_jogo");
          s.execute(
              "INSERT INTO venda(cliente_id,vendedor_id,data_venda,status) SELECT"
                  + " c.id,v.id,CURRENT_TIMESTAMP,'ABERTA' FROM cliente c CROSS JOIN colaborador"
                  + " v");
          String insert =
              "INSERT INTO item_venda(venda_id,jogo_id,quantidade,valor_unitario)"
                  + " SELECT v.id,j.id,1,100 FROM venda v CROSS JOIN jogo j";
          s.execute(insert);
          var erro = assertThrows(SQLException.class, () -> s.execute(insert));
          assertEquals("23505", erro.getSQLState());
          assertTrue(erro.getMessage().contains("uk_item_venda_venda_id_jogo_id"));
          assertEquals(1, contar(s, "SELECT count(*) FROM item_venda"));
        });
  }
}
