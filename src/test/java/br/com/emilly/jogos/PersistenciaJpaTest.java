package br.com.emilly.jogos;

import br.com.emilly.jogos.domain.GeneroJogo;
import br.com.emilly.jogos.domain.Jogo;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PersistenciaJpaTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void devePersistirERecarregarJogoComGenero() {
        GeneroJogo genero = new GeneroJogo("RPG");

        Jogo jogo = new Jogo(
                "JOGO-001",
                "The Witcher 3",
                new java.math.BigDecimal("10.000"),
                new java.math.BigDecimal("99.90"),
                java.time.LocalDate.now()
        );

        genero.adicionarJogo(jogo);

        entityManager.persist(genero);
        entityManager.persist(jogo);
        entityManager.flush();
        entityManager.clear();

        Jogo jogoRecarregado = entityManager.find(Jogo.class, jogo.getId());

        org.junit.jupiter.api.Assertions.assertNotNull(jogoRecarregado);
        org.junit.jupiter.api.Assertions.assertEquals("JOGO-001", jogoRecarregado.getCodigo());
        org.junit.jupiter.api.Assertions.assertEquals("RPG", jogoRecarregado.getGenero().getNome());
    }

    @Test
    void deveTerDezesseteChangeSetsRegistrados() {
        Number quantidade = (Number) entityManager
                .createNativeQuery("SELECT COUNT(*) FROM databasechangelog")
                .getSingleResult();

        org.junit.jupiter.api.Assertions.assertEquals(17L, quantidade.longValue());
    }

    @Test
    void deveRejeitarCodigoDuplicadoNoBanco() {
        GeneroJogo genero1 = new GeneroJogo("Aventura");
        GeneroJogo genero2 = new GeneroJogo("RPG");

        Jogo jogo1 = new Jogo(
                "JOGO-002",
                "Jogo A",
                new java.math.BigDecimal("5.000"),
                new java.math.BigDecimal("49.90"),
                java.time.LocalDate.now()
        );

        Jogo jogo2 = new Jogo(
                "JOGO-002",
                "Jogo B",
                new java.math.BigDecimal("3.000"),
                new java.math.BigDecimal("59.90"),
                java.time.LocalDate.now()
        );

        genero1.adicionarJogo(jogo1);
        genero2.adicionarJogo(jogo2);

        entityManager.persist(genero1);
        entityManager.persist(genero2);
        entityManager.persist(jogo1);
        entityManager.flush();

        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> {
                    entityManager.persist(jogo2);
                    entityManager.flush();
                }
        );
    }

    @Test
    void deveRejeitarQuantidadeNegativaNoBanco() {
        GeneroJogo genero = new GeneroJogo("Estratégia");

        entityManager.persist(genero);
        entityManager.flush();

        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> {
                    entityManager.createNativeQuery("""
                            INSERT INTO jogo
                            (codigo, nome, unidades_disponiveis, preco, data_cadastro, status, genero_jogo_id)
                            VALUES
                            ('JOGO-NEGATIVO', 'Jogo Teste', -1.000, 50.00, CURRENT_DATE, 'ATIVO', :generoId)
                            """)
                            .setParameter("generoId", genero.getId())
                            .executeUpdate();

                    entityManager.flush();
                }
        );
    }
}