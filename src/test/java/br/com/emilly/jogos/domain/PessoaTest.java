package br.com.emilly.jogos.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;
import org.junit.jupiter.api.Test;

class PessoaTest {
  private Pessoa pessoa() {
    return new Pessoa("Pessoa Exemplo", "Pessoa@EXAMPLE.com", "52998224725");
  }

  @Test
  void normalizaEmailEDataDoServidor() {
    var p = pessoa();
    assertEquals("pessoa@example.com", p.getEmail());
    assertEquals(LocalDate.now(), p.getDataCadastro());
    assertEquals(Status.ATIVO, p.getStatus());
  }

  @Test
  void alteracaoPreservaDataEStatus() {
    var p = pessoa();
    var data = p.getDataCadastro();
    p.inativar();
    p.alterarDados("Outro nome", "outro@example.com", "11144477735");
    assertEquals(data, p.getDataCadastro());
    assertEquals(Status.INATIVO, p.getStatus());
    p.ativar();
    assertEquals(Status.ATIVO, p.getStatus());
  }

  @Test
  void validaEmailNomeEAlteracaoNoDominio() {
    var p = pessoa();
    assertThrows(
        IllegalArgumentException.class, () -> p.alterarDados(" ", "a@example.com", "52998224725"));
    assertThrows(
        IllegalArgumentException.class,
        () -> p.alterarDados("Pessoa", "email-invalido", "52998224725"));
    assertThrows(
        IllegalArgumentException.class,
        () -> p.alterarDados("Pessoa", "a@example.com", "52998224724"));
    assertEquals("pessoa@example.com", p.getEmail());
  }

  @Test
  void papeisSimultaneosComMesmaIdentidade() {
    var p = pessoa();
    var c = new Cliente(p, null);
    var v = new Colaborador(p, "VEN-1", FuncaoColaborador.VENDEDOR, LocalDate.now());
    assertSame(c.getPessoa(), v.getPessoa());
    c.alterarDados("123");
    assertEquals("123", c.getTelefone());
    c.alterarDados(" ");
    assertNull(c.getTelefone());
  }

  @Test
  void inativaNaoRecebePapel() {
    var p = pessoa();
    p.inativar();
    assertThrows(IllegalArgumentException.class, () -> new Cliente(p, null));
    assertThrows(
        IllegalArgumentException.class,
        () -> new Colaborador(p, "V", FuncaoColaborador.VENDEDOR, LocalDate.now()));
  }

  @Test
  void colaboradorValidaFuncaoEAdmissao() {
    var p = pessoa();
    assertThrows(
        IllegalArgumentException.class,
        () -> new Colaborador(p, "V", FuncaoColaborador.VENDEDOR, null));
    var c = new Colaborador(p, "V", FuncaoColaborador.ESTOQUISTA, LocalDate.now());
    assertThrows(IllegalArgumentException.class, c::validarVendedor);
    c.alterarDados("G", FuncaoColaborador.GERENTE);
    assertDoesNotThrow(c::validarVendedor);
  }
}
