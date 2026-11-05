package br.com.emilly.jogos.application;

import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.exception.*;
import br.com.emilly.jogos.repository.*;
import br.com.emilly.jogos.repository.specification.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VendaService {
  private final VendaRepository repository;
  private final ClienteService clientes;
  private final ColaboradorService colaboradores;
  private final JogoRepository jogos;

  public VendaService(
      VendaRepository repository,
      ClienteService clientes,
      ColaboradorService colaboradores,
      JogoRepository jogos) {
    this.repository = repository;
    this.clientes = clientes;
    this.colaboradores = colaboradores;
    this.jogos = jogos;
  }

  public Venda buscar(Long id) {
    return repository
        .findOneById(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Venda não encontrada"));
  }

  @Transactional
  public Venda cadastrar(Long clienteId, Long vendedorId) {
    return repository.save(new Venda(clientes.buscar(clienteId), colaboradores.buscar(vendedorId)));
  }

  @Transactional
  public Venda adicionarItem(Long id, Long jogoId, BigDecimal quantidade) {
    var v = buscar(id);
    v.validarAberta();
    var jogo =
        jogos
            .findById(jogoId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Jogo não encontrado"));
    v.adicionarItem(jogo, quantidade);
    return v;
  }

  @Transactional
  public void removerItem(Long id, Long itemId) {
    buscar(id).removerItem(itemId);
  }

  @Transactional
  public Venda cancelar(Long id) {
    var v = buscar(id);
    v.cancelar();
    return v;
  }

  @Transactional
  public Venda finalizar(Long id) {
    var v = buscar(id);
    v.validarFinalizacao();
    for (var item : v.getItens()) {
      if (item.getJogo().getStatus() != Status.ATIVO)
        throw new IllegalArgumentException("Todos os jogos da venda devem estar ativos");
      item.getJogo().retirarUnidades(item.getQuantidade());
    }
    v.finalizar();
    return v;
  }

  public Page<Venda> pesquisar(
      StatusVenda status,
      Long clienteId,
      Long vendedorId,
      LocalDate inicio,
      LocalDate fim,
      Pageable p) {
    Paginacao.validar(p, Set.of("id", "dataVenda", "status"));
    return repository.findAll(
        FiltrosComerciais.vendas(status, clienteId, vendedorId, inicio, fim), p);
  }
}
