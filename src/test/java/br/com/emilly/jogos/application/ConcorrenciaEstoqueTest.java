package br.com.emilly.jogos.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.support.ComercialFixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Sem transação externa de teste: preparação confirmada, duas conexões e leitura final JDBC. */
@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@ActiveProfiles("test")
class ConcorrenciaEstoqueTest {
  @Autowired org.springframework.test.web.servlet.MockMvc mvc;
  @Autowired EntityManager em;
  @Autowired PlatformTransactionManager manager;
  @Autowired JdbcTemplate jdbc;
  @Autowired VendaService vendas;
  @Autowired JogoService jogos;
  @MockitoSpyBean BloqueioEstoque bloqueio;
  ComercialFixture.Dados f;
  List<Long> saleIds = new ArrayList<>();
  List<CountDownLatch> releases = new ArrayList<>();
  ExecutorService executor;
  volatile Consumer<Jogo> before = j -> {};
  volatile Consumer<Jogo> after = j -> {};

  record Resultado(boolean sucesso, String mensagem) {}

  TransactionTemplate tx() {
    return new TransactionTemplate(manager);
  }

  void await(CountDownLatch latch) {
    try {
      assertTrue(latch.await(8, TimeUnit.SECONDS), "Sinal concorrente não chegou");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AssertionError(e);
    }
  }

  CountDownLatch release() {
    var l = new CountDownLatch(1);
    releases.add(l);
    return l;
  }

  @BeforeAll
  static void iniciarEvidencias() throws Exception {
    Files.createDirectories(Path.of("target"));
    Files.writeString(Path.of("target/aula11-concorrencia.jsonl"), "");
  }

  @BeforeEach
  void preparar() {
    assertEquals(
        "jogos_test",
        jdbc.execute(
            (org.springframework.jdbc.core.ConnectionCallback<String>) c -> c.getCatalog()));
    assertEquals("read committed", jdbc.queryForObject("SHOW transaction_isolation", String.class));
    f = ComercialFixture.criar(em, tx());
    executor = Executors.newFixedThreadPool(2);
    tx().executeWithoutResult(
            s -> {
              em.find(Jogo.class, f.jogo1()).retirarUnidades(new BigDecimal("5"));
              em.find(Jogo.class, f.jogo2()).receberUnidades(new BigDecimal("4"));
            });
    doAnswer(
            inv -> {
              Jogo j = inv.getArgument(0);
              before.accept(j);
              inv.callRealMethod();
              after.accept(j);
              return null;
            })
        .when(
            org.springframework.test.util.AopTestUtils.<BloqueioEstoque>getUltimateTargetObject(
                bloqueio))
        .adquirir(any(Jogo.class));
  }

  @AfterEach
  void limparFixturesProprias() throws Exception {
    releases.forEach(CountDownLatch::countDown);
    executor.shutdownNow();
    assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
    tx().executeWithoutResult(
            s -> {
              for (Long id : saleIds) jdbc.update("DELETE FROM venda WHERE id=?", id);
              jdbc.update("DELETE FROM jogo WHERE id IN (?,?)", f.jogo1(), f.jogo2());
              jdbc.update("DELETE FROM genero_jogo WHERE id=?", f.genero());
              jdbc.update("DELETE FROM cliente WHERE id=?", f.cliente());
              jdbc.update("DELETE FROM colaborador WHERE id=?", f.vendedor());
              jdbc.update("DELETE FROM pessoa WHERE id=?", f.pessoa());
            });
  }

  Future<Resultado> iniciar(Runnable action) {
    return executor.submit(
        () -> {
          try {
            return tx().execute(
                    s -> {
                      jdbc.execute("SET LOCAL lock_timeout='5s'");
                      jdbc.execute("SET LOCAL statement_timeout='7s'");
                      action.run();
                      return new Resultado(true, null);
                    });
          } catch (IllegalArgumentException | IllegalStateException e) {
            return new Resultado(false, e.getMessage());
          }
        });
  }

  Resultado get(Future<Resultado> future) throws Exception {
    return future.get(10, TimeUnit.SECONDS);
  }

