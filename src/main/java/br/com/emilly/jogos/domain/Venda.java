package br.com.emilly.jogos.domain;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "venda")
public class Venda {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cliente_id", nullable = false)
  private Cliente cliente;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "vendedor_id", nullable = false)
  private Colaborador vendedor;

  @Column(name = "data_venda", nullable = false)
  private LocalDateTime dataVenda;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private StatusVenda status;

  @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("id ASC")
  private List<ItemVenda> itens = new ArrayList<>();

  protected Venda() {}

  public Venda(Cliente cliente, Colaborador vendedor) {
    if (cliente == null || vendedor == null)
      throw new IllegalArgumentException("Cliente e vendedor são obrigatórios");
    this.cliente = cliente;
    this.vendedor = vendedor;
    validarParticipantes();
    dataVenda = LocalDateTime.now();
    status = StatusVenda.ABERTA;
  }

  public void validarParticipantes() {
    RegrasComerciais.ativa(cliente.getPessoa());
    vendedor.validarVendedor();
  }

  public void validarAberta() {
    if (status != StatusVenda.ABERTA)
      throw new IllegalStateException("Somente vendas abertas podem ser alteradas");
  }

  public void validarFinalizacao() {
    validarAberta();
    if (itens.isEmpty()) throw new IllegalArgumentException("Venda deve possuir ao menos um item");
    validarParticipantes();
  }

  public ItemVenda adicionarItem(Jogo jogo, BigDecimal quantidade) {
    validarAberta();
    if (jogo == null || jogo.getStatus() != Status.ATIVO)
      throw new IllegalArgumentException("Jogo deve estar ativo");
    if (itens.stream()
        .anyMatch(
            i ->
                i.getJogo() == jogo
                    || (jogo.getId() != null && Objects.equals(i.getJogo().getId(), jogo.getId()))))
      throw new IllegalArgumentException("Jogo já incluído na venda");
    ItemVenda item = new ItemVenda(this, jogo, quantidade);
    itens.add(item);
    return item;
  }

  public void removerItem(Long itemId) {
    validarAberta();
    if (!itens.removeIf(i -> itemId != null && Objects.equals(i.getId(), itemId)))
      throw new br.com.emilly.jogos.exception.RecursoNaoEncontradoException(
          "Item não pertence à venda");
  }

  public void finalizar() {
    validarFinalizacao();
    status = StatusVenda.FINALIZADA;
  }

  public void cancelar() {
    validarAberta();
    status = StatusVenda.CANCELADA;
  }

  public BigDecimal calcularTotal() {
    return itens.stream()
        .map(ItemVenda::calcularSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(2, RoundingMode.HALF_UP);
  }

  public Long getId() {
    return id;
  }

  public Cliente getCliente() {
    return cliente;
  }

  public Colaborador getVendedor() {
    return vendedor;
  }

  public LocalDateTime getDataVenda() {
    return dataVenda;
  }

  public StatusVenda getStatus() {
    return status;
  }

  public List<ItemVenda> getItens() {
    return Collections.unmodifiableList(itens);
  }
}
