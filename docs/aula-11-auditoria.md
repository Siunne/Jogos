# Aula 11 — Matriz de conformidade

Material integral: [Aula11 oficial](https://github.com/jeffersonarpasserini/suporteos2026/blob/main/docs/11aula/11aula.md). Esta matriz própria não reutiliza a contagem102 da Aula10. Evidências são arquivos/testes efetivos; a rubrica registra suporte técnico ao nível avançado, sem atribuir nota. Avaliação oral e participação não são presumidas.

Total: 79 linhas; {"ATENDIDO":78,"NÃO VERIFICADO":1}. PARCIAL:0; NÃO ATENDIDO:0.

Caminhos de classes: `src/main/java/br/com/emilly/jogos/`; testes: `src/test/java/br/com/emilly/jogos/`. Os métodos do teste concorrente ficam em `application/ConcorrenciaEstoqueTest.java`. Consulte também [guia](aula-11-concorrencia.md) e [evidências](aula-11-evidencias.md).

| ID | Requisito / referência oficial | Evidência concreta | Situação |
|---|---|---|---|
| 1 | Pré-requisitos: marco Aula10, main, remoto e árvore inicial limpa | Diagnóstico no guia; target/aula11-baseline-hashes.json; HEAD 0a7b201 | ATENDIDO |
| 2 | Suíte anterior executada antes da alteração | target/aula11-baseline-maven.log:178; baseline-node:14; baseline-newman:81/135 | ATENDIDO |
| 3 | §1 / RA1: explicar a atualização perdida | aula-11-concorrencia.md fundamentos; ConcorrenciaEstoqueTest.experimentoSemLocksDemonstraAtualizacaoPerdida | ATENDIDO |
| 4 | §2 / RA2: diferenciar atomicidade, consistência, isolamento e durabilidade | aula-11-concorrencia.md fundamentos; VendaServiceTest.rollbackRealDoSegundoItemPreservaAmbosSaldosEVendaAberta | ATENDIDO |
| 5 | §2.1: manter isolamento global; confirmar READ COMMITTED | ConcorrenciaEstoqueTest.preparar: SHOW transaction_isolation; nenhum arquivo de configuração alterado | ATENDIDO |
| 6 | §3 / RA3: explicar MVCC e leituras concorrentes | aula-11-concorrencia.md; ConcorrenciaEstoqueTest.consultasContinuamLivresEnquantoEscritorMantemLock | ATENDIDO |
| 7 | §4 / RA4: comparar otimista e pessimista | aula-11-concorrencia.md fundamentos, custos e alternativas | ATENDIDO |
| 8 | §4.3 / RA5: justificar PESSIMISTIC_WRITE para estoque de jogos | aula-11-concorrencia.md invariante e atividade autônoma | ATENDIDO |
| 9 | §5 / RA7: ordenar recursos e explicar ciclos de deadlock | VendaService.finalizar; ConcorrenciaEstoqueTest.itensInversosAdquiremLocksPorIdCrescente | ATENDIDO |
| 10 | §6–7: ligar transação, lock, refresh, baixa e commit | BloqueioEstoque.adquirir; VendaService.finalizar; linha do tempo no guia | ATENDIDO |
| 11 | Checkpoint1: identificar código vulnerável real e todos os caminhos | aula-11-concorrencia.md diagnóstico: finalizar, entrada, saida, receberUnidades | ATENDIDO |
| 12 | Checkpoint2 / RA6: EntityManager e lock dentro da transação | BloqueioEstoque.adquirir MANDATORY; VendaService.finalizar e JogoService @Transactional | ATENDIDO |
| 13 | Checkpoint3: participantes e venda aberta antes das baixas | Venda.validarFinalizacao; VendaServiceTest participantes inativos/função/jogo; mesmaVendaNaoPodeSerFinalizadaDuasVezesEmParalelo | ATENDIDO |
| 14 | Checkpoint3: lock antes da conferência de status/saldo | VendaService.finalizar: bloqueio.adquirir precede getStatus e retirarUnidades | ATENDIDO |
| 15 | Checkpoint3: recarregar saldo confirmado mesmo com entidade já carregada | ConcorrenciaEstoqueTest.refreshReleSaldoMesmoComEntidadeJaCarregada | ATENDIDO |
| 16 | Checkpoint3: ordem crescente independente da inclusão | ConcorrenciaEstoqueTest.itensInversosAdquiremLocksPorIdCrescente; rastreio após locks reais | ATENDIDO |
| 17 | Checkpoint3: finalizar somente após todos os itens e rollback integral | VendaService.finalizar; VendaServiceTest.rollbackRealDoSegundoItemPreservaAmbosSaldosEVendaAberta | ATENDIDO |
| 18 | Adaptação JPA: preservar alterações pendentes ao usar refresh | ConcorrenciaEstoqueTest.refreshNaoDescartaAlteracoesPendentesNaMesmaTransacao; lock/flush/refresh e skip se já bloqueado | ATENDIDO |
| 19 | Adaptação JPA: cadastro não sobrescreve estoque concorrente | Jogo @DynamicUpdate; ConcorrenciaEstoqueTest.atualizacaoDeCadastroNaoReenviaSaldoAntigo | ATENDIDO |
| 20 | Adaptação de estado: dois comandos não finalizam a mesma venda | ConcorrenciaEstoqueTest.mesmaVendaNaoPodeSerFinalizadaDuasVezesEmParalelo; VendaService.buscarParaAlterar | ATENDIDO |
| 21 | Adaptação de estado: item não entra após finalização concorrente | ConcorrenciaEstoqueTest.itemNaoPodeSerIncluidoEnquantoFinalizacaoConfirmaMesmaVenda | ATENDIDO |
| 22 | Checkpoint4: entrada manual utiliza a mesma política | JogoService.entrada; ConcorrenciaEstoqueTest.entradasAcumulamSemAtualizacaoPerdida | ATENDIDO |
| 23 | Checkpoint4: saída manual utiliza a mesma política | JogoService.saida; ConcorrenciaEstoqueTest.duasSaidasManuaisNaoConsomemMaisQueSaldo | ATENDIDO |
| 24 | Caminho legado receberUnidades não fica vulnerável | JogoService.receberUnidades; ConcorrenciaEstoqueTest.metodoLegadoDeEntradaTambemAdquireLock | ATENDIDO |
| 25 | Concorrência cruzada entre venda e movimentação | ConcorrenciaEstoqueTest.vendaESaidaManualCompartilhamMesmaPolitica; entradaEVendaAcumulamCorretamente | ATENDIDO |
| 26 | §11.2: leitura, filtros e páginas sem lock pessimista | JogoService.buscarPorId/pesquisar e VendaService.buscar/pesquisar; teste consulta durante lock | ATENDIDO |
| 27 | §11.2: abertura e inclusão não reservam nem bloqueiam estoque | VendaService.cadastrar/adicionarItem; apenas linha de Venda coordena comandos, explicado no guia | ATENDIDO |
| 28 | §12: sem changelog nem alteração no master | Hashes das migrações 001–005 e master comparados ao baseline; target/aula11-preservacao.json | ATENDIDO |
| 29 | Checkpoint5: OpenAPI descreve finalização e movimentos concorrentes | VendaController.finalizar; OpenApiConcorrenciaConfig; OpenApiConcorrenciaTest | ATENDIDO |
| 30 | Checkpoint5: preservar contratos e sem mudar rotas/DTOs | docs/aula-11-openapi.yaml; históricos09/10 preservados; 38 operações/24 caminhos/30 schemas/8 tags | ATENDIDO |
| 31 | Checkpoint6 / RA8: PostgreSQL real, duas tarefas e duas transações | ConcorrenciaEstoqueTest sem @Transactional externo; PlatformTransactionManager; catalog jogos_test confirmado | ATENDIDO |
| 32 | Checkpoint6: fixtures confirmadas antes de iniciar workers | ConcorrenciaEstoqueTest.preparar e venda; TransactionTemplate antes de iniciar | ATENDIDO |
| 33 | Checkpoint6: gates, Future e prazos limitados, sem sleep para disparar disputa | ConcorrenciaEstoqueTest.barreira/iniciar/get/await; polling limitado apenas para diagnóstico | ATENDIDO |
| 34 | Checkpoint6: exatamente uma finalizada e outra aberta | ConcorrenciaEstoqueTest.duasVendasDisputamCincoUnidades repetido3; leitura JDBC independente | ATENDIDO |
| 35 | Checkpoint6: saldo final1, sem negativo nem exigência de vencedora específica | ConcorrenciaEstoqueTest.umVencedor/saldo/estado; compara conjunto de estados | ATENDIDO |
| 36 | Checkpoint6: testes encerram executor e limpam somente próprias fixtures | ConcorrenciaEstoqueTest.limparFixturesProprias; awaitTermination; releases no finally | ATENDIDO |
| 37 | §15: suíte completa executada e relatórios reais revisados | target/aula11-maven-final.log; aula-11-evidencias.md | ATENDIDO |
| 38 | §16: experimento opcional de bloqueio em duas conexões | Automatizado em esperaRealVisivelNoPostgresELiberadaNoCommit; consoles manuais opcionais não alegados | ATENDIDO |
| 39 | §17 / RA9: observar espera e bloqueadores reais | ConcorrenciaEstoqueTest.esperaRealVisivelNoPostgresELiberadaNoCommit; pg_stat_activity/pg_blocking_pids | ATENDIDO |
| 40 | §17: distinguir timeout de saldo insuficiente | ConcorrenciaEstoqueTest.timeoutRealRetorna409SemSaldoParcial; SQLState55P03; HTTP400 de saldo continua distinto | ATENDIDO |
| 41 | §17: diagnóstico e consultas pg_locks sem dados pessoais | aula-11-concorrencia.md consultas e classificação de SQLStates; filtros de privacidade preservados | ATENDIDO |
| 42 | §17: distinguir deadlock real de espera legítima | ConcorrenciaEstoqueTest.diagnosticoDistingueDeadlockRealDeEsperaNormal: ciclo isolado, SQLState40P01 e um commit | ATENDIDO |
| 43 | §18.1: transações curtas, sem chamada externa nem espera no código real | Services locais; gates somente em src/test; duração explicada no guia | ATENDIDO |
| 44 | §18.2: coordenação localizada, não global | BloqueioEstoque por Jogo; Venda por comando; consultas sem PESSIMISTIC_WRITE | ATENDIDO |
| 45 | §18.3: sem retry cego, sem filas ou reservas | Nenhuma dependência ou configuração acrescentada; exceção propaga e causa rollback | ATENDIDO |
| 46 | §18.4: sem detalhes internos na resposta de lock | ApiExceptionHandler.tratarConcorrencia; HTTP409 de timeout real; cinco testes de privacidade | ATENDIDO |
| 47 | §18.5: registrar métricas futuras sem fingir implementação | aula-11-concorrencia.md diagnóstico: duração, espera, abortos, recusas e taxa de erro | ATENDIDO |
| 48 | §19 / RA10: transferência da invariante para loja de jogos | aula-11-concorrencia.md invariante e autonomia; Venda/ItemVenda/Jogo reais, preço histórico preservado | ATENDIDO |
| 49 | §19: oito entregáveis da transferência | aula-11-concorrencia.md cenário, sequência, escolha, implementação, teste, estado, risco e ausência de changelog | ATENDIDO |
| 50 | §20: linha do tempo, fronteiras e ponto do lock | aula-11-concorrencia.md Mermaid e explicação de menos de300 palavras | ATENDIDO |
| 51 | §20: executar teste e registrar estados, saldo e saída | aula-11-evidencias.md; aula-11-resultados.json; target/aula11-concorrencia.jsonl | ATENDIDO |
| 52 | §20: experimento sem lock, discutir não determinismo e restaurar | target/aula11-sem-lock.log: cópia isolada sem lock; fonte principal preservada e suíte protegida aprovada | ATENDIDO |
| 53 | §20: execução e discussão em dupla | Requer participação real da estudante e colega; não demonstrável por execução automatizada | NÃO VERIFICADO |
| 54 | §21.1: recurso protegido | unidadesDisponiveis de Jogo, guia atividade autônoma | ATENDIDO |
| 55 | §21.2: invariante | Consumo confirmado limitado pelo saldo; entradas acumuladas | ATENDIDO |
| 56 | §21.3: estratégia escolhida | PESSIMISTIC_WRITE PostgreSQL, helper e transação | ATENDIDO |
| 57 | §21.4: alternativa rejeitada e justificativa | Otimista/versionamento/retry; synchronized insuficiente entre JVMs | ATENDIDO |
| 58 | §21.5: ordem dos recursos | Jogos por ID crescente; raiz Venda explicada | ATENDIDO |
| 59 | §21.6: duas tarefas concorrentes | ConcorrenciaEstoqueTest e script HTTP adicional | ATENDIDO |
| 60 | §21.7: evidência de resultado válido | Uma venda FINALIZADA, outra ABERTA, saldo1 | ATENDIDO |
| 61 | §21.8: riscos residuais | Deadlocks externos, esperas, transações longas, prova não exaustiva | ATENDIDO |
| 62 | §22: respostas às quinze questões de revisão | aula-11-concorrencia.md seção própria com15 respostas | ATENDIDO |
| 63 | §23 Fundamentação: alternativas/custos/isolamento relacionados | aula-11-concorrencia.md fundamentos; SHOW READ COMMITTED e MVCC observados | ATENDIDO |
| 64 | §23 Implementação: ordem e duração conscientes | Services/helper; testes de stale state e ordem inversa | ATENDIDO |
| 65 | §23 Deadlock: ciclos e diagnóstico | Teste de ciclo deliberado40P01; prevenção ordenada e consultas no guia | ATENDIDO |
| 66 | §23 Teste: invariantes e limites do não determinismo | Barreiras, repetição3, cópia sem lock e HTTP complementar | ATENDIDO |
| 67 | §23 Diagnóstico: espera/timeout/deadlock/negócio separados | Testes reais e classificação SQLState no guia | ATENDIDO |
| 68 | §23 Transferência: recurso real e alternativa rejeitada | Estoque de Jogo e movimentos mistos, guia autônomo | ATENDIDO |
| 69 | §23 Comunicação: simplificações e riscos explícitos | aula-11-concorrencia.md adaptações de refresh, DynamicUpdate, Venda e limites | ATENDIDO |
| 70 | §24.1–3: explicar corrida, atomicidade e comparar estratégias | aula-11-concorrencia.md fundamentos e15 respostas; compreensão pessoal não presumida | ATENDIDO |
| 71 | §24.4–7: transação, lock antes do saldo, IDs crescentes e movimentos | BloqueioEstoque; VendaService; JogoService; suíte concorrente | ATENDIDO |
| 72 | §24.8–9: consultas livres e ausência de changelog | Teste consulta durante lock; hashes migrações/master preservados | ATENDIDO |
| 73 | §24.10–13: duas tarefas, uma finalizada, outra aberta, saldo não negativo | duasVendasDisputamCincoUnidades repetido3 e script HTTP | ATENDIDO |
| 74 | §24.14–16: suíte PostgreSQL, diff limpo e ausência de segredos | Evidências e inventário da Aula11; git diff --check | ATENDIDO |
| 75 | §24: publicação somente quando autorizada | Nenhum comando de escrita Git; HEAD continua Aula10; não se executa marco sugerido | ATENDIDO |
| 76 | §25: discussão e falhas controladas pertinentes | Guia e experimentos isolados; organização de encontros é orientação ao professor, não execução alegada | ATENDIDO |
| 77 | §26: manter segurança/login fora do escopo | Não implementados ContaAcesso, senha, JWT ou autorização | ATENDIDO |
| 78 | Regressão: rollback, preço histórico, fiscais, paginação e HTTP | 178 testes anteriores preservados; Newman81/135; suites VendaServiceTest e OpenApi* | ATENDIDO |
| 79 | Entrega: JSON/YAML e Swagger em test e bloqueado em prod | OpenApiDocumentationTest, OpenApiComercialTest, OpenApiProfilesTest; HTTP real | ATENDIDO |

A participação da estudante em dupla e sua apresentação continuam pendentes. Console manual é opcional; não se alega laboratório visual. Publicação no Git não foi realizada nem integra esta entrega local autorizada.
