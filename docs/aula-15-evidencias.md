# Aula 15 — Evidências e reprodução

## Ambiente e fotografia inicial

A branch `main` começou limpa, com staging vazio. HEAD e a tag `aula-14-autorizacao-rbac` apontavam para `32ac07eef38726b94f8593f60db8d77b7fd6d1a5`. Foram consultados o README, o HELP, o inventário, a auditoria, a matriz e as evidências anteriores. O material oficial foi lido integralmente. O inventário inicial contém as 34 classes executadas e as responsabilidades reais. [Material oficial da Aula 15](https://github.com/jeffersonarpasserini/suporteos2026/blob/main/docs/15aula/15aula.md).

| Componente | Versão ou configuração efetiva |
|---|---|
| Windows e Java | Windows 11, Temurin 21.0.12 |
| Wrapper | Maven 3.9.16, confirmado por `.\mvnw.cmd -v` |
| Spring Boot e Spring Security | 4.0.8 e 7.0.7 |
| Surefire e Failsafe | Surefire 3.5.6 herdado, com JUnit Platform. Failsafe é gerenciado pelo parent, sem execução neste projeto. |
| JUnit Jupiter e Mockito | 6.0.3 e 5.20.0 |
| Springdoc e Liquibase | 3.0.3 e 5.0.3 |
| PostgreSQL e driver JDBC | Instância local 17.11, driver 42.7.13 |
| JaCoCo | 0.8.16 resolvido pelo Maven Central e executado no Java 21 |
| Node e npm | 24.13.0 e 11.6.2 |

O PostgreSQL local na porta 5433 forneceu a infraestrutura real sem Docker. As execuções com persistência fixaram `DB_TEST_URL` em `jdbc:postgresql://localhost:5433/jogos_test`. O Hibernate identificou `jogos_test/public` e isolamento `READ_COMMITTED`. Os testes de segurança conferem `current_database()`. Os testes de perfil documental usam contextos de configuração sem executar persistência em dev ou produção. As migrações 001–006 permaneceram intactas.

O POM já usava os plugins Spring Boot e Liquibase. O Surefire recebe o `argLine` do agente sem configuração adicional do projeto. Os testes com Mockito e o spy da suíte de concorrência passaram com a instrumentação. O aviso de attach dinâmico observado permaneceu visível. O POM acrescenta somente o plugin e propriedades de cobertura.

## Rodadas Maven

| Execução | Comando | Testes | Falhas | Erros | Ignorados | Tempo Maven |
|---|---|---:|---:|---:|---:|---:|
| Baseline somente relatório | `.\mvnw.cmd clean verify` | 366 | 0 | 0 | 0 | 2 min 09 s |
| Conta isolada | `.\mvnw.cmd "-Dtest=ContaAcessoTest" test` | 21 | 0 | 0 | 0 | 10,066 s |
| Service isolado | `.\mvnw.cmd "-Dtest=JogoServiceUnitTest" test` | 4 | 0 | 0 | 0 | 6,723 s |
| Atividade orientada | `.\mvnw.cmd clean verify` | 391 | 0 | 0 | 0 | 1 min 42 s |
| RED isolado | `.\mvnw.cmd "-Dtest=ContaAcessoCustoBcryptTest" test` | 4 | 2 | 0 | 0 | Consulte `red.log` |
| GREEN isolado | Mesmo comando RED | 4 | 0 | 0 | 0 | 11,276 s |
| GREEN completo | `.\mvnw.cmd clean verify` | 395 | 0 | 0 | 0 | 1 min 40 s |
| REFACTOR com portão | `.\mvnw.cmd clean verify` | 395 | 0 | 0 | 0 | 1 min 44 s |
| Portão temporário | `.\mvnw.cmd "-Djacoco.minimum.branch.coverage=0.90" verify` | 395 | 0 | 0 | 0 | 3 min 05 s |

O build com portão temporário usou `verify` após a rodada REFACTOR. O XML correspondente contém duas sessões acumuladas. Os 395 testes passaram e o Maven retornou código 1 por violação do limite de branches no goal `jacoco:0.8.16:check`, que registrou `branches covered ratio is 0.88, but expected minimum is 0.90` e `Coverage checks have not been met`. A propriedade de linha de comando manteve os limites do POM em `0.70` para linhas e `0.40` para branches.

A execução final `.\mvnw.cmd clean verify` retornou código 0 em 3 min 26 s e produziu uma única sessão. Os 37 relatórios Surefire somam 395 execuções, zero falhas, zero erros e zero ignorados. Os contadores finais coincidem com os da rodada acumulada, incluindo 1.548/1.594 linhas e 581/660 branches. O log `final.log` contém `All coverage checks have been met` e `BUILD SUCCESS`. A reprovação específica comprova o funcionamento do portão. Para uma futura reprodução com dados isolados, prefira `clean verify` também na execução com o limite temporário.

## Regressão HTTP externa e Node

| Coleção | Instância usada | Requisições | Verificações | Falhas |
|---|---|---:|---:|---:|
| Aula 12 | JAR histórico em `4000630` | 94 | 239 | 0 |
| Aula 13 | JAR histórico em `e58342e` | 98 | 250 | 0 |
| Aula 14 | JAR da validação final | 112 | 278 | 0 |

As três instâncias rodaram uma por vez em `127.0.0.1:18081`, com o profile `test` e `jogos_test/public`. Os JARs históricos foram empacotados em cópias temporárias feitas com `git archive`, sem alterar branch, index ou histórico. O pacote histórico foi usado exclusivamente para reproduzir o laboratório da respectiva versão. O administrador da Aula 14 foi gerado em memória, provisionado pelo runner e removido do banco de testes no encerramento. Os relatórios Newman foram sanitizados antes da escrita.

O comparador validou o subconjunto de JSON Schema de 112 respostas e cobriu 44 operações. O contrato servido final tem 30 caminhos, 39 schemas e dez grupos. A suíte Maven manteve 86 casos JWT, 20 de autorização e bootstrap e 19 de concorrência de estoque. A matriz de autorização cobre 132 combinações no teste que percorre 44 operações com três identidades. Os casos MockMvc e Newman fornecem evidências complementares.

### Como repetir

Para cada aula histórica, extraia o commit correspondente com `git archive --format=zip` em uma pasta temporária. Empacote nessa pasta com `.\mvnw.cmd "-DskipTests" package`. A opção serve somente à preparação dos JARs históricos, sem substituir o `clean verify` da Aula 15. Para a Aula 14 use o JAR do build final. Inicie cada instância a partir da raiz atual, para importar o `.env` local, e passe o arquivo de configuração de teste da versão usada. Para a Aula 14 use as variáveis de bootstrap e um administrador fictício conforme o [guia da Aula 14](aula-14-autorizacao.md).

```powershell
$env:DB_TEST_URL = 'jdbc:postgresql://localhost:5433/jogos_test'
java -jar $jar --spring.profiles.active=test --server.address=127.0.0.1 --server.port=18081 "--spring.config.additional-location=file:$configTest"
```

Em outro terminal, com a instância pronta, substitua `$aula` por 12, 13 ou 14 de acordo com a instância.

```powershell
$base = 'http://127.0.0.1:18081'
[Environment]::SetEnvironmentVariable("AULA${aula}_BASE_URL", $base)
[Environment]::SetEnvironmentVariable("AULA${aula}_BANCO_TESTE_CONFIRMADO", 'jogos_test')
curl.exe -fsS "$base/v3/api-docs" -o "target/aula$aula-openapi.json"
npm.cmd exec --offline --package=newman -- node "scripts/aula$aula-newman.cjs"
```

Mantenha desabilitado o bootstrap nos JARs históricos e pare cada instância ao terminar sua coleta. Para a Aula 14, exporte também `/v3/api-docs.yaml` em `target/aula14-openapi.yaml`. Depois das três rodadas, execute:

```powershell
node scripts/aula14-conferir-newman.cjs
Copy-Item target/aula14-openapi.json target/aula10-openapi.json
node --test (Get-ChildItem scripts -Filter '*.test.cjs' | ForEach-Object FullName)
```

O alias da Aula 10 alimenta o teste de equivalência Unicode do contrato atual e usa o fixture Java gerado pelo Maven. As validações que esperam especificamente a Aula 12 ou 13 leem os artefatos históricos recém-gerados. O Node aprovou 30 testes, zero falhas e zero ignorados.


## Auditoria independente

O `clean verify` independente terminou em 2 min 31 s com 395 testes aprovados, zero falhas, erros ou ignorados. O JaCoCo confirmou 1.548/1.594 linhas e 581/660 branches, incluindo 46/46 linhas e 30/30 branches em `ContaAcesso`. O CSV coincidiu por classe com o relatório final preservado.

O laboratório independente aprovou 112 requisições e 278 verificações Newman, seguidas de 30 testes Node com os artefatos atualizados. O contrato manteve 44 operações, 30 caminhos, 39 schemas e dez grupos. O `AdministradorInicialRunner` foi exercitado na JVM externa do JAR. Essa execução ficou fora da cobertura instrumentada do Surefire. As validações e o diff confirmaram a preservação dos contratos e das funcionalidades anteriores, sem regressão comprovada.

O RED foi reproduzido com o código publicado da Aula 14. O [registro TDD](aula-15-tdd.md) descreve o resultado independente e o alcance dos artefatos locais como evidência da cronologia.

## Artefatos locais

Os relatórios JaCoCo iniciais, intermediários e finais, XMLs Surefire da base, logs e diffs do TDD ficam em `%TEMP%/jogos-aula15-evidencias`. Em particular, `baseline-jacoco`, `orientada-jacoco`, `green-jacoco`, `refactor-jacoco`, `portao-90-jacoco` e `final-jacoco` preservam as medições por rodada. O HTML final é reproduzível em `target/site/jacoco/index.html`, junto de XML e CSV. Os relatórios gerados ficam fora do Git.
