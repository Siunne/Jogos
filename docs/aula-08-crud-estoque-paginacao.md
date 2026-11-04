# Aula 08 — CRUD, estoque e pesquisa paginada

Especificação: https://github.com/jeffersonarpasserini/suporteos2026/blob/main/docs/08aula/08aula.md

## Diagnóstico e adaptação

Projeto Java 21 / Spring Boot 4.0.8 / PostgreSQL. Camadas existentes: `domain`, `application`, `repository`, `dto`, `mapper`, `controller` e `exception`. Os cadastros e consultas anteriores foram mantidos, com a correção do contrato da data de criação do jogo descrita abaixo. Os quatro GETs de gêneros e fornecedores já existiam; faltava sua cobertura de sucesso em MockMvc e sua inclusão nos clientes e na documentação.

`GrupoProduto` corresponde a `GeneroJogo`; `Produto` corresponde a `Jogo`. No jogo, descrição é `nome`, valor unitário é `preco` e saldo é `unidadesDisponiveis`. O estoque mínimo e fornecedor opcional já estavam na migração 003. Não foram criadas entidades ou colunas para simular o domínio do professor.

## Rotas

| Método | Rota | Entrada | Saída | Sucesso | Erros previstos |
|---|---|---|---|---|---|
| POST | `/api/generos` | `{nome}` | GeneroJogoResponse | 201 + Location | 400, 409 |
| GET | `/api/generos/{id}` | ID na rota | GeneroJogoResponse | 200 | 404 |
| GET | `/api/generos` | filtros e Pageable | PaginaResponse de gêneros | 200 | 400 |
| PUT | `/api/generos/{id}` | ID e `{nome}` | GeneroJogoResponse | 200 | 400, 404, 409 |
| PUT | `/api/generos/{id}/status` | ID e `{status}` | GeneroJogoResponse | 200 | 400, 404 |
| DELETE | `/api/generos/{id}` | ID na rota | sem corpo | 204 | 404, 409 em uso |
| POST | `/api/jogos` | JogoRequest, sem data | JogoResponse | 201 + Location | 400, 404, 409 |
| GET | `/api/jogos/{id}` | ID na rota | JogoResponse | 200 | 404 |
| GET | `/api/jogos` | filtros e Pageable | PaginaResponse de jogos | 200 | 400 |
| PUT | `/api/jogos/{id}` | ID e JogoAtualizacaoRequest | JogoResponse | 200 | 400, 404 |
| PUT | `/api/jogos/{id}/status` | ID e `{status}` | JogoResponse | 200 | 400, 404 |
| POST | `/api/jogos/{id}/estoque/entradas` | ID e `{quantidade}` | JogoResponse | 200 | 400, 404 |
| POST | `/api/jogos/{id}/estoque/saidas` | ID e `{quantidade}` | JogoResponse | 200 | 400, 404 |
| DELETE | `/api/jogos/{id}` | ID na rota | sem corpo | 204 | 404 |
| POST | `/api/fornecedores` | `{razaoSocial,cnpj}` | FornecedorResponse | 201 + Location | 400, 409 |
| GET | `/api/fornecedores` | sem corpo | array de FornecedorResponse | 200 | — |
| GET | `/api/fornecedores/{id}` | ID na rota | FornecedorResponse | 200 | 404 |
| GET | `/api/health` | sem corpo | texto `OK` | 200 | — |

IDs relacionados inexistentes também retornam 404. Violações de integridade concorrentes retornam 409. O formato de erro existente (`timestamp`, `status`, `error`, `message`, `path`, `fields`) foi preservado. Não há captura genérica de todas as exceções como 400.

## Cadastro e representações completas

Os exemplos desta seção são ilustrativos: IDs variam conforme o banco, e a data exibida corresponde ao dia hipotético do cadastro. Requisições e respostas reais, sem substituir IDs ou timestamps, estão em [Evidências HTTP](aula-08-evidencias-http.md).

POST `/api/generos`, `Content-Type: application/json`:

```json
{"nome":"RPG"}
```

Resposta 201, `Location: /api/generos/2`:

