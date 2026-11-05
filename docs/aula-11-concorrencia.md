# Aula 11 — Concorrência do estoque de jogos

Material integral consultado: [Aula 11 oficial](https://github.com/jeffersonarpasserini/suporteos2026/blob/main/docs/11aula/11aula.md). A implementação adapta Produto para Jogo e saldoEstoque para unidadesDisponiveis. O marco inicial é `0a7b201c5b61ae13a2832f734aac70bcd196e301`, branch main, tag publicada `aula-10-pessoas-vendas`. Nenhuma publicação foi realizada na Aula 11.

## Diagnóstico anterior à alteração

Diretório inicialmente limpo; Java 21, Spring Boot 4.0.8, PostgreSQL 17.11, Hibernate 7.2.24.Final, springdoc 3.0.3. Baseline executado: **178 testes Maven**, **14 Node**, **81 requisições e 135 verificações Newman**, sem falhas. Resultados temporários: `target/aula11-baseline-*`. Nenhuma dependência foi acrescentada.

Foram encontrados quatro caminhos de estoque: `VendaService.finalizar`, `JogoService.entrada`, `saida` e o método legado `receberUnidades`. O construtor de Jogo apenas estabelece o saldo inicial. PUT de atualização não recebe saldo, código, status ou data. As operações de cadastro e status não devem reenviar um saldo antigo ao banco.

## Invariante e fundamentos

O estoque de um jogo é um recurso escasso da loja: vendas e saídas manuais não podem confirmar consumo superior ao saldo, e entradas simultâneas devem se acumular. O total monetário usa o preço histórico dos itens; o lock não recalcula esse preço.

Atomicidade é confirmar todas as baixas ou nenhuma; isolamento é impedir interferência indevida entre transações. Consistência preserva as invariantes e durabilidade mantém commits. `@Transactional` sozinho não transforma leitura, decisão e gravação em uma operação exclusiva.

Os testes confirmam `READ COMMITTED`, padrão do PostgreSQL. Cada comando vê dados confirmados ao seu início; o contexto JPA pode ainda conter uma entidade antiga. MVCC permite SELECT comum observar a versão confirmada enquanto outro escritor mantém um lock. A aplicação não muda globalmente o isolamento.

Locking otimista com `@Version` é uma alternativa para conflitos raros: detecta atualização concorrente, mas uma operação precisa abortar e exige política de tratamento/repetição. Lock pessimista é adequado aqui porque a disputa pelo último estoque é esperada: serializa a decisão por jogo, ao custo de espera e menor throughput. Não é uma solução universal.

## Implementação e adaptações necessárias

`BloqueioEstoque.adquirir` exige transação (`MANDATORY`) e centraliza a política de `PESSIMISTIC_WRITE` no PostgreSQL. A finalização mantém `@Transactional`, valida a venda e os participantes, ordena os itens por ID de Jogo e bloqueia cada jogo antes de verificar status e saldo. O resultado é confirmado somente após todos os itens; uma exceção propaga e provoca rollback.

Sequência do helper:

1. Se este contexto já mantém `PESSIMISTIC_WRITE` na entidade, utiliza seu estado local protegido. Repetir refresh nessa situação descartaria movimentos ainda pendentes na mesma transação.
2. Adquire o lock sem descartar alterações locais (`EntityManager.lock`).
3. Executa flush sob lock, preservando alterações pendentes legítimas.
4. Executa `refresh(jogo, PESSIMISTIC_WRITE)` para recarregar o estado confirmado no banco.

Um refresh literal e incondicional do exemplo perderia alterações locais quando casos de uso são compostos na mesma transação. Os testes exercitam preço pendente e várias movimentações consecutivas, além de uma entidade carregada antes do commit concorrente.

`@DynamicUpdate` em Jogo evita que um PUT de nome/preço/status reenvie todas as colunas, incluindo saldo antigo. O teste `atualizacaoDeCadastroNaoReenviaSaldoAntigo` força essa intercalação. Não foi introduzido versionamento nem coluna nova.

Entradas e saídas usam o mesmo helper. `receberUnidades` delega à entrada dentro de sua transação. Após refresh, os relacionamentos necessários ao DTO são inicializados; OSIV continua false.

Os comandos que modificam uma mesma Venda também adquirem lock **na linha da Venda**, antes de consultar seu estado. Isso impede dupla finalização e inclusão/cancelamento concorrentes ao fechamento. Inclusão e remoção **não bloqueiam a linha do Jogo** nem reservam estoque. Essa é uma adaptação deliberada ao domínio existente: coordenação do estado da venda, separada da política de estoque do professor. Abertura, consultas, filtros e paginação continuam sem solicitação explícita de lock pessimista.

## Ordem, duração e deadlocks

Cada finalização bloqueia sua Venda e depois os Jogos em ID crescente, independentemente da inclusão dos itens. Movimentações manuais bloqueiam um único Jogo. A duração é a transação local, sem chamada HTTP externa, espera artificial ou interação humana dentro do código de produção. Os gates e timeouts existem apenas nos testes.

Sem ordenação: A mantém Jogo 1 e espera Jogo 2; B mantém Jogo 2 e espera Jogo 1. O ciclo caracteriza deadlock. PostgreSQL aborta uma transação, que deve sofrer rollback integral. Ordenar elimina esse ciclo específico, sem garantir ausência universal de deadlock: outras FKs, operações futuras e transações externas com locks já adquiridos podem formar ciclos.

`flush` é do contexto inteiro. Chamadores futuros não devem modificar saldos diretamente, antecipar locks em ordem diferente nem agrupar arbitrariamente vários casos de uso em uma transação longa. O helper não promete corrigir uma escrita direta por SQL ou um uso arbitrário das entidades fora dos services.

Timeout/deadlock de bloqueio é tratado como **409 ApiError**, com mensagem `Conflito concorrente; operação não concluída`, sem SQL, documento pessoal ou retry automático. Saldo insuficiente continua **400** e não é timeout. Nenhum timeout global foi imposto; em produção, o tempo máximo de espera depende da configuração operacional do banco. Os testes usam `SET LOCAL`, sem persistir configuração.

## Atividade orientada — linha do tempo

```mermaid
sequenceDiagram
    participant A as Transação A
    participant PG as PostgreSQL (saldo 5)
    participant B as Transação B
    A->>PG: BEGIN; carrega Venda A
    B->>PG: BEGIN; carrega Venda B
    A->>PG: PESSIMISTIC_WRITE Jogo
    B->>PG: solicita mesmo lock; aguarda
    A->>PG: refresh 5; retira 4; finaliza
    A->>PG: COMMIT; saldo 1; libera lock
    PG-->>B: lock concedido; refresh 1
    B->>PG: tentativa de retirar 4 falha
    B->>PG: ROLLBACK; venda segue ABERTA
```

Explicação (menos de 300 palavras): duas vendas abertas disputam quatro unidades cada sobre saldo cinco. As tarefas começam após fixtures confirmadas e mantêm transações e conexões independentes. Uma delas bloqueia o Jogo; a outra aguarda. A vencedora lê cinco sob lock, retira quatro e confirma. A perdedora recarrega um após conseguir o lock, rejeita a quantidade e desfaz sua transação. A identidade da vencedora não é predeterminada. As consultas JDBC finais verificam saldo um e estados ABERTA/FINALIZADA, sem depender dos objetos usados pelos workers. Atomicidade preserva uma venda com múltiplos itens; locking protege a decisão contra outra transação. Um experimento de teste isolado contorna o helper e força ambas as leituras em cinco: as duas vendas terminam finalizadas, mas saldo um, reproduzindo atualização perdida. Sem barreira, esse defeito depende do escalonamento e pode não aparecer em uma execução. O código de produção não foi desprotegido para fazer o experimento. A execução e discussão em dupla continuam dependentes da estudante.

## Atividade autônoma — oito entregáveis

| Entregável | Adaptação ao tema Jogos |
|---|---|
| Recurso protegido | unidadesDisponiveis do Jogo comercializado |
| Invariante | soma de consumos confirmados não excede disponibilidade; entradas se acumulam |
| Estratégia | lock pessimista por linha do Jogo na transação |
| Alternativa rejeitada | otimista exige abortos por versão e política adicional; synchronized não coordena JVMs distintas |
| Ordem | Venda do comando; Jogos por ID crescente |
| Teste concorrente | ConcorrenciaEstoqueTest, duas tarefas, transações e conexões reais |
| Evidência | ABERTA/FINALIZADA, saldo 1, SQL independente; evidências separadas |
| Riscos | espera, transações longas, ciclos externos, limite da prova e operações que ignorem a política |

O protocolo reside no banco compartilhado; não depende de memória compartilhada entre instâncias. Os testes executam duas conexões na mesma JVM, não duas JVMs ou carga distribuída.

## Execução reproduzível

Configure externamente `DB_TEST_URL`, `DB_TEST_USERNAME` e `DB_TEST_PASSWORD` para **jogos_test**. Não compartilhe `.env`. Liquibase já existente inicializa o banco, `ddl-auto=validate` e OSIV=false permanecem. Não há changelog da Aula 11: a mudança é de coordenação, sem alteração estrutural. Migrações 001–005 e master são preservados.

```powershell
.\mvnw.cmd test
# Instância HTTP segura: application-test está em src/test/resources.
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=test' '-Dspring-boot.run.arguments=--server.port=18080 --spring.config.additional-location=file:src/test/resources/application-test.properties'
# Em outro terminal, confira no log JDBC o catálogo jogos_test antes das escritas:
node scripts/aula11-exportar-contrato.cjs
node --test scripts/aula09-schema-validator.test.cjs scripts/aula10-contrato-gerador.test.cjs scripts/aula10-unicode-equivalencia.test.cjs
$env:AULA11_BANCO_TESTE_CONFIRMADO='jogos_test'
node scripts/aula11-concorrencia-http.cjs
npm.cmd exec --offline --package=newman -- newman run postman/Jogos-Aula10.postman_collection.json --env-var baseUrl=http://localhost:18080 --reporters cli,json --reporter-json-export target/aula11-newman.json
node scripts/aula10-conferir-newman.cjs target/aula11-newman.json
git diff --check
```

Newman precisa estar disponível no cache npm para `--offline`; se não estiver, instale a ferramenta de acordo com o ambiente. Não substitua Newman por executor próprio. As coleções históricas são mantidas; o script HTTP adicional escreve fixtures fictícias próprias e as deixa somente em jogos_test. Os testes Java concorrentes removem somente suas próprias fixtures após leitura das evidências.

Swagger: `http://localhost:18080/swagger-ui.html`; contrato JSON `/v3/api-docs` e YAML `/v3/api-docs.yaml`. São as mesmas 38 operações, 24 caminhos, 30 schemas e oito tags. Entrega nova: [aula-11-openapi.yaml](aula-11-openapi.yaml). Os contratos Aula 09 e 10 são históricos; o exportador antigo recusa sobrescrever Aula 10 com uma aplicação Aula 11.

Os validadores Node verificam um subconjunto declarado de JSON Schema; não certificam integralmente a especificação OpenAPI. Os testes Maven interpretam YAML e o comparam estruturalmente ao JSON atual. Profiles dev/test habilitam documentação; prod a desabilita, sem conexão com produção nos testes.

## Diagnóstico sem dados pessoais

```sql
-- Consultas de leitura: executar no banco isolado de testes.
SELECT pid, state, wait_event_type, wait_event,
       pg_blocking_pids(pid) AS bloqueadores
FROM pg_stat_activity
WHERE datname = current_database();

SELECT pid, locktype, relation::regclass, mode, granted
FROM pg_locks
WHERE database = (SELECT oid FROM pg_database WHERE datname = current_database());
```

Não exportar `query` nem parâmetros de clientes em evidências. Um row lock pode aparecer como espera de transactionid; não é obrigatório enxergar um tuple lock específico. Espera legítima tem bloqueador ativo e progride após commit/rollback; timeout de lock traz SQLState **55P03**; deadlock **40P01**; cancelamento por statement_timeout **57014**; estoque insuficiente é exceção de domínio, sem SQLState. Falha de conexão é infraestrutura, não saldo insuficiente. O teste observa `wait_event_type='Lock'` e `pg_blocking_pids`, depois libera o titular e exige conclusão com timeout do Future.

O experimento opcional dos dois consoles pode ser reproduzido em jogos_test: A abre transação e executa SELECT do Jogo de teste FOR UPDATE; B tenta atualizar a mesma linha; a espera termina quando A faz COMMIT/ROLLBACK. Não foi necessário executar manualmente esse laboratório: há duas conexões reais automatizadas, mas não se afirma interação em consoles ou Swagger visual.

Métricas futuras: duração de finalização, tempo de espera, abortos por deadlock, recusas por saldo e erros por endpoint. Não foram adicionados mensageria, reservas, autenticação, JWT, retry, isolamento global ou controle otimista.

## Quinze questões de revisão

1. `@Transactional` delimita a unidade, mas permite duas leituras do mesmo saldo confirmado.
2. Atomicidade evita confirmação parcial; isolamento coordena interferências entre transações.
3. Atualização perdida ocorre quando uma escrita baseada em leitura antiga sobrescreve outra baixa.
4. MVCC disponibiliza versões confirmadas a leitores sem bloquear cada SELECT comum.
5. Otimista costuma ser melhor quando conflitos são raros e abortar/repetir é aceitável.
6. Pessimista é justificável quando a disputa pelo recurso é esperada e a transação é curta.
7. Decidir antes do lock permite validar saldo já consumido por outra transação.
8. Refresh substitui o snapshot antigo do contexto pelo estado do banco; exige cuidado com mudanças locais.
9. Ordenar IDs impede o ciclo específico de duas vendas com ordem inversa de jogos.
10. HTTP externo dentro da transação prolongaria locks e aumentaria espera, falhas e risco de ciclos.
11. Synchronized coordena somente a memória de uma JVM, não instâncias independentes.
12. Não há nova coluna/tabela: política de execução não demanda changelog.
13. Mocks não provam MVCC, espera ou rollback do PostgreSQL; o spy dos testes mantém a implementação real.
14. O escalonamento decide a vencedora; ordem de criação das vendas não é critério de justiça.
15. Bloqueadores e eventos de espera observáveis, prazos limitados e conclusão após liberação distinguem espera de aparente travamento.

Resultados e auditoria: [evidências](aula-11-evidencias.md), [matriz de conformidade](aula-11-auditoria.md), [inventário](aula-11-arquivos.md). A participação/apresentação humana não pode ser comprovada pelo código.
