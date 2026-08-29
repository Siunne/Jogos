package br.com.emilly.jogos.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeneroJogoTest {

    @Test
    void deveAdicionarJogoEManejarOsDoisLadosDaAssociacao() {
        GeneroJogo genero = new GeneroJogo("RPG");
        Jogo jogo = novoJogo("JG001");

        genero.adicionarJogo(jogo);

        assertEquals(1, genero.getJogos().size());
        assertSame(jogo, genero.getJogos().getFirst());
        assertSame(genero, jogo.getGenero());
    }

    @Test
    void naoDeveAdicionarJogoNulo() {
        GeneroJogo genero = new GeneroJogo("RPG");

        assertThrows(
                NullPointerException.class,
                () -> genero.adicionarJogo(null)
        );
    }

    @Test
    void naoDeveAdicionarDoisJogosComOMesmoCodigo() {
        GeneroJogo genero = new GeneroJogo("RPG");
        genero.adicionarJogo(novoJogo("JG001"));

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> genero.adicionarJogo(novoJogo("JG001"))
        );

        assertEquals(
                "Código do jogo já utilizado no gênero",
                excecao.getMessage()
        );
    }

    @Test
    void naoDevePermitirQueJogoPertencaADoisGeneros() {
        GeneroJogo rpg = new GeneroJogo("RPG");
        GeneroJogo aventura = new GeneroJogo("Aventura");

        Jogo jogo = novoJogo("JG001");

        rpg.adicionarJogo(jogo);

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> aventura.adicionarJogo(jogo)
        );

        assertEquals(
                "Jogo já pertence a outro gênero",
                excecao.getMessage()
        );
    }

    @Test
    void naoDeveExporUmaListaInternaModificavel() {
        GeneroJogo genero = new GeneroJogo("RPG");

        Jogo jogo = novoJogo("JG001");
        genero.adicionarJogo(jogo);

        assertThrows(
                UnsupportedOperationException.class,
                () -> genero.getJogos().add(
                        novoJogo("JG002")
                )
        );
    }

    private Jogo novoJogo(String codigo) {
        return new Jogo(
                codigo,
                "The Witcher 3",
                new BigDecimal("3.000"),
                new BigDecimal("12.90"),
                LocalDate.of(2026, 8, 29)
        );
    }
}