```json
{"id":2,"nome":"RPG","status":"ATIVO"}
```

Esse é o formato completo de `GeneroJogoResponse`, reutilizado por GET por ID, PUT do nome e PUT de status. Por exemplo, PUT `/api/generos/2/status` com `{"status":"INATIVO"}` retorna 200 com `{"id":2,"nome":"RPG","status":"INATIVO"}`. Na pesquisa, essas representações aparecem em `conteudo`.

POST `/api/fornecedores`:

```json
{"razaoSocial":"Distribuidora de Jogos","cnpj":"12345678000199"}
```

Resposta 201, `Location: /api/fornecedores/3`:

```json
{"id":3,"razaoSocial":"Distribuidora de Jogos","cnpj":"12345678000199","status":"ATIVO"}
```

GET `/api/fornecedores/3` retorna esse mesmo JSON com 200. GET `/api/fornecedores` retorna um array dessas representações, por exemplo:

```json
[{"id":3,"razaoSocial":"Distribuidora de Jogos","cnpj":"12345678000199","status":"ATIVO"}]
```

POST `/api/jogos`:

```json
{
  "codigo":"JOGO-001",
  "nome":"The Witcher 3",
  "unidadesDisponiveis":10,
  "preco":149.90,
  "estoqueMinimo":2,
  "generoId":2,
  "fornecedorId":3
}
```

Resposta 201, `Location: /api/jogos/10`:

```json
{
  "id":10,
  "codigo":"JOGO-001",
  "nome":"The Witcher 3",
  "preco":149.90,
  "estoqueMinimo":2,
  "generoId":2,
  "generoNome":"RPG",
  "fornecedorId":3,
  "fornecedorRazaoSocial":"Distribuidora de Jogos",
  "unidadesDisponiveis":10,
  "dataCadastro":"2026-11-04",
  "status":"ATIVO"
}
```

Este é o formato completo de `JogoResponse`, também usado em GET por ID, PUT, status e movimentações. As operações retornam todos esses campos, com os valores resultantes. Nas páginas, cada elemento de `conteudo` usa o mesmo formato.

`dataCadastro` não pertence ao DTO de criação. `JogoMapper.toEntity` chama `Jogo.novoCadastro`, que define `LocalDate.now()` no servidor, segundo o fuso padrão da JVM. Não há campo editável nem setter para essa data. A hidratação JPA de registros existentes e os construtores anteriores foram preservados: não há atualização retroativa de datas, migração nova ou alteração de coluna. Um campo extra `dataCadastro` enviado por um cliente antigo é ignorado e não controla a data gravada.

O saldo inicial informado no POST continua sendo permitido pelo contrato existente. Depois do cadastro, ele só é alterado pelas movimentações. Fornecedor pode ser `null` ou omitido.

## Escolha dos métodos e códigos HTTP

PUT substitui a representação editável completa de um recurso identificado. Repetir o mesmo corpo deixa o mesmo estado final, sem movimentar estoque ou redefinir a data. Sua operação de status também é idempotente: inativar novamente mantém o estado INATIVO.

POST cria um recurso na coleção ou registra uma movimentação. Cada entrada/saída é aplicada novamente se a requisição for repetida; por isso, estoque usa POST. A criação retorna 201 com Location; a movimentação retorna 200 com o jogo atualizado.

DELETE representa exclusão física: remove o cadastro e retorna 204 sem corpo. Repetir a exclusão não recria o recurso; a tentativa seguinte recebe 404. Quando a intenção é apenas retirar o cadastro de uso, utiliza-se PUT de status: a inativação mantém a linha, o identificador e os vínculos, e pode ser revertida. Um gênero com jogos pode ser inativado, mas não removido: a exclusão conflita com as referências existentes e retorna 409.

400 representa corpo, parâmetro ou regra de negócio inválidos; 404 representa o recurso ou referência inexistente; 409 representa conflito com cadastros existentes ou vínculos que impedem exclusão. São situações distintas e não devem ser agrupadas em um 400 genérico. Os erros usam `ApiError`; exemplos completos observados estão ao final e no arquivo de evidências.

## Exemplos de atualização