  Long venda(boolean inversa, boolean dois) {
    var v = vendas.cadastrar(f.cliente(), f.vendedor());
    saleIds.add(v.getId());
    if (dois && inversa) vendas.adicionarItem(v.getId(), f.jogo2(), new BigDecimal("4"));
    vendas.adicionarItem(v.getId(), f.jogo1(), new BigDecimal("4"));
    if (dois && !inversa) vendas.adicionarItem(v.getId(), f.jogo2(), new BigDecimal("4"));
    return v.getId();
  }

  void barreira() {
    var barrier = new CyclicBarrier(2);
    before =
        j -> {
          if (j.getId().equals(f.jogo1()))
            try {
              barrier.await(8, TimeUnit.SECONDS);
            } catch (Exception e) {
              throw new AssertionError(e);
            }
        };
  }

  void saldo(Long id, String value) {
    assertEquals(
        0,
        jdbc.queryForObject(
                "SELECT unidades_disponiveis FROM jogo WHERE id=?", BigDecimal.class, id)
            .compareTo(new BigDecimal(value)));
  }

  String estado(Long id) {
    return jdbc.queryForObject("SELECT status FROM venda WHERE id=?", String.class, id);
  }

  void umVencedor(Resultado a, Resultado b) {
    assertNotEquals(a.sucesso(), b.sucesso());
    assertEquals("Unidades disponíveis insuficientes", a.sucesso() ? b.mensagem() : a.mensagem());
  }

  void evidencia(String cenario, Long... ids) throws Exception {
    var result = new LinkedHashMap<String, Object>();
    result.put("cenario", cenario);
    result.put(
        "saldoA",
        jdbc.queryForObject(
            "SELECT unidades_disponiveis FROM jogo WHERE id=?", BigDecimal.class, f.jogo1()));
    result.put(
        "saldoB",
        jdbc.queryForObject(
            "SELECT unidades_disponiveis FROM jogo WHERE id=?", BigDecimal.class, f.jogo2()));
    result.put("estados", Arrays.stream(ids).map(this::estado).toList());
    Files.writeString(
        Path.of("target/aula11-concorrencia.jsonl"),
        new ObjectMapper().writeValueAsString(result) + "\n",
        StandardOpenOption.APPEND);
  }

  @RepeatedTest(3)
  void duasVendasDisputamCincoUnidades() throws Exception {
    Long a = venda(false, false), b = venda(false, false);
    barreira();
    var fa = iniciar(() -> vendas.finalizar(a));
    var fb = iniciar(() -> vendas.finalizar(b));
    umVencedor(get(fa), get(fb));
    saldo(f.jogo1(), "1");
    assertEquals(Set.of("ABERTA", "FINALIZADA"), Set.of(estado(a), estado(b)));
    evidencia("duas vendas 4+4 sobre saldo5", a, b);
  }

  @Test
  void duasSaidasManuaisNaoConsomemMaisQueSaldo() throws Exception {
    barreira();
    var a = iniciar(() -> jogos.saida(f.jogo1(), new BigDecimal("4")));
    var b = iniciar(() -> jogos.saida(f.jogo1(), new BigDecimal("4")));
    umVencedor(get(a), get(b));
    saldo(f.jogo1(), "1");
    evidencia("duas saídas manuais");
  }

  @Test
  void vendaESaidaManualCompartilhamMesmaPolitica() throws Exception {
    Long id = venda(false, false);
    barreira();
    var a = iniciar(() -> vendas.finalizar(id));
    var b = iniciar(() -> jogos.saida(f.jogo1(), new BigDecimal("4")));
    var va = get(a);
    umVencedor(va, get(b));
    saldo(f.jogo1(), "1");
    assertEquals(va.sucesso() ? "FINALIZADA" : "ABERTA", estado(id));
    evidencia("venda contra saída manual", id);
  }

  @Test
  void entradasAcumulamSemAtualizacaoPerdida() throws Exception {
    barreira();
    var a = iniciar(() -> jogos.entrada(f.jogo1(), new BigDecimal("2")));
    var b = iniciar(() -> jogos.entrada(f.jogo1(), new BigDecimal("3")));
    assertTrue(get(a).sucesso());
    assertTrue(get(b).sucesso());
    saldo(f.jogo1(), "10");
    evidencia("entradas 2+3");
  }

  @Test
  void entradaEVendaAcumulamCorretamente() throws Exception {
    Long id = venda(false, false);
    barreira();
    var a = iniciar(() -> jogos.entrada(f.jogo1(), new BigDecimal("3")));
    var b = iniciar(() -> vendas.finalizar(id));
    assertTrue(get(a).sucesso());
    assertTrue(get(b).sucesso());
    saldo(f.jogo1(), "4");
    evidencia("entrada e venda", id);
  }

