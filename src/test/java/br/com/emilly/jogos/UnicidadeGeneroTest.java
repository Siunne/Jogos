package br.com.emilly.jogos;

import br.com.emilly.jogos.domain.GeneroJogo;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test") @Transactional
class UnicidadeGeneroTest {
    @Autowired EntityManager em;
    @Test void indiceRejeitaCaixaEEspacosMesmoSemService() {
        em.persist(new GeneroJogo("Unicidade RPG"));
        em.flush();
        var erro = assertThrows(org.hibernate.exception.ConstraintViolationException.class,
            () -> em.createNativeQuery("INSERT INTO genero_jogo(nome,status) VALUES ('  UNICIDADE RPG  ', 'ATIVO')").executeUpdate());
        assertEquals("23505", erro.getSQLState());
        assertTrue(erro.getSQLException().getMessage().contains("uk_genero_jogo_nome_lower"));
    }
}
