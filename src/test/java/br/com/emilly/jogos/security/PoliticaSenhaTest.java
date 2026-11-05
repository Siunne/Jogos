package br.com.emilly.jogos.security;

import static org.junit.jupiter.api.Assertions.*;

import br.com.emilly.jogos.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PoliticaSenhaTest {
  @Test
  void nulEPadrõesPeriodicosComNulSaoRejeitadosAntesDoEncoder() {
    var bruto =
        org.springframework.security.crypto.factory.PasswordEncoderFactories
            .createDelegatingPasswordEncoder();
    String a = "loja\0".repeat(4), b = "loja\0".repeat(6);
    a = a.substring(0, a.length() - 1);
    b = b.substring(0, b.length() - 1);
    assertTrue(bruto.matches("\0".repeat(20), bruto.encode("\0".repeat(12))));
    assertTrue(bruto.matches(b, bruto.encode(a)));
    for (String invalida :
        new String[] {
          a, b, "\0".repeat(12), "\0".repeat(13), "\0".repeat(20), "prefixo-valido\0fim"
        }) {
      assertFalse(PoliticaSenha.valida(invalida));
      assertThrows(IllegalArgumentException.class, () -> PoliticaSenha.validar(invalida));
    }
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"curta", "            ", "\t\n\r", "12345678901"})
  void rejeitaSenhaAusenteCurtaOuBranca(String senha) {
    assertFalse(PoliticaSenha.valida(senha));
    assertThrows(IllegalArgumentException.class, () -> PoliticaSenha.validar(senha));
  }

  @Test
  void limitesAsciiUnicodeEUnicodeMalformado() {
    assertTrue(PoliticaSenha.valida("a".repeat(12)));
    assertTrue(PoliticaSenha.valida("a".repeat(72)));
    assertFalse(PoliticaSenha.valida("a".repeat(73)));
    assertTrue(PoliticaSenha.valida("á".repeat(36)));
    assertFalse(PoliticaSenha.valida("á".repeat(37)));
    assertTrue(PoliticaSenha.valida("😀".repeat(18)));
    assertFalse(PoliticaSenha.valida("😀".repeat(19)));
    assertFalse(PoliticaSenha.valida("a".repeat(12) + "\uD800"));
  }

  @Test
  void loginSoNormalizaEspacoAsciiECaixa() {
    assertEquals("abc.def-_123", ContaAcesso.normalizarLogin("  AbC.DeF-_123  "));
    assertEquals("a".repeat(80), ContaAcesso.normalizarLogin(" A" + "a".repeat(79) + " "));
    for (String s : new String[] {"a".repeat(81), "\u001babc", "abc\u00a0", "abc\u2003", "abc\t"})
      assertThrows(IllegalArgumentException.class, () -> ContaAcesso.normalizarLogin(s));
  }

  @Test
  void dominioNaoAceitaTextoAbertoComoHash() {
    var p = new Pessoa("Pessoa Exemplo", "ficticia@example.invalid", "52998224725");
    assertThrows(
        IllegalArgumentException.class,
        () -> new ContaAcesso(p, "operador", "texto-aberto-ficticio"));
  }
}
