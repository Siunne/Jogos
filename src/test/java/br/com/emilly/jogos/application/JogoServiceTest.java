package br.com.emilly.jogos.application;

import br.com.emilly.jogos.domain.GeneroJogo;
import br.com.emilly.jogos.domain.Jogo;
import br.com.emilly.jogos.exception.RecursoNaoEncontradoException;
import br.com.emilly.jogos.repository.GeneroJogoRepository;
import br.com.emilly.jogos.repository.JogoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.persistence.EntityManager;
import java.util.UUID;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JogoServiceTest {

    @Autowired
    private JogoService jogoService;

    @Autowired
    private GeneroJogoRepository generoRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void deveCadastrarJogoComGenero() {

        GeneroJogo genero =
                generoRepository.save(new GeneroJogo("RPG"));

        Jogo jogo = novoJogo("TESTE-001");

        Jogo cadastrado =
                jogoService.cadastrar(jogo, genero.getId());

        assertNotNull(cadastrado.getId());
        assertEquals(
                genero.getId(),
                cadastrado.getGenero().getId());
    }

    @Test
    void naoDeveCadastrarQuandoGeneroNaoExiste() {

        Jogo jogo = novoJogo("TESTE-ROLLBACK");

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> jogoService.cadastrar(
                        jogo,
                        Long.MAX_VALUE));

        assertFalse(
                jogoRepository.existsByCodigo(jogo.getCodigo()));
    }

    @Test
    void deveAtualizarUnidadesPorDirtyChecking() {

        GeneroJogo genero =
                generoRepository.save(new GeneroJogo("Aventura"));

        Jogo jogo = jogoService.cadastrar(
                novoJogo("TESTE-DIRTY"),
                genero.getId());

        jogoService.receberUnidades(
                jogo.getId(),
                new BigDecimal("5.000"));

        entityManager.flush();
        entityManager.clear();

        Jogo atualizado = jogoRepository
                .findById(jogo.getId())
                .orElseThrow();

        assertEquals(
                0,
                new BigDecimal("15.000")
                        .compareTo(atualizado.getUnidadesDisponiveis()));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void deveReverterEntradaJaEnviadaAoBancoQuandoTransacaoFalhar() {
        String identificador = UUID.randomUUID().toString();
        Long generoId = transactionTemplate.execute(s ->
                generoRepository.save(new GeneroJogo("Rollback " + identificador)).getId());
        Long jogoId = null;
        try {
            jogoId = jogoService.cadastrar(novoJogo(identificador), generoId).getId();
            Long id = jogoId;
            assertThrows(RecursoNaoEncontradoException.class, () ->
                    transactionTemplate.executeWithoutResult(s -> {
                        jogoService.receberUnidades(id, new BigDecimal("5.000"));
                        entityManager.flush();
                        entityManager.clear();
                        assertEquals(0, new BigDecimal("15.000").compareTo(
                                jogoRepository.findById(id).orElseThrow().getUnidadesDisponiveis()));
                        // A falha ocorre depois de o UPDATE ter sido enviado ao PostgreSQL.
                        jogoService.alterar(id, "Alteração recusada", BigDecimal.ONE,
                                BigDecimal.ZERO, Long.MAX_VALUE, null);
                    }));
            assertEquals(0, new BigDecimal("10.000").compareTo(
                    jogoRepository.findById(id).orElseThrow().getUnidadesDisponiveis()));
        } finally {
            if (jogoId != null) jogoRepository.deleteById(jogoId);
            generoRepository.deleteById(generoId);
        }
    }

    private Jogo novoJogo(String codigo) {
        return new Jogo(
                codigo,
                "Jogo de Teste",
                new BigDecimal("10.000"),
                new BigDecimal("99.90"),
                LocalDate.now()
        );
    }
}
