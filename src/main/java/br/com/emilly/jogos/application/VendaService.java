package br.com.emilly.jogos.application;

import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.exception.*;
import br.com.emilly.jogos.repository.*;
import br.com.emilly.jogos.repository.specification.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
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
  private final EntityManager em;
  private final BloqueioEstoque bloqueio;

  public VendaService(
      VendaRepository repository,
      ClienteService clientes,
      ColaboradorService colaboradores,
      JogoRepository jogos,
      EntityManager em,
      BloqueioEstoque bloqueio) {
    this.repository = repository;
    this.clientes = clientes;
    this.colaboradores = colaboradores;
    this.jogos = jogos;
    this.em = em;
    this.bloqueio = bloqueio;
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
    var v = buscarParaAlterar(id);
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
    buscarParaAlterar(id).removerItem(itemId);
  }

  @Transactional
  public Venda cancelar(Long id) {
    var v = buscarParaAlterar(id);
    v.cancelar();
    return v;
  }

  @Transactional
  public Venda finalizar(Long id) {
    var v = buscarParaAlterar(id);
    v.validarFinalizacao();
    var ordenados =
        v.getItens().stream().sorted(Comparator.comparing(item -> item.getJogo().getId())).toList();
    for (var item : ordenados) {
      bloqueio.adquirir(item.getJogo());
      if (item.getJogo().getStatus() != Status.ATIVO)
        throw new IllegalArgumentException("Todos os jogos da venda devem estar ativos");
      item.getJogo().retirarUnidades(item.getQuantidade());
    }
    v.finalizar();
    return v;
  }

  private Venda buscarParaAlterar(Long id) {
    var v = em.find(Venda.class, id);
    if (v == null) throw new RecursoNaoEncontradoException("Venda não encontrada");
    // Coordena comandos sobre a mesma venda; não bloqueia jogos em inclusão/remoção.
    if (em.getLockMode(v) != LockModeType.PESSIMISTIC_WRITE) {
      em.lock(v, LockModeType.PESSIMISTIC_WRITE);
      em.flush();
      em.refresh(v, LockModeType.PESSIMISTIC_WRITE);
    }
    // Query com EntityGraph recarrega os relacionamentos necessários ao DTO após refresh.
    return repository
        .findOneById(id)
        .orElseThrow(() -> new RecursoNaoEncontradoException("Venda não encontrada"));
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
