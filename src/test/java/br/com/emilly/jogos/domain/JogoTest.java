package br.com.emilly.jogos.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JogoTest {

    @Test
    void deveCriarJogoAtivoComDadosValidos() {
        Jogo jogo = novoJogo("3.000", "12.90");

        assertEquals("JG001", jogo.getCodigo());
        assertEquals("The Witcher 3", jogo.getNome());
        assertEquals(Status.ATIVO, jogo.getStatus());
        assertEquals(LocalDate.of(2026, 8, 29), jogo.getDataCadastro());
    }

    @Test
    void deveCalcularValorTotal() {
        Jogo jogo = novoJogo("3.000", "12.90");

        BigDecimal valorTotal = jogo.calcularValorTotal();

        assertEquals(
                0,
                new BigDecimal("38.70").compareTo(valorTotal)
        );
    }

    @Test
    void deveReceberERetirarUnidades() {
        Jogo jogo = novoJogo("3.000", "12.90");

        jogo.receberUnidades(new BigDecimal("2.500"));
        jogo.retirarUnidades(new BigDecimal("1.000"));

        assertEquals(
                0,
                new BigDecimal("4.500")
                        .compareTo(jogo.getUnidadesDisponiveis())
        );
    }

    @Test
    void naoDeveRetirarQuantidadeMaiorQueAsUnidadesDisponiveis() {
        Jogo jogo = novoJogo("3.000", "12.90");

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> jogo.retirarUnidades(
                        new BigDecimal("3.001"))
        );

        assertEquals(
                "Unidades disponíveis insuficientes",
                excecao.getMessage()
        );
    }

    @Test
    void naoDeveCriarJogoComCodigoEmBranco() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Jogo(
                        "  ",
                        "The Witcher 3",
                        BigDecimal.ZERO,
                        new BigDecimal("12.90"),
                        LocalDate.of(2026, 8, 29)
                )
        );
    }

    @Test
    void naoDeveCriarJogoComUnidadesNegativas() {
        assertThrows(
                IllegalArgumentException.class,
                () -> novoJogo("-0.001", "12.90")
        );
    }

    @Test
    void deveAlterarOStatusPorComportamentoExplicito() {
        Jogo jogo = novoJogo("3.000", "12.90");

        jogo.inativar();
        assertEquals(Status.INATIVO, jogo.getStatus());

        jogo.ativar();
        assertEquals(Status.ATIVO, jogo.getStatus());
    }

    private Jogo novoJogo(
            String unidades,
            String preco) {

        return new Jogo(
                "JG001",
                "The Witcher 3",
                new BigDecimal(unidades),
                new BigDecimal(preco),
                LocalDate.of(2026, 8, 29)
        );
    }
}