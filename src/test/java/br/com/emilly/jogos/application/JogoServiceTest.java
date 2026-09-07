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
    void deveFazerRollbackQuandoGeneroNaoExiste() {

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

        Jogo atualizado = jogoRepository
                .findById(jogo.getId())
                .orElseThrow();

        assertEquals(
                0,
                new BigDecimal("15.000")
                        .compareTo(atualizado.getUnidadesDisponiveis()));
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