PUT `/api/generos/{id}`:

```json
{"nome":"Aventura"}
```

PUT `/api/jogos/{id}` (substitui a representação editável completa):

```json
{"nome":"The Witcher 3","preco":129.90,"estoqueMinimo":4.000,"generoId":2,"fornecedorId":null}
```

`fornecedorId: null` remove a associação com o fornecedor. `generoId` é obrigatório. Os IDs do exemplo são ilustrativos; a coleção captura os IDs reais. Não há alteração de `codigo`, `unidadesDisponiveis`, `dataCadastro` ou `status` por esse DTO. Campos extras enviados para esses atributos não são mapeados nem alteram a entidade. O POST mantém o saldo inicial, mas a data passa a ser gerada exclusivamente pelo servidor.

Para o jogo 10 do exemplo anterior, o PUT acima retorna 200 com a representação completa:

```json
{
  "id":10,"codigo":"JOGO-001","nome":"The Witcher 3","preco":129.90,
  "estoqueMinimo":4,"generoId":2,"generoNome":"RPG","fornecedorId":null,
  "fornecedorRazaoSocial":null,"unidadesDisponiveis":10,
  "dataCadastro":"2026-11-04","status":"ATIVO"
}
```

Um PUT `/api/generos/2` com `{"nome":"Aventura"}` retorna 200 com `{"id":2,"nome":"Aventura","status":"ATIVO"}`. Não altera o status existente.

PUT `/api/generos/{id}/status` ou `/api/jogos/{id}/status`:

```json
{"status":"INATIVO"}
```

Também aceita `ATIVO`. Texto desconhecido ou ausência de status retorna 400.

POST `/api/jogos/{id}/estoque/entradas` ou `/api/jogos/{id}/estoque/saidas`:

```json
{"quantidade":2.500}
```

Quantidade deve ser positiva, com até três casas decimais. Saída acima do saldo retorna 400 e não altera o saldo. Repetir um POST realiza outra movimentação; PUT é idempotente. O gênero com jogos pode ser inativado, mas sua exclusão retorna 409.

## Filtros, paginação e ordenação

Gêneros: `nome` (trecho, ignorando caixa e espaços externos) e `status`.

Jogos: `nome`, `status`, `generoId`, `fornecedorId` e `abaixoEstoqueMinimo=true`. Todos são combinados por AND em Specifications no banco. O último filtro compara `unidadesDisponiveis < estoqueMinimo`; ausente ou `false` não restringe o saldo. `%` e `_` no nome são tratados como caracteres literais.

Exemplo: `GET /api/jogos?nome=Witcher&status=ATIVO&generoId=2&fornecedorId=3&abaixoEstoqueMinimo=true&page=0&size=20&sort=nome,asc`.

`page` começa em 0; padrão 0. `size` padrão 20, permitido de 1 a 100. Página negativa, tamanho fora do intervalo e parâmetros numéricos malformados retornam 400. Ordenação padrão: `id,asc`. É possível repetir `sort`, como `sort=nome,asc&sort=id,asc`.

Campos permitidos para gêneros: `id`, `nome`, `status`.

Campos permitidos para jogos: `id`, `nome`, `codigo`, `preco`, `estoqueMinimo`, `unidadesDisponiveis`, `dataCadastro`, `status`.

Campo desconhecido, caminho de associação ou direção diferente de `asc`/`desc` retorna 400.

```json
{
  "conteudo": [],
  "pagina": 0,
  "tamanho": 20,
  "totalElementos": 0,
  "totalPaginas": 0,
  "primeira": true,
  "ultima": true
}
```

As listagens de jogos e gêneros da Aula 07 passam de array para `PaginaResponse`. Fornecedores continuam com array. Respostas de gênero incluem `status`; respostas de jogo incluem também `unidadesDisponiveis`, `dataCadastro` e `status`.

## Arquitetura e migração

Comportamentos nas entidades; casos de uso transacionais nos Services; Controllers apenas recebem DTOs e produzem respostas com os Mappers. A troca de gênero atualiza ambas as coleções em memória e a referência dona da FK. O método anterior de inclusão continua impedindo a associação acidental a dois gêneros.