  @Test
  void metodoLegadoDeEntradaTambemAdquireLock() throws Exception {
    barreira();
    var a = iniciar(() -> jogos.receberUnidades(f.jogo1(), new BigDecimal("3")));
    var b = iniciar(() -> jogos.saida(f.jogo1(), new BigDecimal("4")));
    assertTrue(get(a).sucesso());
    assertTrue(get(b).sucesso());
    saldo(f.jogo1(), "4");
    evidencia("entrada legada e saída");
  }

  @Test
  void itensInversosAdquiremLocksPorIdCrescente() throws Exception {
    Long a = venda(true, true), b = venda(false, true);
    barreira();
    var order = new ConcurrentHashMap<String, List<Long>>();
    after =
        j ->
            order
                .computeIfAbsent(Thread.currentThread().getName(), k -> new ArrayList<>())
                .add(j.getId());
    var fa = iniciar(() -> vendas.finalizar(a));
    var fb = iniciar(() -> vendas.finalizar(b));
    umVencedor(get(fa), get(fb));
    for (var ids : order.values()) {
      assertEquals(ids.stream().sorted().toList(), ids);
      assertEquals(f.jogo1(), ids.getFirst());
    }
    saldo(f.jogo1(), "1");
    saldo(f.jogo2(), "1");
    evidencia("itens inversos, locks crescentes", a, b);
  }

  @Test
  void mesmaVendaNaoPodeSerFinalizadaDuasVezesEmParalelo() throws Exception {
    Long id = venda(false, false);
    var start = new CountDownLatch(1);
    var a =
        iniciar(
            () -> {
              await(start);
              vendas.finalizar(id);
            });
    var b =
        iniciar(
            () -> {
              await(start);
              vendas.finalizar(id);
            });
    start.countDown();
    var ra = get(a);
    var rb = get(b);
    assertNotEquals(ra.sucesso(), rb.sucesso());
    assertEquals(
        "Somente vendas abertas podem ser alteradas", ra.sucesso() ? rb.mensagem() : ra.mensagem());
    saldo(f.jogo1(), "1");
    evidencia("finalização duplicada", id);
  }

  @Test
  void refreshReleSaldoMesmoComEntidadeJaCarregada() throws Exception {
    var loaded = new CountDownLatch(1);
    var proceed = release();
    var stale =
        iniciar(
            () -> {
              assertEquals(
                  new BigDecimal("5.000"), em.find(Jogo.class, f.jogo1()).getUnidadesDisponiveis());
              loaded.countDown();
              await(proceed);
              jogos.saida(f.jogo1(), new BigDecimal("4"));
            });
    await(loaded);
    jogos.saida(f.jogo1(), new BigDecimal("4"));
    proceed.countDown();
    assertFalse(get(stale).sucesso());
    saldo(f.jogo1(), "1");
    evidencia("contexto previamente carregado");
  }

  @Test
  void refreshNaoDescartaAlteracoesPendentesNaMesmaTransacao() {
    tx().executeWithoutResult(
            s -> {
              em.find(Jogo.class, f.jogo1()).alterarPreco(new BigDecimal("120"));
              jogos.entrada(f.jogo1(), new BigDecimal("2"));
              jogos.saida(f.jogo1(), BigDecimal.ONE);
              jogos.entrada(f.jogo1(), BigDecimal.ONE);
            });
    saldo(f.jogo1(), "7");
    assertEquals(
        0,
        jdbc.queryForObject("SELECT preco FROM jogo WHERE id=?", BigDecimal.class, f.jogo1())
            .compareTo(new BigDecimal("120")));
  }

