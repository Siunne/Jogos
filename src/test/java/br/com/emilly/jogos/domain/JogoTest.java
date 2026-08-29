package br.com.emilly.jogos.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
class JogoTest {

    @Test
    void deveCriarJogoComDadosValidos() {
        GeneroJogo genero = new GeneroJogo(
                1L,
                "RPG",
                Status.ATIVO
        );

        Jogo jogo = new Jogo(
                1L,
                "JG001",
                "The Witcher 3",
                5,
                new BigDecimal("149.90"),
                LocalDate.of(2026, 8, 29),
                Status.ATIVO,
                genero
        );

        assertEquals(1L, jogo.getId());
        assertEquals("JG001", jogo.getCodigo());
        assertEquals("The Witcher 3", jogo.getNome());
        assertEquals(5, jogo.getUnidadesDisponiveis());
        assertEquals(new BigDecimal("149.90"), jogo.getPreco());
        assertEquals(LocalDate.of(2026, 8, 29), jogo.getDataCadastro());
        assertEquals(Status.ATIVO, jogo.getStatus());
        assertEquals(genero, jogo.getGenero());
    }

    @Test
    void naoDeveCriarJogoComCodigoVazio() {
        GeneroJogo genero = new GeneroJogo(
                1L,
                "RPG",
                Status.ATIVO
        );

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> new Jogo(
                        1L,
                        "",
                        "The Witcher 3",
                        5,
                        new BigDecimal("149.90"),
                        LocalDate.of(2026, 8, 29),
                        Status.ATIVO,
                        genero
                )
        );

        assertEquals("Código do jogo é obrigatório", erro.getMessage());
    }

    @Test
    void naoDeveCriarJogoComNomeVazio() {
        GeneroJogo genero = new GeneroJogo(
                1L,
                "RPG",
                Status.ATIVO
        );

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> new Jogo(
                        1L,
                        "JG001",
                        "",
                        5,
                        new BigDecimal("149.90"),
                        LocalDate.of(2026, 8, 29),
                        Status.ATIVO,
                        genero
                )
        );

        assertEquals("Nome do jogo é obrigatório", erro.getMessage());
    }

    @Test
    void naoDeveCriarJogoComUnidadesNegativas() {
        GeneroJogo genero = new GeneroJogo(
                1L,
                "RPG",
                Status.ATIVO
        );

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> new Jogo(
                        1L,
                        "JG001",
                        "The Witcher 3",
                        -1,
                        new BigDecimal("149.90"),
                        LocalDate.of(2026, 8, 29),
                        Status.ATIVO,
                        genero
                )
        );

        assertEquals(
                "Unidades disponíveis devem ser zero ou maior",
                erro.getMessage()
        );
    }

    @Test
    void naoDeveCriarJogoComPrecoNegativo() {
        GeneroJogo genero = new GeneroJogo(
                1L,
                "RPG",
                Status.ATIVO
        );

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> new Jogo(
                        1L,
                        "JG001",
                        "The Witcher 3",
                        5,
                        new BigDecimal("-10.00"),
                        LocalDate.of(2026, 8, 29),
                        Status.ATIVO,
                        genero
                )
        );

        assertEquals(
                "Preço deve ser zero ou maior",
                erro.getMessage()
        );
    }

    @Test
    void naoDeveCriarJogoSemDataCadastro() {
        GeneroJogo genero = new GeneroJogo(
                1L,
                "RPG",
                Status.ATIVO
        );

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> new Jogo(
                        1L,
                        "JG001",
                        "The Witcher 3",
                        5,
                        new BigDecimal("149.90"),
                        null,
                        Status.ATIVO,
                        genero
                )
        );

        assertEquals(
                "Data de cadastro é obrigatória",
                erro.getMessage()
        );
    }

    @Test
    void naoDeveCriarJogoSemStatus() {
        GeneroJogo genero = new GeneroJogo(
                1L,
                "RPG",
                Status.ATIVO
        );

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> new Jogo(
                        1L,
                        "JG001",
                        "The Witcher 3",
                        5,
                        new BigDecimal("149.90"),
                        LocalDate.of(2026, 8, 29),
                        null,
                        genero
                )
        );

        assertEquals(
                "Status é obrigatório",
                erro.getMessage()
        );
    }

    @Test
    void naoDeveCriarJogoSemGenero() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> new Jogo(
                        1L,
                        "JG001",
                        "The Witcher 3",
                        5,
                        new BigDecimal("149.90"),
                        LocalDate.of(2026, 8, 29),
                        Status.ATIVO,
                        null
                )
        );

        assertEquals(
                "Gênero do jogo é obrigatório",
                erro.getMessage()
        );
    }


}