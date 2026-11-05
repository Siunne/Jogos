# Aula 10 — Evidências reais

## Verificação atual — equivalência Unicode do OpenAPI

**Maven: 178 testes aprovados; Node: 14 aprovados; Newman: 81 requisições/135 verificações sem falhas.** Corpus Java/ECMAScript: 5.539 entradas, nove campos e zero divergências; POST/PUT e JDBC confirmados. HTTP de regressão:253 chamadas esperadas. [Relatório atual](aula-10-openapi-unicode.md). Logs em target/aula10-unicode-*. As seções seguintes são evidências históricas de rodadas anteriores.


## Histórico: campos obrigatórios após normalização

**Maven: 176 testes, Node: 12 testes e Newman: 81 requisições/135 verificações aprovados, sem falhas.** HTTP real adicional: 253 chamadas, incluindo 162 rejeições400, preservação de dados e NBSP. [Relatório atual](aula-10-validacao-textos.md). Logs em target/aula10-textos-*. Cinco provas de privacidade, rollback e preço histórico incluídos na suíte completa. As evidências seguintes pertencem às rodadas anteriores e são preservadas como histórico.

## Evidências da implementação anterior preservadas


Baseline antes de implementar: 85 testes aprovados, log target/aula10-baseline.log. Compilação final: target/aula10-compile-final.log. Suíte final: **152 testes, 0 falhas, 0 erros, 0 ignorados**, BUILD SUCCESS; log target/aula10-suite-final.log e XMLs Surefire. Os 85 casos anteriores foram preservados; fixtures fiscais e verificações de contrato/changelog foram adaptadas à evolução, sem remover testes.

| Suíte | Testes | Falhas | Erros | Ignorados |
|---|---:|---:|---:|---:|
| br.com.emilly.jogos.api.OpenApiComercialTest | 5 | 0 | 0 | 0 |
| br.com.emilly.jogos.api.OpenApiDocumentationTest | 19 | 0 | 0 | 0 |
| br.com.emilly.jogos.api.OpenApiProfilesTest | 3 | 0 | 0 | 0 |
| br.com.emilly.jogos.application.JogoServiceTest | 4 | 0 | 0 | 0 |
| br.com.emilly.jogos.application.VendaServiceTest | 8 | 0 | 0 | 0 |
| br.com.emilly.jogos.controller.Aula08ControllerTest | 24 | 0 | 0 | 0 |
| br.com.emilly.jogos.controller.Aula10ControllerTest | 15 | 0 | 0 | 0 |
| br.com.emilly.jogos.controller.ConsultasAula07Test | 5 | 0 | 0 | 0 |
| br.com.emilly.jogos.controller.GeneroJogoControllerTest | 5 | 0 | 0 | 0 |
| br.com.emilly.jogos.domain.Aula08DomainTest | 6 | 0 | 0 | 0 |
| br.com.emilly.jogos.domain.DocumentoFiscalTest | 22 | 0 | 0 | 0 |
| br.com.emilly.jogos.domain.GeneroJogoTest | 5 | 0 | 0 | 0 |
| br.com.emilly.jogos.domain.JogoTest | 7 | 0 | 0 | 0 |
| br.com.emilly.jogos.domain.PessoaTest | 6 | 0 | 0 | 0 |
| br.com.emilly.jogos.domain.VendaTest | 9 | 0 | 0 | 0 |
| br.com.emilly.jogos.JogosApplicationTests | 1 | 0 | 0 | 0 |
| br.com.emilly.jogos.MigracaoComercialTest | 2 | 0 | 0 | 0 |
| br.com.emilly.jogos.MigracaoGeneroTest | 1 | 0 | 0 | 0 |
| br.com.emilly.jogos.PersistenciaJpaTest | 4 | 0 | 0 | 0 |
| br.com.emilly.jogos.UnicidadeGeneroTest | 1 | 0 | 0 | 0 |

