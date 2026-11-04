package br.com.emilly.jogos;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import javax.sql.DataSource;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test")
class MigracaoGeneroTest {
    @Autowired DataSource dataSource;

    @Test void aplicarReverterEImpedirDuplicidadesSemExcluirDados() throws Exception {
        // Schema exclusivo: não reverte nem modifica as tabelas do projeto.
        String schema = "aula08_" + UUID.randomUUID().toString().replace("-", "");
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA " + schema);
            try {
                statement.execute("CREATE TABLE " + schema + ".genero_jogo (nome varchar(120), status varchar(20))");
                statement.execute("INSERT INTO " + schema + ".genero_jogo VALUES ('RPG', 'ATIVO')");
                var database = new PostgresDatabase();
                database.setConnection(new JdbcConnection(connection));
                database.setDefaultSchemaName(schema);
                database.setLiquibaseSchemaName(schema);
                var liquibase = new Liquibase("db/changelog/changes/004-unique-nome-genero-jogo.yaml",
                        new ClassLoaderResourceAccessor(), database);
                liquibase.update(new Contexts(), new LabelExpression());
                try (var result = statement.executeQuery("SELECT to_regclass('" + schema + ".uk_genero_jogo_nome_lower')")) {
                    assertTrue(result.next());
                    assertNotNull(result.getString(1));
                }
                liquibase.rollback(1, new Contexts(), new LabelExpression());
                try (var result = statement.executeQuery("SELECT to_regclass('" + schema + ".uk_genero_jogo_nome_lower')")) {
                    assertTrue(result.next());
                    assertNull(result.getString(1));
                }
                statement.execute("INSERT INTO " + schema + ".genero_jogo VALUES ('  rpg  ', 'ATIVO')");
                assertThrows(liquibase.exception.LiquibaseException.class,
                        () -> liquibase.update(new Contexts(), new LabelExpression()));
                try (var result = statement.executeQuery("SELECT count(*) FROM " + schema + ".genero_jogo")) {
                    assertTrue(result.next());
                    assertEquals(2, result.getInt(1));
                }
            } finally {
                statement.execute("DROP SCHEMA " + schema + " CASCADE");
            }
        }
    }
}
