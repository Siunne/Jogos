# Aula 07 - API REST

## Complemento das consultas e evolução na Aula 08

Os endpoints abaixo já estavam implementados e receberam testes MockMvc de sucesso e requisições na coleção:

| Método | Rota | Retorno |
|---|---|---|
| GET | `/api/generos` | 200, listagem paginada a partir da Aula 08 |
| GET | `/api/generos/{id}` | 200 ou 404 |
| GET | `/api/fornecedores` | 200, array de fornecedores |
| GET | `/api/fornecedores/{id}` | 200 ou 404 |

A partir da Aula 08, `/api/generos` e `/api/jogos` retornam um objeto com `conteudo`, `pagina`, `tamanho`, `totalElementos`, `totalPaginas`, `primeira` e `ultima`. Clientes devem ler `conteudo` em vez de consumir o corpo como array. A página começa em zero, tem tamanho padrão 20 e máximo 100. Fornecedores mantêm o array.

Exemplo: `GET /api/generos?nome=RPG&status=ATIVO&page=0&size=20&sort=nome,asc`.

A coleção existente foi preservada e ampliada; `baseUrl` aponta por padrão para `http://localhost:8080`. O health check agora consulta `/api/health`. Os cadastros capturam IDs em variáveis. Consulte [Aula 08](aula-08-crud-estoque-paginacao.md) para os novos contratos, exemplos e regras.

### Mudança do cadastro de jogo após a auditoria da Aula 08

POST `/api/jogos` deixa de receber `dataCadastro`. A data atual é definida pelo servidor e aparece somente na resposta; o cadastro continua retornando 201 com Location. Datas já gravadas são preservadas e não podem ser alteradas pelo PUT. Exemplo atualizado de entrada:

```json
{"codigo":"JOGO-001","nome":"The Witcher 3","unidadesDisponiveis":10,"preco":149.90,"estoqueMinimo":2,"generoId":2,"fornecedorId":null}
```

Os IDs são ilustrativos. A coleção utiliza os IDs reais capturados nos cadastros. Exemplos completos das respostas e erros estão na documentação da Aula 08.

## Objetivo

Implementar e testar uma API REST para o sistema de jogos, utilizando Spring Boot, validações, tratamento de exceções e testes com Postman.

## Endpoints testados

### Health Check

- Método: GET
- Endpoint: `/api/health`
- Status esperado: `200 OK`

Utilizado para verificar se a aplicação está funcionando corretamente.

### Criar gênero

- Método: POST
- Endpoint: `/api/generos`
- Status esperado: `201 Created`

Realiza o cadastro de um novo gênero de jogo.

### Criar fornecedor

- Método: POST
- Endpoint: `/api/fornecedores`
- Status esperado: `201 Created`

Realiza o cadastro de um novo fornecedor.

### Criar jogo

- Método: POST
- Endpoint: `/api/jogos`
- Status esperado: `201 Created`

Realiza o cadastro de um novo jogo relacionado a um gênero e fornecedor.

### Buscar jogo por ID

- Método: GET
- Endpoint: `/api/jogos/{id}`
- Status esperado: `200 OK`

Retorna os dados de um jogo existente pelo seu identificador.

### Listar jogos

- Método: GET
- Endpoint: `/api/jogos`
- Status esperado: `200 OK`

Retorna a lista de jogos cadastrados.

## Status HTTP utilizados

### 200 - OK

Indica que a requisição foi realizada com sucesso. Foi utilizado nas operações de consulta e listagem.

### 201 - Created

Indica que um novo recurso foi criado com sucesso. Foi utilizado nos cadastros de gênero, fornecedor e jogo.

### 400 - Bad Request

Indica que a requisição possui dados inválidos ou que o JSON enviado está malformado.

A API retorna informações sobre os campos que não passaram pelas validações.

### 404 - Not Found

Indica que o recurso solicitado não foi encontrado.

Exemplo testado: tentativa de buscar um jogo utilizando um ID inexistente.

### 409 - Conflict

Indica conflito com os dados já existentes no sistema.

Exemplo testado: tentativa de cadastrar novamente um gênero com o mesmo nome.

## Validações testadas

Foram realizados testes enviando dados inválidos para verificar as validações da API, incluindo:

- campos obrigatórios vazios;
- valores negativos;
- recurso inexistente;
- gênero duplicado;
- JSON inválido ou malformado.

## Testes

Os endpoints foram testados utilizando o Postman.

Foram verificados cenários de sucesso e de erro, incluindo os status HTTP 200, 201, 400, 404 e 409.

A Collection utilizada nos testes foi exportada e adicionada ao projeto.
