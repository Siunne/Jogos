# Aula 15 — Testes de software e cobertura

A Aula 15 usa a suíte da tag `aula-14-autorizacao-rbac` como base. A caracterização de conta em memória, os testes do serviço de jogos com Mockito e o TDD do intervalo BCrypt acrescentam casos localizados à evidência de integração preservada.

- [Estratégia e conceitos](aula-15-estrategia-testes.md), [inventário](aula-15-inventario-testes.md) e [diagramas](aula-15-diagramas.md).
- [TDD](aula-15-tdd.md), [cobertura](aula-15-cobertura.md) e [evidências e reprodução](aula-15-evidencias.md).
- [Questões de revisão](aula-15-questoes.md) e [conformidade acadêmica](aula-15-conformidade.md).
- [Material oficial](https://github.com/jeffersonarpasserini/suporteos2026/blob/main/docs/15aula/15aula.md) e [evidências da Aula 14](aula-14-evidencias.md).

## Execução no Windows

Use Java 21, o Maven Wrapper e um PostgreSQL local. Configure credenciais de teste no ambiente ou no `.env` ignorado. Confira que a URL resolve exclusivamente para `jogos_test` antes de executar a suíte completa.

```powershell
.\mvnw.cmd -v
.\mvnw.cmd "-Dtest=ContaAcessoTest" test
.\mvnw.cmd "-Dtest=JogoServiceUnitTest" test
$env:DB_TEST_URL = 'jdbc:postgresql://localhost:5433/jogos_test'
.\mvnw.cmd clean verify
Invoke-Item target/site/jacoco/index.html
```

O relatório também contém `jacoco.xml` e `jacoco.csv`. O portão de `verify` exige 70% de linhas e 40% de branches para o projeto. Para repetir a falha controlada com dados novos, execute `.\mvnw.cmd "-Djacoco.minimum.branch.coverage=0.90" clean verify` e depois `.\mvnw.cmd clean verify` com os limites do POM. Consulte as [evidências da rodada original](aula-15-evidencias.md) e os [limites do portão](aula-15-cobertura.md). Leia também o relatório por classe e confira o oráculo dos cenários críticos.
