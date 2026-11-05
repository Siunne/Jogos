# Aula 11 — Evidências de execução

Execuções reais em 05/11/2026, exclusivamente no PostgreSQL **jogos_test**, versão 17.11, isolamento READ COMMITTED. Não foram executadas escritas em desenvolvimento ou produção. Valores abaixo são resultados observados, não contagens esperadas do projeto do professor.

## Resultados finais

| Verificação | Resultado | Relatório temporário ignorado pelo Git |
|---|---|---|
| Baseline anterior à alteração | 178 Maven, 14 Node, Newman 81 requisições/135 verificações; zero falhas | target/aula11-baseline-* |
| Regressão intermediária dos services e controllers | 51 testes aprovados | target/aula11-etapa1.log |
| Suíte Maven completa final | **198 executados, 0 falhas, 0 erros, 0 ignorados**, BUILD SUCCESS, 1min28s | target/aula11-maven-final.log e target/surefire-reports |
| Teste concorrente novo | **19 execuções**, incluindo três repetições do cenário principal | TEST-br.com.emilly.jogos.application.ConcorrenciaEstoqueTest.xml |
| Teste OpenAPI novo | 1 aprovado; política e erros documentados, sem novas rotas | TEST-br.com.emilly.jogos.api.OpenApiConcorrenciaTest.xml |
| Node | **14 aprovados, 0 falhas**; corpus Java/ECMAScript e testes anteriores preservados | target/aula11-node-final.log |
| Newman real | **81 requisições, 135 verificações, 0 falhas** | target/aula11-newman.json e aula11-newman-final.log |
| Respostas Newman contra contrato | 81 respostas cobrindo as 38 operações; subconjunto declarado de JSON Schema | scripts/aula10-conferir-newman.cjs target/aula11-newman.json |
| HTTP concorrente adicional | **40 requisições**, cinco cenários, todas as invariantes aprovadas | target/aula11-http.json e aula11-http-final.log |
| Privacidade | 3 testes de filtro + 2 de integração = **cinco provas aprovadas** | PersistencePrivacyFilterTest e PersistencePrivacyIntegrationTest |
| Profiles | **três provas aprovadas**: dev/test documentados, prod bloqueado sem DataSource | OpenApiProfilesTest |
| Contrato | JSON e YAML interpretados e comparados; **38 operações, 24 caminhos, 30 schemas, oito tags** | OpenApiDocumentationTest, OpenApiComercialTest; docs/aula-11-openapi.yaml |
| Histórico | **24 arquivos históricos/migrações comparados por SHA-256 e preservados** | target/aula11-preservacao.json |

Os 178 testes anteriores não foram removidos ou desabilitados. Dois arquivos de teste OpenAPI foram adaptados para comparar o contrato **atual** com a entrega Aula 11 e incluir os novos códigos 409; as verificações anteriores foram mantidas. O YAML Aula 10 permanece histórico. O incremento é de 20 execuções: 19 concorrentes e uma documental.

Na execução inicial da nova classe houve um erro de montagem do spy fora de transação, corrigido ao instrumentar o alvo do proxy. A primeira suíte ampliada teve uma falha documental por falta do exemplo JSON da resposta 409; o exemplo foi acrescentado, preservando a asserção que detectou a omissão. A última suíte acima está integralmente aprovada. Esses erros de desenvolvimento não são omitidos nem confundidos com os resultados finais.

## Estados persistidos e concorrência

Os workers recebem somente IDs, cada um abre sua própria transação/conexão. As fixtures são confirmadas antes do disparo. O spy acrescenta barreiras antes/depois do helper, mas executa o método real, EntityManager e PostgreSQL. As leituras finais usam JDBC após o encerramento das transações, não os objetos alterados pelos workers. Fixtures próprias dos testes Java são removidas somente após essas leituras.

| Cenário | Observação real |
|---|---|
| Duas vendas de quatro, saldo cinco (três repetições) | Uma ABERTA e outra FINALIZADA; saldo **1**; nenhuma identidade de vencedora exigida |
| Duas saídas manuais de quatro | Um sucesso e uma rejeição por saldo; saldo **1** |
| Venda e saída manual de quatro | Um sucesso; saldo **1**, estado da venda coerente com seu resultado |
| Entradas de dois e três | Ambas confirmadas, saldo **10** |
| Entrada de três e venda de quatro | Ambas confirmadas, saldo **4** |
| Entrada pelo método legado e saída | Saldo **4**, sem atualização perdida |
| Dois jogos, inclusão em ordem inversa | Locks observados em ID crescente; uma venda confirma; saldos **1/1** |
| Duas finalizações da mesma venda | Uma falha de estado; uma única baixa; saldo **1** |
| Entidade previamente carregada em cinco | Outra transação confirma retirada de quatro; refresh lê **1** e impede nova saída de quatro |
| Alterações pendentes na mesma transação | Preço **120** mantido e sequência entrada/saída/entrada termina em saldo **7** |
| Cadastro com snapshot de saldo antigo | Nome/preço atualizados, saldo concorrente continua **1** com DynamicUpdate |
| Leitura durante lock com atualização não confirmada | SELECT comum conclui e retorna saldo confirmado **5**; após commit saldo **6** |
| Espera real | pg_stat_activity mostra Lock e pg_blocking_pids não vazio; após commit as duas operações concluem, saldo **3** |
| Timeout real de 150ms | SQLState **55P03**, HTTP **409 ApiError**, transação marcada para rollback e saldo **5** |
| Inclusão concorrente à finalização da mesma venda | Inclusão rejeitada após fechamento; um item preservado; saldos **1/5** |
| Deadlock deliberado fora da política ordenada | Um COMMIT e um SQLState **40P01**; nenhuma alteração de saldo, **5/5** |

