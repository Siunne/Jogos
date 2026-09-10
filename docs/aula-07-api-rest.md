# Aula 07 - API REST

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