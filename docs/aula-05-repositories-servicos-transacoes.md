# Aula 05 - Spring Data JPA, Repositories, Serviços e Transações

## Objetivo

Nesta aula foram implementados repositories com Spring Data JPA e serviços transacionais para o domínio de jogos.

A implementação utiliza as entidades `GeneroJogo` e `Jogo`, criadas nas aulas anteriores.

## Repositories

Foram criados dois repositories:

- `GeneroJogoRepository`
- `JogoRepository`

Os repositories estendem `JpaRepository`, permitindo utilizar operações de persistência fornecidas pelo Spring Data JPA.

### Consultas por chave de negócio

No `GeneroJogoRepository` foram criadas consultas pelo nome do gênero:

- `existsByNomeIgnoreCase`
- `findByNomeIgnoreCase`

No `JogoRepository` foram criadas consultas pelo código do jogo:

- `findByCodigo`
- `existsByCodigo`

O código funciona como uma chave de negócio utilizada para localizar um jogo e impedir cadastros duplicados.

### Consulta por relacionamento

Foi criada a consulta:

`findByGeneroId(Long generoId)`

Ela permite buscar os jogos relacionados a determinado gênero.

Também foi criada:

`findByStatus(Status status)`

para consultar jogos de acordo com seu status.

## Serviços

Foram criados:

- `GeneroJogoService`
- `JogoService`

Os serviços utilizam injeção de dependência por construtor.

As operações de escrita utilizam `@Transactional`.

As operações somente de leitura utilizam:

`@Transactional(readOnly = true)`

## Cadastro de gênero

O `GeneroJogoService` verifica se já existe um gênero com o mesmo nome antes de realizar o cadastro.

Quando existe duplicidade, é lançada a exceção:

`RecursoDuplicadoException`

Quando um gênero não é encontrado, é utilizada:

`RecursoNaoEncontradoException`

## Cadastro de jogo

O `JogoService` verifica se o código do jogo já está cadastrado.

Depois, busca o gênero informado.

A associação entre jogo e gênero é realizada através do comportamento de domínio:

`genero.adicionarJogo(jogo)`

Após a associação, o jogo é persistido pelo `JogoRepository`.

## Transações, rollback e dirty checking

Foi criado um teste de cadastro de jogo com gênero válido.

Também foi criado um teste de falha utilizando um identificador de gênero inexistente.

Nesse cenário, o serviço lança `RecursoNaoEncontradoException` e o teste verifica que o jogo não ficou persistido.

Isso demonstra o comportamento esperado da operação transacional diante de uma falha.

Também foi demonstrado o dirty checking do JPA através da operação `receberUnidades`.

O serviço busca o jogo dentro de uma transação e executa `jogo.receberUnidades(quantidade)` sem chamar `save()` novamente.

Como a entidade está sendo gerenciada pelo JPA, a alteração é detectada e persistida ao final da transação.

## Responsabilidade das regras

As responsabilidades foram separadas da seguinte forma:

- Domínio: mantém comportamentos e regras próprias das entidades, como a associação entre gênero e jogo.
- Serviço: coordena os casos de uso, valida duplicidades e define os limites das transações.
- Banco de dados: mantém as restrições estruturais e de integridade definidas pelas migrations do Liquibase.

## Testes

Após a implementação da Aula 05, foram executados 20 testes.

Resultado:

- 20 testes executados
- 0 falhas
- 0 erros
- BUILD SUCCESS