O script HTTP executa cinco desses cenários por requisições reais à instância test: vendas concorrentes, saídas concorrentes, entradas concorrentes, venda contra saída e vendas com dois jogos em ordem inversa. HTTPs observados nos conflitos de saldo: **200/400**. GETs posteriores conferem saldos e estados. Isso complementa as barreiras Java; chamadas simultâneas por HTTP, sozinhas, não garantem que ambas tenham lido antes do primeiro commit.

As evidências versionáveis, sem documentos pessoais ou SQL com parâmetros, estão em [aula-11-resultados.json](aula-11-resultados.json). O JSON consolida os relatórios efetivamente gerados, inclusive resultados por suíte, estados, saldos e contagens. Logs completos e relatórios Newman permanecem somente em target, pois podem conter payloads fictícios.

## Rollback e preço histórico preservados

`VendaServiceTest.rollbackRealDoSegundoItemPreservaAmbosSaldosEVendaAberta` testa falha no segundo jogo: força flush e observa por JDBC o primeiro saldo temporário **8**, comprova rollbackOnly e, em outra transação REQUIRES_NEW com clear, verifica saldos **10/1** e venda **ABERTA**. O lock não esconde uma baixa parcial nem a deixa confirmada.

`snapshotPersistidoEFinalizacaoBaixamUmaUnicaVez` preserva o preço original do item após alteração do Jogo, confirma a baixa única e bloqueia repetição. Os testes de participantes inativos, função inadequada, jogo inativo, cancelamento, documentos fiscais, filtros, paginação, status e HTTP das aulas anteriores também foram executados na suíte completa.

## Experimentos sem proteção

Há duas evidências complementares, sempre em fixtures próprias de jogos_test:

1. `experimentoSemLocksDemonstraAtualizacaoPerdida` usa duas transações diretas JPA, sem o helper protegido. A barreira força leitura inicial cinco em ambas. Duas vendas finalizam, mas saldo termina um: **oito unidades vendidas com apenas cinco disponíveis**. O teste passa ao comprovar esse defeito deliberado, não ao aprovar tal comportamento de produção.
2. Uma cópia isolada em `target/aula11-sem-lock` recebeu a remoção temporária dos dois pontos de aquisição de lock do helper. O teste principal protegido foi executado três vezes: **3 testes, 3 falhas esperadas, 0 erros**. A asserção de um único vencedor falhou porque os dois resultados eram sucesso. Relatório: `target/aula11-sem-lock.log`. O código original não foi removido nem restaurado por Git: permaneceu protegido durante todo o experimento.

Não se usa a falha intencional da cópia para afirmar que a suíte principal falhou. O resultado final principal é 198/0/0/0. Uma corrida sem barreira pode não ocorrer em toda execução; estes experimentos controlam o escalonamento para tornar o defeito observável. Eles não representam teste de carga nem prova formal de todos os schedules.

## Swagger e ausência de migração

A instância test iniciou na porta 18080 com configuração explícita de `src/test/resources/application-test.properties`; log `target/aula11-app-final.log` confirma jogos_test e Liquibase: **0 novos changesets, 42 previamente aplicados**. Não houve nova tabela, coluna ou mudança de master. A primeira tentativa de iniciar com `spring-boot:run` sem importar o arquivo de teste não encontrou DataSource; o comando reproduzível do guia resolve a localização do profile, sem alterar configurações.

Swagger UI, JSON, YAML e swagger-config foram verificados por HTTP e pelos testes. Não se afirma uma interação visual manual da Aula 11, nem se substituem as capturas históricas anteriores. Produção não foi acessada: os testes de profiles excluem DataSource e Liquibase para testar somente disponibilidade documental.

## Limitações e pendências

Participação/discussão em dupla e apresentação exigem comprovação humana. Não foram executados dois processos de aplicação em hosts distintos, carga prolongada, caos de rede ou medição de throughput. A solução usa locks do PostgreSQL e não depende de synchronized para funcionar entre instâncias, mas os testes comprovam especificamente duas transações reais no ambiente disponível.

Ordenação reduz o ciclo entre jogos, sem prometer ausência universal de deadlocks. Transações externas com locks prévios ou escritas diretas que ignorem os services não são corrigidas magicamente. Não há timeout global nem retry automático. Autenticação, JWT, reservas, mensageria e filas continuam fora do escopo. Não houve commit, push, tag ou staging.
