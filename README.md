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