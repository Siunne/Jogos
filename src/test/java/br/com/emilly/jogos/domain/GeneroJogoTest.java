package br.com.emilly.jogos.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
class GeneroJogoTest {

    @Test
    void deveCriarGeneroComDadosValidos() {
        GeneroJogo genero = new GeneroJogo(
                1L,
                "RPG",
                Status.ATIVO
        );

        assertEquals(1L, genero.getId());
        assertEquals("RPG", genero.getNome());
        assertEquals(Status.ATIVO, genero.getStatus());
    }

    @Test
    void naoDeveCriarGeneroComNomeVazio() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> new GeneroJogo(
                        1L,
                        "",
                        Status.ATIVO
                )
        );

        assertEquals("Nome do gênero é obrigatório", erro.getMessage());
    }

    @Test
    void naoDeveCriarGeneroSemStatus() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> new GeneroJogo(
                        1L,
                        "RPG",
                        null
                )
        );

        assertEquals("Status é obrigatório", erro.getMessage());
    }

    @Test
    void deveInativarGenero() {
        GeneroJogo genero = new GeneroJogo(
                1L,
                "RPG",
                Status.ATIVO
        );

        genero.inativar();

        assertEquals(Status.INATIVO, genero.getStatus());
    }

    @Test
    void deveAtivarGenero() {
        GeneroJogo genero = new GeneroJogo(
                1L,
                "RPG",
                Status.INATIVO
        );

        genero.ativar();

        assertEquals(Status.ATIVO, genero.getStatus());
    }



}