Node: sete testes do validador aprovados, inclusive multipleOf (target/aula10-node.log). Newman real 6.2.3: **81 requisições, 135 verificações, 0 falhas**, execução 2026-11-05T12:14:27.707Z; relatório target/aula10-newman.json. Inclui as 30 requisições anteriores e 51 novas, não um executor próprio. A validação Node continua um subconjunto explícito, não certificação integral de OpenAPI.

## Persistência, migração e rollback

42 changesets: 18 anteriores intactos + 24 da 005. MigracaoComercialTest aplica master em schema limpo e incrementalmente após 001–004, verifica cinco tabelas, sete checks e seis FKs, preserva fornecedor legado. VendaServiceTest força flush antes da conclusão da transação de teste marcada rollbackOnly: JDBC vê saldo temporário8; após rollback, outra transação vê10/1 e ABERTA. O teste de sucesso confirma baixa e impede finalização duplicada. O teste de eficiência verifica paginação sem coleção e no máximo duas consultas.

O Newman comprova externamente rollback depois da venda principal: insuficiência em B, HTTP400, A continua8, B continua7 e venda ABERTA. Consulte os JSONs essenciais abaixo. Nenhuma operação de desenvolvimento ou produção foi executada, exceto SELECT de diagnóstico de fornecedores. Não foram saneados CNPJs automaticamente.

## Atividade orientada e contrato

[JSONs reais essenciais](aula-10-evidencias-http.json). Status200 de finalização; estados e saldos; erro400 na tentativa de remover item finalizado. Preço histórico unitário100 e subtotal200 após catálogo120; total350 com o segundo jogo. As 20 novas operações possuem êxitos exercitados no Newman e/ou MockMvc e fazem parte da comparação exata das 38 rotas com os mappings reais.

OpenAPI real: 38 operações / 24 caminhos / 30 schemas / oito tags. JSON/YAML são interpretados e comparados aos artefatos, com resolução de refs. Arquivo versionável exclusivo: docs/aula-10-openapi.yaml; temporários em target/aula10-openapi.*. O YAML da Aula09 permanece inalterado. Profiles dev/test/prod são verificados sem DataSource no contexto de produção. Swagger UI real foi aberto em Chrome, verificou38 operações e os quatro grupos novos: [captura](evidencias/aula-10-swagger-ui.png). A captura foi aberta e inspecionada; as antigas permanecem.

## Falhas intermediárias corrigidas, sem ocultação

Uma anotação Schema gerada repetia atributos; foi corrigida antes de avançar. Duas fixtures antigas tinham CNPJ inválido; foram substituídas mantendo as verificações. O teste de contagem esperava18 changesets totais; agora confirma exatamente18 anteriores e24 novos. O parser isolado dos exemplos precisava dos módulos JavaTime; foi ajustado sem alterar a API. O ensaio de schema isolado precisava restaurar autocommit após Liquibase para que uma rejeição SQL esperada não impedisse a limpeza do próprio schema de testes. Nenhum erro foi contornado com ddl-auto=update, OSIV, exclusão de registros legados ou desabilitação de testes.

## Reprodução e conferência final

A cópia em target/aula10-reproducao contém fontes, wrapper, documentação, scripts e coleções, sem .git, .env ou classes pré-compiladas. Foi executada com configuração externa apontando somente a jogos_test: **152 testes, 0 falhas, 0 erros, 0 ignorados; BUILD SUCCESS** (target/aula10-reproducao.log). Não foi executado git clone. A inspeção final manteve os changesets001–004 e o YAML histórico09 inalterados. Os dois fornecedores inválidos de desenvolvimento continuam dois; em testes permanecem os12 legados inválidos, acrescidos apenas de fornecedores novos válidos. Nenhum saneamento automático foi feito.

## Pendências humanas preservadas

Sem teste de concorrência/locks, login, estorno ou segurança implementada, por recorte explícito da aula. Revisão em dupla/apresentação exige participação humana: NÃO VERIFICADA. Saneamento dos dois fornecedores de desenvolvimento exige confirmação documental e autorização futura. Migração005 não foi aplicada em dev/prod nesta execução. Nenhum commit, push ou tag da Aula10 foi criado. Os artefatos temporários/logs permanecem ignorados em target.