`JogoRepository.findAll(Specification, Pageable)` carrega gênero e fornecedor por EntityGraph. `open-in-view=false` e `ddl-auto=validate` foram mantidos.

A migração `004-unique-nome-genero-jogo.yaml` cria `uk_genero_jogo_nome_lower` sobre `LOWER(BTRIM(nome))`, com rollback `DROP INDEX`. Uma precondição interrompe a migração se encontrar duplicidades. Ela não exclui nem escolhe registros automaticamente. As migrações anteriores não foram alteradas.

Antes de aplicar em outro banco, consultar:

```sql
SELECT LOWER(BTRIM(nome)), COUNT(*)
FROM genero_jogo
GROUP BY LOWER(BTRIM(nome))
HAVING COUNT(*) > 1;
```

Se houver resultados, revisar manualmente os cadastros e suas referências antes de repetir a migração. O índice protege também inserções diretas e concorrentes; o Service antecipa conflitos por nome normalizado.

## Postman

Importar `Jogos - Aula 07.postman_collection.json`, agora com o nome “Jogos - Aulas 07 e 08”. Configurar `baseUrl` (padrão `http://localhost:8080`) e executar a coleção inteira na ordem 00–29. Os dez pedidos anteriores foram mantidos. O health check foi corrigido para a API local.

O pedido 00 gera um identificador de execução para nomes, código e CNPJ de teste. Os cadastros capturam `generoId`, `fornecedorId` e `jogoId`. Os novos pedidos cobrem consultas de gênero/fornecedor, PUTs, status, remoção/restauração de fornecedor, estoque e saldo preservado, filtros, metadados, ordenação inválida e exclusões. A tentativa de excluir gênero em uso precede a exclusão do jogo; a exclusão bem-sucedida do gênero é a última operação. O fornecedor permanece cadastrado, pois sua exclusão não faz parte da atividade.

O POST de jogo não envia data. Seu script verifica a data gerada e captura `dataCadastroJogo`; os PUTs conferem sua preservação. Os DELETEs bem-sucedidos verificam tanto 204 quanto `pm.response.text() === ""`. Executar a coleção em banco de testes, pois ela cria e modifica seus próprios cadastros.

## Verificação

Executar com PostgreSQL de testes disponível e `.env` preenchido, sem expor credenciais:

```powershell
.\mvnw.cmd '-Dtest=ConsultasAula07Test' test
.\mvnw.cmd '-Dtest=GeneroJogoTest,JogoTest,Aula08DomainTest' test
.\mvnw.cmd '-Dtest=PersistenciaJpaTest,UnicidadeGeneroTest' test
.\mvnw.cmd '-Dtest=Aula08ControllerTest' test
.\mvnw.cmd test
```

Os testes HTTP da Aula 08 não usam transação externa: verificam persistência real e mapeamento após o fechamento da transação do Service. As fixtures têm identificadores exclusivos e a limpeza remove somente os registros criados pelo próprio teste. O teste antigo de estoque negativo passou a informar o estoque mínimo para atingir efetivamente a constraint de saldo.

O teste `MigracaoGeneroTest` aplica e reverte a migração 004 em schema temporário exclusivo, insere duplicatas após o rollback e confirma que a precondição impede nova aplicação sem apagar os dois registros. O schema temporário é removido ao final. As tabelas normais do projeto não são revertidas.

### Cobertura fortalecida após a auditoria

