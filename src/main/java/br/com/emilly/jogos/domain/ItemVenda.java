package br.com.emilly.jogos.domain;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Entity
@Table(
    name = "item_venda",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_item_venda_venda_id_jogo_id",
            columnNames = {"venda_id", "jogo_id"}))
public class ItemVenda {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "venda_id", nullable = false)
  private Venda venda;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "jogo_id", nullable = false)
  private Jogo jogo;

  @Column(nullable = false, precision = 18, scale = 3)
  private BigDecimal quantidade;

  @Column(name = "valor_unitario", nullable = false, precision = 18, scale = 2)
  private BigDecimal valorUnitario;

  protected ItemVenda() {}

  ItemVenda(Venda venda, Jogo jogo, BigDecimal quantidade) {
    this.venda = Objects.requireNonNull(venda);
    this.jogo = Objects.requireNonNull(jogo);
    this.quantidade = RegrasComerciais.decimal(quantidade, 15, 3, true, "Quantidade");
    this.valorUnitario = RegrasComerciais.decimal(jogo.getPreco(), 16, 2, false, "Preço");
  }

  public BigDecimal calcularSubtotal() {
    return quantidade.multiply(valorUnitario).setScale(2, RoundingMode.HALF_UP);
  }

  public Long getId() {
    return id;
  }

  public Venda getVenda() {
    return venda;
  }

  public Jogo getJogo() {
    return jogo;
  }

  public BigDecimal getQuantidade() {
    return quantidade;
  }

  public BigDecimal getValorUnitario() {
    return valorUnitario;
  }
}
