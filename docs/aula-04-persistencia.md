# Aula 04 - Persistência com JPA, PostgreSQL e Liquibase

## Banco de dados

O projeto utiliza PostgreSQL como banco de dados nos ambientes de desenvolvimento e teste.

A criação e evolução do esquema são controladas pelo Liquibase. O Hibernate utiliza `ddl-auto=validate`, portanto apenas valida se o mapeamento JPA está de acordo com o banco criado pelas migrations.

## Diagrama

```text
GeneroJogo
-------------------------
id              PK
nome            NOT NULL
status          NOT NULL
        |
        | 1
        |
        | N
        v
Jogo
-------------------------
id                      PK
codigo                  NOT NULL, UNIQUE
nome                    NOT NULL
unidades_disponiveis    NOT NULL, CHECK >= 0
preco                   NOT NULL, CHECK >= 0
data_cadastro           NOT NULL
status                  NOT NULL
genero_jogo_id          NOT NULL, FK
```

Relacionamento: `GeneroJogo 1:N Jogo`

Um gênero pode possuir vários jogos e cada jogo pertence obrigatoriamente a um gênero.

## Migrations Liquibase

Foram registrados 8 changeSets no banco de dados.

As migrations criam as tabelas `genero_jogo` e `jogo` e adicionam as restrições necessárias para garantir a integridade dos dados.

## Justificativa das restrições

A restrição `UNIQUE` em `jogo.codigo` impede que dois jogos sejam cadastrados com o mesmo código.

A `FOREIGN KEY` `genero_jogo_id` garante que todo jogo esteja relacionado a um gênero existente. O uso de `ON DELETE RESTRICT` impede a remoção de um gênero que ainda possui jogos associados.

Os campos obrigatórios possuem `NOT NULL`, pois representam informações necessárias para manter uma entidade válida no sistema, como nome, código, quantidade, preço, data, status e gênero.

Os `CHECK` de `unidades_disponiveis` e `preco` impedem valores negativos.

Os `CHECK` de status garantem que somente os valores `ATIVO` ou `INATIVO` sejam armazenados.

## Perfis

O projeto possui os seguintes perfis:

- `dev`: utiliza o banco `jogos_dev`;
- `test`: utiliza o banco `jogos_test`;
- `prod`: utiliza variáveis de ambiente fornecidas pelo ambiente de produção.

As senhas reais não são versionadas no Git. O arquivo `.env.example` contém somente exemplos das variáveis necessárias.

## Testes

Ao executar:

```powershell
.\mvnw.cmd test
```

o resultado obtido foi:

```text
Tests run: 17
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

Entre os testes de persistência estão:

- persistência e recarga de um jogo com seu gênero;
- confirmação dos 8 changeSets registrados;
- rejeição de código de jogo duplicado pelo banco;
- rejeição de quantidade negativa pelo banco.