  @Test
  void atualizacaoDeCadastroNaoReenviaSaldoAntigo() throws Exception {
    var loaded = new CountDownLatch(1);
    var proceed = release();
    var update =
        iniciar(
            () -> {
              var j = em.find(Jogo.class, f.jogo1());
              assertEquals(new BigDecimal("5.000"), j.getUnidadesDisponiveis());
              loaded.countDown();
              await(proceed);
              jogos.alterar(
                  j.getId(),
                  "Nome alterado",
                  new BigDecimal("120"),
                  BigDecimal.ONE,
                  f.genero(),
                  null);
            });
    await(loaded);
    jogos.saida(f.jogo1(), new BigDecimal("4"));
    proceed.countDown();
    assertTrue(get(update).sucesso());
    saldo(f.jogo1(), "1");
    assertEquals(
        "Nome alterado",
        jdbc.queryForObject("SELECT nome FROM jogo WHERE id=?", String.class, f.jogo1()));
    evidencia("cadastro concorrente não sobrescreve saldo");
  }

  @Test
  void consultasContinuamLivresEnquantoEscritorMantemLock() throws Exception {
    var locked = new CountDownLatch(1);
    var proceed = release();
    var holder =
        iniciar(
            () -> {
              var j = em.find(Jogo.class, f.jogo1());
              bloqueio.adquirir(j);
              j.receberUnidades(BigDecimal.ONE);
              em.flush();
              locked.countDown();
              await(proceed);
            });
    await(locked);
    try {
      var reader = executor.submit(() -> jogos.buscarPorId(f.jogo1()).getUnidadesDisponiveis());
      assertEquals(new BigDecimal("5.000"), reader.get(2, TimeUnit.SECONDS));
    } finally {
      proceed.countDown();
    }
    assertTrue(get(holder).sucesso());
    saldo(f.jogo1(), "6");
  }

  @Test
  void esperaRealVisivelNoPostgresELiberadaNoCommit() throws Exception {
    var locked = new CountDownLatch(1);
    var proceed = release();
    var waiterStarted = new CountDownLatch(1);
    var waiterPid = new java.util.concurrent.atomic.AtomicInteger();
    after =
        j -> {
          if (Thread.currentThread().getName().endsWith("thread-1")) {
            locked.countDown();
            await(proceed);
          }
        };
    var holder = iniciar(() -> jogos.entrada(f.jogo1(), new BigDecimal("2")));
    await(locked);
    var waiter =
        iniciar(
            () -> {
              waiterPid.set(jdbc.queryForObject("SELECT pg_backend_pid()", Integer.class));
              waiterStarted.countDown();
              jogos.saida(f.jogo1(), new BigDecimal("4"));
            });
    await(waiterStarted);
    try {
      long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
      boolean waiting = false;
      while (System.nanoTime() < deadline) {
        waiting =
            Boolean.TRUE.equals(
                jdbc.queryForObject(
                    "SELECT EXISTS(SELECT 1 FROM pg_stat_activity WHERE pid=? AND"
                        + " wait_event_type='Lock' AND cardinality(pg_blocking_pids(pid))>0)",
                    Boolean.class,
                    waiterPid.get()));
        if (waiting) break;
        Thread.sleep(10);
      }
      assertTrue(waiting, "PostgreSQL deve observar a sessão aguardando lock");
      assertFalse(waiter.isDone());
    } finally {
      proceed.countDown();
    }
    assertTrue(get(holder).sucesso());
    assertTrue(get(waiter).sucesso());
    saldo(f.jogo1(), "3");
    evidencia("espera observada em pg_stat_activity/pg_blocking_pids");
  }

  @Test
  void timeoutRealRetorna409SemSaldoParcial() throws Exception {
    var locked = new CountDownLatch(1);
    var proceed = release();
    var holder =
        iniciar(
            () -> {
              bloqueio.adquirir(em.find(Jogo.class, f.jogo1()));
              locked.countDown();
              await(proceed);
            });
    await(locked);
    try {
      assertThrows(
          org.springframework.transaction.UnexpectedRollbackException.class,
          () ->
              tx().executeWithoutResult(
                      s -> {
                        jdbc.execute("SET LOCAL lock_timeout='150ms'");
                        try {
                          mvc.perform(
                                  org.springframework.test.web.servlet.request
                                      .MockMvcRequestBuilders.post(
                                          "/api/jogos/{id}/estoque/saidas", f.jogo1())
                                      .contentType("application/json")
                                      .content("{\"quantidade\":4}"))
                              .andExpect(
                                  org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                      .status()
                                      .isConflict())
                              .andExpect(
                                  org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                      .jsonPath("$.message")
                                      .value("Conflito concorrente; operação não concluída"))
                              .andExpect(
                                  org.springframework.test.web.servlet.result.MockMvcResultMatchers
                                      .jsonPath("$.fields")
                                      .isEmpty());
                        } catch (Exception e) {
                          throw new AssertionError(e);
                        }
                      }));
    } finally {
      proceed.countDown();
    }
    assertTrue(get(holder).sucesso());
    saldo(f.jogo1(), "5");
    evidencia("timeout real HTTP409 sem baixa");
  }

