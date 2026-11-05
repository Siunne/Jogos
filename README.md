# Jogos

Projeto desenvolvido em Java com Spring Boot para gerenciamento de jogos.

## Tema

Sistema para cadastrar e organizar jogos por gênero.

## Entidades

- Jogo
- GeneroJogo

## Relacionamento

GeneroJogo 1:N Jogo

Um gênero pode possuir vários jogos e cada jogo pertence a um gênero.

## Tecnologias

- Java 21
- Spring Boot
- Maven
- Spring Web
- Validation
- Spring Data JPA
- PostgreSQL
- Liquibase

## Perfis

O projeto possui configurações separadas para os ambientes:

- dev
- test
- prod

As credenciais reais do banco de dados são mantidas fora do versionamento.

## Banco de dados

O PostgreSQL é utilizado nos ambientes de desenvolvimento e teste.

O Liquibase é responsável pela criação e evolução do esquema do banco de dados, enquanto o Hibernate valida o esquema por meio de `ddl-auto=validate`.

## Teste da aplicação

Endpoint:

```text
GET /api/health
```

Resultado esperado:

```text
OK
```

Para executar os testes:

```powershell
.\mvnw.cmd test
```

## Aula 04

A documentação da persistência, diagrama, restrições e evidências dos testes está disponível em:

`docs/aula-04-persistencia.md`

## Aula 05

Nesta aula foram implementados repositories com Spring Data JPA, serviços transacionais, consultas derivadas, tratamento de exceções, rollback e dirty checking.

Foram criados:

- `GeneroJogoRepository`
- `JogoRepository`
- `GeneroJogoService`
- `JogoService`
- `RecursoNaoEncontradoException`
- `RecursoDuplicadoException`

A documentação da Aula 05 está disponível em:

`docs/aula-05-repositories-servicos-transacoes.md`

Ao final da aula, foram executados 20 testes com sucesso.

## Aula 09 — OpenAPI

O contrato da Jogos API está documentado com springdoc 3.0.3, compatível com Spring Boot 4.0.8. A [documentação da Aula 09](docs/aula-09-openapi.md) contém execução segura, matriz das 18 operações, ficha de contrato, laboratório visual, segurança e resultados reais. Consulte também o [YAML gerado](docs/aula-09-openapi.yaml), as [evidências](docs/aula-09-evidencias.md) e a [auditoria](docs/aula-09-auditoria.md).

Em dev/test: `/v3/api-docs`, `/v3/api-docs.yaml` e `/swagger-ui.html`. Em prod, os endpoints documentais ficam desabilitados. Para a instância segura de testes e as variáveis locais necessárias, siga o guia antes de executar os exemplos.

A Aula 09 foi publicada no commit `96fd68ff7964bc8e351d54c9a097be4110fe3e7e`, com a tag `aula-09-openapi`.

## Aula 10 — Pessoas, papéis e vendas de jogos

O [guia da Aula 10](docs/aula-10-pessoas-vendas.md) reúne modelagem por composição, validação fiscal, migração 005, 20 novas operações, preço histórico e finalização transacional. Consulte o [contrato atual](docs/aula-10-openapi.yaml), a [coleção completa](postman/Jogos-Aula10.postman_collection.json), as [evidências](docs/aula-10-evidencias.md) e a [auditoria](docs/aula-10-auditoria.md).

A Aula 10 permanece exclusivamente local, sem commit, push ou tag. Exemplos e coleções devem utilizar o banco de testes; desenvolvimento e produção não foram migrados nesta execução. O contrato e as evidências da Aula 09 continuam identificados como históricos.
