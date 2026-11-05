package br.com.emilly.jogos.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.*;
import java.time.*;
import org.junit.jupiter.api.Test;

class VendaTest {
  private Venda venda() {
    var p = new Pessoa("Pessoa", "pessoa@example.com", "52998224725");
    return new Venda(
        new Cliente(p, null), new Colaborador(p, "V", FuncaoColaborador.VENDEDOR, LocalDate.now()));
  }

  private Jogo jogo() {
    return Jogo.novoCadastro(
        "COD",
        "Reino das Estrelas",
        new BigDecimal("10.000"),
        new BigDecimal("100.00"),
        BigDecimal.ONE);
  }

  @Test
  void preservaPrecoHistoricoETotal() {
    var v = venda();
    var j = jogo();
    var i = v.adicionarItem(j, new BigDecimal("2.000"));
    j.alterarPreco(new BigDecimal("120.00"));
    assertEquals(new BigDecimal("100.00"), i.getValorUnitario());
    assertEquals(new BigDecimal("200.00"), i.calcularSubtotal());
    assertEquals(new BigDecimal("200.00"), v.calcularTotal());
    assertEquals(new BigDecimal("10.000"), j.getUnidadesDisponiveis());
  }

  @Test
  void rejeitaJogoRepetido() {
    var v = venda();
    var j = jogo();
    v.adicionarItem(j, BigDecimal.ONE);
    assertThrows(IllegalArgumentException.class, () -> v.adicionarItem(j, BigDecimal.ONE));
  }

  @Test
  void rejeitaQuantidadesInvalidas() {
    for (var q :
        new BigDecimal[] {null, BigDecimal.ZERO, new BigDecimal("-1"), new BigDecimal("1.2345")})
      assertThrows(IllegalArgumentException.class, () -> venda().adicionarItem(jogo(), q));
  }

  @Test
  void vaziaNaoFinaliza() {
    assertThrows(IllegalArgumentException.class, () -> venda().finalizar());
  }

  @Test
  void finalizadaNaoAlteraNemEstorna() {
    var v = venda();
    v.adicionarItem(jogo(), BigDecimal.ONE);
    v.finalizar();
    assertThrows(IllegalStateException.class, v::finalizar);
    assertThrows(IllegalStateException.class, v::cancelar);
    assertThrows(IllegalStateException.class, () -> v.adicionarItem(jogo(), BigDecimal.ONE));
    assertThrows(IllegalStateException.class, () -> v.removerItem(1L));
  }

  @Test
  void cancelaSemBaixaENaoReabre() {
    var v = venda();
    var j = jogo();
    v.adicionarItem(j, BigDecimal.ONE);
    v.cancelar();
    assertEquals(StatusVenda.CANCELADA, v.getStatus());
    assertEquals(new BigDecimal("10.000"), j.getUnidadesDisponiveis());
    assertThrows(IllegalStateException.class, v::finalizar);
  }

  @Test
  void rejeitaJogoInativo() {
    var j = jogo();
    j.inativar();
    assertThrows(IllegalArgumentException.class, () -> venda().adicionarItem(j, BigDecimal.ONE));
  }

  @Test
  void subtotalFracionarioArredondaComHalfUp() {
    var j = jogo();
    j.alterarPreco(new BigDecimal("0.01"));
    var v = venda();
    var item = v.adicionarItem(j, new BigDecimal("0.500"));
    assertEquals(new BigDecimal("0.01"), item.calcularSubtotal());
    assertEquals(new BigDecimal("0.01"), v.calcularTotal());
  }

  @Test
  void dtoQuantidadeRejeitaMaisDeTresCasas() {
    try (var factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
      var erros =
          factory
              .getValidator()
              .validate(new br.com.emilly.jogos.dto.ItemVendaRequest(1L, new BigDecimal("1.2345")));
      assertTrue(erros.stream().anyMatch(e -> e.getPropertyPath().toString().equals("quantidade")));
    }
  }
}