  @Test
  void itemNaoPodeSerIncluidoEnquantoFinalizacaoConfirmaMesmaVenda() throws Exception {
    Long id = venda(false, false);
    var locked = new CountDownLatch(1);
    var proceed = release();
    after =
        j -> {
          locked.countDown();
          await(proceed);
        };
    var finalizacao = iniciar(() -> vendas.finalizar(id));
    await(locked);
    var started = new CountDownLatch(1);
    var inclusao =
        iniciar(
            () -> {
              started.countDown();
              vendas.adicionarItem(id, f.jogo2(), BigDecimal.ONE);
            });
    await(started);
    proceed.countDown();
    assertTrue(get(finalizacao).sucesso());
    assertFalse(get(inclusao).sucesso());
    assertEquals(
        1,
        jdbc.queryForObject("SELECT count(*) FROM item_venda WHERE venda_id=?", Integer.class, id));
    saldo(f.jogo1(), "1");
    saldo(f.jogo2(), "5");
  }

  @Test
  void diagnosticoDistingueDeadlockRealDeEsperaNormal() throws Exception {
    // Experimento deliberado fora da política ordenada de produção, somente fixtures próprias.
    var ambos = new CyclicBarrier(2);
    java.util.function.Function<Boolean, String> ciclo =
        inverso -> {
          try {
            tx().executeWithoutResult(
                    s -> {
                      jdbc.execute("SET LOCAL lock_timeout='4s'");
                      jdbc.execute("SET LOCAL statement_timeout='6s'");
                      jdbc.queryForObject(
                          "SELECT id FROM jogo WHERE id=? FOR UPDATE",
                          Long.class,
                          inverso ? f.jogo2() : f.jogo1());
                      try {
                        ambos.await(3, TimeUnit.SECONDS);
                      } catch (Exception e) {
                        throw new AssertionError(e);
                      }
                      jdbc.queryForObject(
                          "SELECT id FROM jogo WHERE id=? FOR UPDATE",
                          Long.class,
                          inverso ? f.jogo1() : f.jogo2());
                    });
            return "COMMIT";
          } catch (org.springframework.dao.DataAccessException e) {
            Throwable cause = e;
            while (cause != null) {
              if (cause instanceof java.sql.SQLException sql) return sql.getSQLState();
              cause = cause.getCause();
            }
            throw e;
          }
        };
    var a = executor.submit(() -> ciclo.apply(false));
    var b = executor.submit(() -> ciclo.apply(true));
    assertEquals(
        Set.of("COMMIT", "40P01"),
        Set.of(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS)));
    saldo(f.jogo1(), "5");
    saldo(f.jogo2(), "5");
    evidencia("experimento diagnóstico: ciclo deliberado SQLState40P01; saldos preservados");
  }

  @Test
  void experimentoSemLocksDemonstraAtualizacaoPerdida() throws Exception {
    Long a = venda(false, false), b = venda(false, false);
    var read = new CyclicBarrier(2);
    java.util.function.Function<Long, Runnable> unsafe =
        id ->
            () -> {
              var v = em.find(Venda.class, id);
              var j = em.find(Jogo.class, f.jogo1());
              assertEquals(new BigDecimal("5.000"), j.getUnidadesDisponiveis());
              try {
                read.await(8, TimeUnit.SECONDS);
              } catch (Exception e) {
                throw new AssertionError(e);
              }
              j.retirarUnidades(new BigDecimal("4"));
              v.finalizar();
            };
    // Apenas fixtures próprias em jogos_test; não usa nem altera o serviço protegido.
    var fa = iniciar(unsafe.apply(a));
    var fb = iniciar(unsafe.apply(b));
    assertTrue(get(fa).sucesso());
    assertTrue(get(fb).sucesso());
    saldo(f.jogo1(), "1");
    assertEquals("FINALIZADA", estado(a));
    assertEquals("FINALIZADA", estado(b));
    evidencia("experimento controlado sem política: vende8 de5", a, b);
  }
}
