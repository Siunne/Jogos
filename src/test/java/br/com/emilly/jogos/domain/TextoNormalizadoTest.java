package br.com.emilly.jogos.domain;

import static org.junit.jupiter.api.Assertions.*;

import br.com.emilly.jogos.support.TextosTeste;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TextoNormalizadoTest {
  @Test
  void emailNormalizadoNaoPodeUltrapassarLimiteNemAlterarPessoa() {
    String email = "\u0130".repeat(63) + "@" + ("a".repeat(36) + ".").repeat(3) + "com";
    assertTrue(email.length() <= 180);
    assertTrue(email.toLowerCase(java.util.Locale.ROOT).length() > 180);
    var p = pessoa();
    assertThrows(IllegalArgumentException.class, () -> new Pessoa("Nome", email, "52998224725"));
    assertThrows(
        IllegalArgumentException.class, () -> p.alterarDados("Outro", email, "11144477735"));
    assertEquals("Nome anterior", p.getNome());
    assertEquals("anterior@example.com", p.getEmail());
  }

  Pessoa pessoa() {
    return new Pessoa("Nome anterior", "anterior@example.com", "52998224725");
  }

  Jogo jogo(String codigo, String nome) {
    return Jogo.novoCadastro(codigo, nome, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.ZERO);
  }

  @Test
  void pessoaRejeitaTodosControlesEVaziosSemAlterarIdentidade() {
    var p = pessoa();
    for (String value : TextosTeste.invalidos()) {
      assertThrows(
          IllegalArgumentException.class, () -> new Pessoa(value, "a@example.com", "52998224725"));
      assertThrows(
          IllegalArgumentException.class,
          () -> p.alterarDados(value, "novo@example.com", "11144477735"));
      assertEquals("Nome anterior", p.getNome());
      assertEquals("anterior@example.com", p.getEmail());
      assertEquals("52998224725", p.getCpf());
      assertThrows(
          IllegalArgumentException.class, () -> p.alterarDados("Novo", value, "11144477735"));
      assertEquals("Nome anterior", p.getNome());
    }
  }

  @Test
  void matriculaInvalidaNaoAlteraMatriculaNemFuncao() {
    var p = pessoa();
    var c = new Colaborador(p, "VEN-ANTERIOR", FuncaoColaborador.VENDEDOR, LocalDate.now());
    for (String value : TextosTeste.invalidos()) {
      assertThrows(
          IllegalArgumentException.class,
          () -> new Colaborador(p, value, FuncaoColaborador.VENDEDOR, LocalDate.now()));
      assertThrows(
          IllegalArgumentException.class, () -> c.alterarDados(value, FuncaoColaborador.GERENTE));
      assertEquals("VEN-ANTERIOR", c.getMatricula());
      assertEquals(FuncaoColaborador.VENDEDOR, c.getFuncao());
    }
  }

  @Test
  void demaisTextosObrigatoriosNaoPodemVirarVazios() {
    var g = new GeneroJogo("Aventura");
    var j = jogo("CODIGO", "Reino");
    for (String value : TextosTeste.invalidos()) {
      assertThrows(IllegalArgumentException.class, () -> new GeneroJogo(value));
      assertThrows(IllegalArgumentException.class, () -> g.alterarNome(value));
      assertEquals("Aventura", g.getNome());
      assertThrows(IllegalArgumentException.class, () -> new Fornecedor(value, "11222333000181"));
      assertThrows(IllegalArgumentException.class, () -> jogo(value, "Reino"));
      assertThrows(IllegalArgumentException.class, () -> jogo("CODIGO", value));
      assertThrows(IllegalArgumentException.class, () -> j.alterarNome(value));
      assertEquals("Reino", j.getNome());
    }
  }

  @Test
  void normalizaValidosEPreservaNbspSemAfrouxarLimites() {
    for (String text : new String[] {"\u00a0", "\u2007", "\u202f", " Nome ", "\u0000Nome\u001b"}) {
      assertEquals(text.trim(), new Pessoa(text, " A@EXAMPLE.COM ", "52998224725").getNome());
      assertEquals(
          text.trim(),
          new Colaborador(pessoa(), text, FuncaoColaborador.VENDEDOR, LocalDate.now())
              .getMatricula());
      assertEquals(text.trim(), new GeneroJogo(text).getNome());
      assertEquals(text.trim(), new Fornecedor(text, "11222333000181").getRazaoSocial());
      assertEquals(text.trim(), jogo(text, text).getNome());
    }
    assertThrows(
        IllegalArgumentException.class,
        () -> new Pessoa(" ".repeat(150) + "A", "a@example.com", "52998224725"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new Colaborador(
                pessoa(), " ".repeat(30) + "A", FuncaoColaborador.VENDEDOR, LocalDate.now()));
  }

  @Test
  void telefoneOpcionalNormalizaAusenciaEPreservaNbsp() {
    var c = new Cliente(pessoa(), null);
    for (String value : TextosTeste.invalidos()) {
      c.alterarDados(value);
      assertNull(c.getTelefone());
    }
    c.alterarDados("\u00a0");
    assertEquals("\u00a0", c.getTelefone());
    c.alterarDados(" 123 ");
    assertEquals("123", c.getTelefone());
    assertThrows(IllegalArgumentException.class, () -> c.alterarDados("A".repeat(31)));
    assertEquals("123", c.getTelefone());
  }
}
