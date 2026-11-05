package br.com.emilly.jogos.application;

import static org.junit.jupiter.api.Assertions.*;

import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.repository.*;
import br.com.emilly.jogos.support.*;
import jakarta.persistence.EntityManager;
import java.math.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/** Sem transação de teste: commit de preparação, falha do service e leitura independente. */
@SpringBootTest
@ActiveProfiles("test")
class VendaServiceTest {
  @Autowired EntityManager em;
  @Autowired TransactionTemplate tx;
  @Autowired VendaService vendas;
  @Autowired JogoRepository jogos;
  @Autowired VendaRepository repository;
  @Autowired PessoaService pessoas;
  @Autowired ColaboradorService colaboradores;
  @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
  @Autowired jakarta.persistence.EntityManagerFactory factory;
  ComercialFixture.Dados dados;

  @BeforeEach
  void preparar() {
    dados = ComercialFixture.criar(em, tx);
  }

  private Long abrir() {
    return vendas.cadastrar(dados.cliente(), dados.vendedor()).getId();
  }

  private void saldo(Long jogo, String esperado) {
    assertEquals(
        0,
        jogos
            .findById(jogo)
            .orElseThrow()
            .getUnidadesDisponiveis()
            .compareTo(new BigDecimal(esperado)));
  }

  @Test
  void rollbackRealDoSegundoItemPreservaAmbosSaldosEVendaAberta() {
    Long id = abrir();
    vendas.adicionarItem(id, dados.jogo1(), new BigDecimal("2"));
    vendas.adicionarItem(id, dados.jogo2(), new BigDecimal("2"));
    var independente = new TransactionTemplate(tx.getTransactionManager());
    independente.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    // A transação externa existe somente para a prova: após capturar a falha do
    // service, força o flush e observa a baixa temporária por JDBC. O service
    // deve ter marcado a transação compartilhada para rollback.
    assertThrows(
        org.springframework.transaction.UnexpectedRollbackException.class,
        () ->
            independente.execute(
                status -> {
                  assertThrows(IllegalArgumentException.class, () -> vendas.finalizar(id));
                  assertTrue(
                      status.isRollbackOnly(), "A fronteira do service precisa marcar rollback");
                  em.flush();
                  assertEquals(
                      0,
                      jdbc.queryForObject(
                              "SELECT unidades_disponiveis FROM jogo WHERE id=?",
                              BigDecimal.class,
                              dados.jogo1())
                          .compareTo(new BigDecimal("8")));
                  return null;
                }));
    independente.executeWithoutResult(
        s -> {
          em.clear();
          saldo(dados.jogo1(), "10");
          saldo(dados.jogo2(), "1");
          assertEquals(StatusVenda.ABERTA, repository.findOneById(id).orElseThrow().getStatus());
        });
  }

  @Test
  void paginaResumoExecutaNoBancoSemBuscarColecoes() {
    for (int i = 0; i < 3; i++) {
      Long id = abrir();
      vendas.adicionarItem(id, dados.jogo1(), BigDecimal.ONE);
    }
    var statistics = factory.unwrap(org.hibernate.SessionFactory.class).getStatistics();
    boolean enabled = statistics.isStatisticsEnabled();
    statistics.setStatisticsEnabled(true);
    statistics.clear();
    try {
      var pagina =
          vendas.pesquisar(
              null,
              dados.cliente(),
              null,
              null,
              null,
              org.springframework.data.domain.PageRequest.of(0, 1));
      assertEquals(3, pagina.getTotalElements());
      assertEquals(1, pagina.getContent().size());
      for (var venda : pagina) {
        assertFalse(factory.getPersistenceUnitUtil().isLoaded(venda, "itens"));
        assertNotNull(new br.com.emilly.jogos.mapper.VendaMapper().toResumo(venda).clienteNome());
      }
      assertEquals(0, statistics.getCollectionFetchCount());
      assertTrue(statistics.getPrepareStatementCount() <= 2);
    } finally {
      statistics.setStatisticsEnabled(enabled);
    }
  }

  @Test
  void snapshotPersistidoEFinalizacaoBaixamUmaUnicaVez() {
    Long id = abrir();
    vendas.adicionarItem(id, dados.jogo1(), new BigDecimal("2"));
    tx.executeWithoutResult(
        s -> jogos.findById(dados.jogo1()).orElseThrow().alterarPreco(new BigDecimal("120.00")));
    var venda = vendas.buscar(id);
    assertEquals(new BigDecimal("100.00"), venda.getItens().getFirst().getValorUnitario());
    assertEquals(new BigDecimal("200.00"), venda.calcularTotal());
    saldo(dados.jogo1(), "10");
    vendas.finalizar(id);
    saldo(dados.jogo1(), "8");
    assertEquals(StatusVenda.FINALIZADA, vendas.buscar(id).getStatus());
    assertThrows(IllegalStateException.class, () -> vendas.finalizar(id));
    saldo(dados.jogo1(), "8");
  }

  @Test
  void participanteInativoImpedeFinalizacaoMasMantemHistorico() {
    var outroVendedor = ComercialFixture.criar(em, tx);
    Long id = vendas.cadastrar(dados.cliente(), outroVendedor.vendedor()).getId();
    vendas.adicionarItem(id, dados.jogo1(), BigDecimal.ONE);
    pessoas.status(dados.pessoa(), Status.INATIVO);
    assertThrows(IllegalArgumentException.class, () -> vendas.finalizar(id));
    assertNotNull(vendas.buscar(id));
    saldo(dados.jogo1(), "10");
  }

  @Test
  void somenteVendedorInativoTambemImpedeFinalizacao() {
    var outroVendedor = ComercialFixture.criar(em, tx);
    Long id = vendas.cadastrar(dados.cliente(), outroVendedor.vendedor()).getId();
    vendas.adicionarItem(id, dados.jogo1(), BigDecimal.ONE);
    pessoas.status(outroVendedor.pessoa(), Status.INATIVO);
    assertThrows(IllegalArgumentException.class, () -> vendas.finalizar(id));
    saldo(dados.jogo1(), "10");
    assertEquals(StatusVenda.ABERTA, vendas.buscar(id).getStatus());
  }

  @Test
  void funcaoAlteradaImpedeFinalizacao() {
    Long id = abrir();
    vendas.adicionarItem(id, dados.jogo1(), BigDecimal.ONE);
    var c = colaboradores.buscar(dados.vendedor());
    colaboradores.alterar(c.getId(), c.getMatricula(), FuncaoColaborador.ESTOQUISTA);
    assertThrows(IllegalArgumentException.class, () -> vendas.finalizar(id));
    saldo(dados.jogo1(), "10");
  }

  @Test
  void jogoInativoImpedeFinalizacao() {
    Long id = abrir();
    vendas.adicionarItem(id, dados.jogo1(), BigDecimal.ONE);
    tx.executeWithoutResult(s -> jogos.findById(dados.jogo1()).orElseThrow().inativar());
    assertThrows(IllegalArgumentException.class, () -> vendas.finalizar(id));
    saldo(dados.jogo1(), "10");
  }

  @Test
  void cancelamentoMantemSaldo() {
    Long id = abrir();
    vendas.adicionarItem(id, dados.jogo1(), BigDecimal.ONE);
    vendas.cancelar(id);
    assertEquals(StatusVenda.CANCELADA, vendas.buscar(id).getStatus());
    saldo(dados.jogo1(), "10");
    assertThrows(
        IllegalStateException.class, () -> vendas.adicionarItem(id, dados.jogo2(), BigDecimal.ONE));
  }
}