- `cadastroGeraDataNoServidorSemAceitarDataDoCliente`: cria sem data e tenta fornecer uma data antiga; confirma 201, Location, data gerada e registro persistido.
- `putPreservaCamposControlados` e `repetirPutPreservaEstadoFinalEDataHistorica`: usam jogo com data de 2020 para comprovar que PUT não redefine nem altera a data original, mesmo após repetição.
- `paginacaoDeJogosComprovaConteudoOrdenacaoEMetadadosNasDuasPaginas`: quatro jogos exclusivos, duas páginas de tamanho 2, IDs e nomes em ordem, exatamente sete campos, todos os metadados em ambas as páginas, ordem inversa, padrão 20 e limites 0/100/101.
- `deveAtualizarUnidadesPorDirtyChecking`: executa flush/clear antes de consultar novamente.
- `naoDeveCadastrarQuandoGeneroNaoExiste`: nome corrigido para descrever a ausência de cadastro sem alegar rollback.
- `deveReverterEntradaJaEnviadaAoBancoQuandoTransacaoFalhar`: uma transação independente altera saldo de 10 para 15, envia UPDATE com flush, limpa o contexto e confirma 15 no banco. Uma referência inexistente provoca rollback; fora dessa transação, a leitura retorna 10. A limpeza remove somente as fixtures desse teste.
- Persistência negativa: assertions verificam `ConstraintViolationException`, SQLState 23505/23514 e os identificadores das constraints de código único e saldo não negativo.
- Listagens da Aula 07: assertions exigem os IDs das fixtures criadas pelo próprio teste.
- Filtro de mínimo: três jogos com saldo abaixo, igual e acima do mínimo; `false` e ausente retornam os três, `true` retorna apenas o primeiro.
- Saída válida: o saldo é relido por uma nova consulta ao repository após a resposta HTTP.

A implementação de filtros, paginação, ordenação, Services e Controllers não precisou de alteração para essas correções. As mudanças de produção ficaram no DTO de criação, Mapper e fábrica de cadastro da entidade.

## Evidencias reais apos as correcoes de 04/11/2026

Execucao real de `.\mvnw.cmd test`: **63 testes, 0 falhas, 0 erros e 0 ignorados; BUILD SUCCESS**. Conferidos os relatorios XML em `target/surefire-reports`. A etapa das quatro classes alteradas passou com 37 testes antes da suite completa.

A aplicacao iniciou com o perfil `test` na porta 18080. O Liquibase informou 18 changesets aplicados e nenhuma migracao pendente; a migracao 004, seu rollback e o bloqueio de duplicidades passaram nos testes PostgreSQL. Nenhuma migracao foi editada nesta correcao, e o desenvolvimento nao foi acessado.

A colecao atualizada passou com **30 requisicoes e 43 verificacoes**. O executor local Node enviou as requisicoes e executou os scripts Postman; esta evidencia nao corresponde a uma execucao na interface Postman/Newman. As requisicoes, respostas completas, IDs, datas e timestamps reais estao em [Evidencias HTTP](aula-08-evidencias-http.md).

### Erros observados (ApiError completo)

**22 - Saída maior que saldo** - `POST /api/jogos/335/estoque/saidas`; HTTP 400.

```json
{
  "timestamp": "2026-11-04T20:01:05.5563796",
  "status": 400,
  "error": "Bad Request",
  "message": "Unidades disponíveis insuficientes",
  "path": "/api/jogos/335/estoque/saidas",
  "fields": {}
}
```

**07 - Jogo inexistente - 404** - `GET /api/jogos/9223372036854775807`; HTTP 404.

```json
{
  "timestamp": "2026-11-04T20:01:05.4043786",
  "status": 404,
  "error": "Not Found",
  "message": "Jogo não encontrado",
  "path": "/api/jogos/9223372036854775807",
  "fields": {}
}
```

**08 - Gênero duplicado - 409** - `POST /api/generos`; HTTP 409.

```json
{
  "timestamp": "2026-11-04T20:01:05.4143811",
  "status": 409,
  "error": "Conflict",
  "message": "Nome do gênero já cadastrado",
  "path": "/api/generos",
  "fields": {}
}
```

**27 - Tentar excluir gênero em uso** - `DELETE /api/generos/494`; HTTP 409.

```json
{
  "timestamp": "2026-11-04T20:01:05.6083942",
  "status": 409,
  "error": "Conflict",
  "message": "Gênero não pode ser excluído porque possui jogos",
  "path": "/api/generos/494",
  "fields": {}
}
```

Campos de validacao sao preenchidos em `fields` quando o erro vem de Bean Validation. Nos erros de regra, recurso inexistente e conflito acima, `fields` e um objeto vazio. Falhas inesperadas nao sao convertidas indiscriminadamente em 400.
