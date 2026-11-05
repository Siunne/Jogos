package br.com.emilly.jogos.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class Aula08DomainTest {
  private Jogo jogo() {
    return new Jogo("A08", "Jogo", new BigDecimal("10"), BigDecimal.ONE, LocalDate.now());
  }

  @Test
  void alterarNomeGenero() {
    var genero = new GeneroJogo("RPG");
    genero.alterarNome(" Aventura ");
    assertEquals("Aventura", genero.getNome());
  }

  @Test
  void rejeitarNomeInvalido() {
    var genero = new GeneroJogo("RPG");
    assertThrows(IllegalArgumentException.class, () -> genero.alterarNome(" "));
    assertThrows(IllegalArgumentException.class, () -> genero.alterarNome("a".repeat(121)));
    assertEquals("RPG", genero.getNome());
  }

  @Test
  void trocarGenero() {
    var antigo = new GeneroJogo("RPG");
    var novo = new GeneroJogo("Aventura");
    var jogo = jogo();
    antigo.adicionarJogo(jogo);
    jogo.alterarGenero(novo);
    jogo.alterarGenero(novo);
    assertTrue(antigo.getJogos().isEmpty());
    assertEquals(1, novo.getJogos().size());
    assertSame(jogo, novo.getJogos().getFirst());
    assertSame(novo, jogo.getGenero());
  }

  @Test
  void trocaRecusadaPreservaAssociacao() {
    var antigo = new GeneroJogo("RPG");
    var novo = new GeneroJogo("Aventura");
    var jogo = jogo();
    antigo.adicionarJogo(jogo);
    novo.adicionarJogo(jogo());
    assertThrows(IllegalArgumentException.class, () -> jogo.alterarGenero(novo));
    assertSame(antigo, jogo.getGenero());
    assertTrue(antigo.getJogos().contains(jogo));
  }

  @Test
  void alterarDadosERemoverFornecedor() {
    var jogo = jogo();
    jogo.associarFornecedor(new Fornecedor("Distribuidora", "11222333000181"));
    jogo.associarFornecedor(null);
    jogo.alterarNome("Novo nome");
    jogo.alterarPreco(BigDecimal.TEN);
    jogo.alterarEstoqueMinimo(BigDecimal.ONE);
    assertNull(jogo.getFornecedor());
    assertEquals("Novo nome", jogo.getNome());
    assertEquals(BigDecimal.TEN, jogo.getPreco());
    assertEquals(BigDecimal.ONE, jogo.getEstoqueMinimo());
  }

  @Test
  void saidaRecusadaPreservaSaldo() {
    var jogo = jogo();
    assertThrows(IllegalArgumentException.class, () -> jogo.retirarUnidades(new BigDecimal("11")));
    assertEquals(0, BigDecimal.TEN.compareTo(jogo.getUnidadesDisponiveis()));
  }
}
