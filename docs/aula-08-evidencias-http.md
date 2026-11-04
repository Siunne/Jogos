# Aula 08 - Evidencias HTTP reais em 04/11/2026

Execucao observada: `2026-11-04T23:01:05.632Z`. Perfil: `test`, porta 18080, PostgreSQL de testes.

Executor: Node HTTP executando os scripts da colecao Postman; nao foi usada a interface Postman nem o Newman.

**Resultado: 30 requisicoes, 43 verificacoes aprovadas.**

Os exemplos abaixo sao capturas reais desta execucao. IDs e timestamps foram preservados; apenas a indentacao JSON foi aplicada. Nao sao exemplos hipoteticos nem dados do desenvolvimento. Os cadastros foram gerados pela propria colecao.

## 01 - Criar gênero

`POST http://localhost:18080/api/generos`

Corpo enviado:

```json
{
  "nome": "RPG 1793833264528"
}
```

Resposta: **HTTP 201**, `Location: /api/generos/494`.

```json
{
  "id": 494,
  "nome": "RPG 1793833264528",
  "status": "ATIVO"
}
```

## 02 - Criar fornecedor

`POST http://localhost:18080/api/fornecedores`

Corpo enviado:

```json
{
  "razaoSocial": "Distribuidora 1793833264528",
  "cnpj": "01793833264528"
}
```

Resposta: **HTTP 201**, `Location: /api/fornecedores/172`.

```json
{
  "id": 172,
  "razaoSocial": "Distribuidora 1793833264528",
  "cnpj": "01793833264528",
  "status": "ATIVO"
}
```

## 03 - Criar jogo

`POST http://localhost:18080/api/jogos`

Corpo enviado:

```json
{
  "codigo": "JOGO-1793833264528",
  "nome": "The Witcher 3",
  "unidadesDisponiveis": 10,
  "preco": 149.9,
  "estoqueMinimo": 2,
  "generoId": 494,
  "fornecedorId": 172
}
```

Resposta: **HTTP 201**, `Location: /api/jogos/335`.

```json
{
  "id": 335,
  "codigo": "JOGO-1793833264528",
  "nome": "The Witcher 3",
  "preco": 149.9,
  "estoqueMinimo": 2,
  "generoId": 494,
  "generoNome": "RPG 1793833264528",
  "fornecedorId": 172,
  "fornecedorRazaoSocial": "Distribuidora 1793833264528",
  "unidadesDisponiveis": 10,
  "dataCadastro": "2026-11-04",
  "status": "ATIVO"
}
```

## 04 - Buscar jogo por ID

`GET http://localhost:18080/api/jogos/335`

Sem corpo de requisicao.

Resposta: **HTTP 200**.

```json
{
  "id": 335,
  "codigo": "JOGO-1793833264528",
  "nome": "The Witcher 3",
  "preco": 149.9,
  "estoqueMinimo": 2,
  "generoId": 494,
  "generoNome": "RPG 1793833264528",
  "fornecedorId": 172,
  "fornecedorRazaoSocial": "Distribuidora 1793833264528",
  "unidadesDisponiveis": 10,
  "dataCadastro": "2026-11-04",
  "status": "ATIVO"
}
```

## 07 - Jogo inexistente - 404

`GET http://localhost:18080/api/jogos/9223372036854775807`

Sem corpo de requisicao.

Resposta: **HTTP 404**.

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

## 08 - Gênero duplicado - 409

`POST http://localhost:18080/api/generos`

Corpo enviado:

```json
{
  "nome": "RPG 1793833264528"
}
```

Resposta: **HTTP 409**.

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

## 14 - Alterar gênero

`PUT http://localhost:18080/api/generos/494`

Corpo enviado:

```json
{
  "nome": "Aventura 1793833264528"
}
```

Resposta: **HTTP 200**.

```json
{
  "id": 494,
  "nome": "Aventura 1793833264528",
  "status": "ATIVO"
}
```

## 15 - Inativar gênero

`PUT http://localhost:18080/api/generos/494/status`

Corpo enviado:

```json
{
  "status": "INATIVO"
}
```

Resposta: **HTTP 200**.

```json
{
  "id": 494,
  "nome": "Aventura 1793833264528",
  "status": "INATIVO"
}
```

## 16 - Alterar jogo

`PUT http://localhost:18080/api/jogos/335`

Corpo enviado:

```json
{
  "nome": "The Witcher 3 atualizado",
  "preco": 129.9,
  "estoqueMinimo": 20,
  "generoId": 494,
  "fornecedorId": 172
}
```

Resposta: **HTTP 200**.

```json
{
  "id": 335,
  "codigo": "JOGO-1793833264528",
  "nome": "The Witcher 3 atualizado",
  "preco": 129.9,
  "estoqueMinimo": 20,
  "generoId": 494,
  "generoNome": "Aventura 1793833264528",
  "fornecedorId": 172,
  "fornecedorRazaoSocial": "Distribuidora 1793833264528",
  "unidadesDisponiveis": 10,
  "dataCadastro": "2026-11-04",
  "status": "ATIVO"
}
```

## 17 - Remover fornecedor do jogo

`PUT http://localhost:18080/api/jogos/335`

Corpo enviado:

```json
{
  "nome": "The Witcher 3 atualizado",
  "preco": 129.9,
  "estoqueMinimo": 20,
  "generoId": 494,
  "fornecedorId": null
}
```

Resposta: **HTTP 200**.

```json
{
  "id": 335,
  "codigo": "JOGO-1793833264528",
  "nome": "The Witcher 3 atualizado",
  "preco": 129.9,
  "estoqueMinimo": 20,
  "generoId": 494,
  "generoNome": "Aventura 1793833264528",
  "fornecedorId": null,
  "fornecedorRazaoSocial": null,
  "unidadesDisponiveis": 10,
  "dataCadastro": "2026-11-04",
  "status": "ATIVO"
}
```

## 18 - Restaurar fornecedor do jogo

`PUT http://localhost:18080/api/jogos/335`

Corpo enviado:

```json
{
  "nome": "The Witcher 3 atualizado",
  "preco": 129.9,
  "estoqueMinimo": 20,
  "generoId": 494,
  "fornecedorId": 172
}
```

Resposta: **HTTP 200**.

```json
{
  "id": 335,
  "codigo": "JOGO-1793833264528",
  "nome": "The Witcher 3 atualizado",
  "preco": 129.9,
  "estoqueMinimo": 20,
  "generoId": 494,
  "generoNome": "Aventura 1793833264528",
  "fornecedorId": 172,
  "fornecedorRazaoSocial": "Distribuidora 1793833264528",
  "unidadesDisponiveis": 10,
  "dataCadastro": "2026-11-04",
  "status": "ATIVO"
}
```

## 19 - Alterar status do jogo

`PUT http://localhost:18080/api/jogos/335/status`

Corpo enviado:

```json
{
  "status": "INATIVO"
}
```

Resposta: **HTTP 200**.

```json
{
  "id": 335,
  "codigo": "JOGO-1793833264528",
  "nome": "The Witcher 3 atualizado",
  "preco": 129.9,
  "estoqueMinimo": 20,
  "generoId": 494,
  "generoNome": "Aventura 1793833264528",
  "fornecedorId": 172,
  "fornecedorRazaoSocial": "Distribuidora 1793833264528",
  "unidadesDisponiveis": 10,
  "dataCadastro": "2026-11-04",
  "status": "INATIVO"
}
```

## 20 - Entrada de estoque

`POST http://localhost:18080/api/jogos/335/estoque/entradas`

Corpo enviado:

```json
{
  "quantidade": 5
}
```

Resposta: **HTTP 200**.

```json
{
  "id": 335,
  "codigo": "JOGO-1793833264528",
  "nome": "The Witcher 3 atualizado",
  "preco": 129.9,
  "estoqueMinimo": 20,
  "generoId": 494,
  "generoNome": "Aventura 1793833264528",
  "fornecedorId": 172,
  "fornecedorRazaoSocial": "Distribuidora 1793833264528",
  "unidadesDisponiveis": 15,
  "dataCadastro": "2026-11-04",
  "status": "INATIVO"
}
```

## 21 - Saída de estoque

`POST http://localhost:18080/api/jogos/335/estoque/saidas`

Corpo enviado:

```json
{
  "quantidade": 2
}
```

Resposta: **HTTP 200**.

```json
{
  "id": 335,
  "codigo": "JOGO-1793833264528",
  "nome": "The Witcher 3 atualizado",
  "preco": 129.9,
  "estoqueMinimo": 20,
  "generoId": 494,
  "generoNome": "Aventura 1793833264528",
  "fornecedorId": 172,
  "fornecedorRazaoSocial": "Distribuidora 1793833264528",
  "unidadesDisponiveis": 13,
  "dataCadastro": "2026-11-04",
  "status": "INATIVO"
}
```

## 22 - Saída maior que saldo

`POST http://localhost:18080/api/jogos/335/estoque/saidas`

Corpo enviado:

```json
{
  "quantidade": 999
}
```

Resposta: **HTTP 400**.

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

## 23 - Conferir saldo após recusa

`GET http://localhost:18080/api/jogos/335`

Sem corpo de requisicao.

Resposta: **HTTP 200**.

```json
{
  "id": 335,
  "codigo": "JOGO-1793833264528",
  "nome": "The Witcher 3 atualizado",
  "preco": 129.9,
  "estoqueMinimo": 20,
  "generoId": 494,
  "generoNome": "Aventura 1793833264528",
  "fornecedorId": 172,
  "fornecedorRazaoSocial": "Distribuidora 1793833264528",
  "unidadesDisponiveis": 13,
  "dataCadastro": "2026-11-04",
  "status": "INATIVO"
}
```

## 24 - Pesquisar jogos com filtros

`GET http://localhost:18080/api/jogos?nome=Witcher&status=INATIVO&generoId=494&fornecedorId=172&abaixoEstoqueMinimo=true`

Sem corpo de requisicao.

Resposta: **HTTP 200**.

```json
{
  "conteudo": [
    {
      "id": 335,
      "codigo": "JOGO-1793833264528",
      "nome": "The Witcher 3 atualizado",
      "preco": 129.9,
      "estoqueMinimo": 20,
      "generoId": 494,
      "generoNome": "Aventura 1793833264528",
      "fornecedorId": 172,
      "fornecedorRazaoSocial": "Distribuidora 1793833264528",
      "unidadesDisponiveis": 13,
      "dataCadastro": "2026-11-04",
      "status": "INATIVO"
    }
  ],
  "pagina": 0,
  "tamanho": 20,
  "totalElementos": 1,
  "totalPaginas": 1,
  "primeira": true,
  "ultima": true
}
```

## 25 - Paginação e ordenação

`GET http://localhost:18080/api/jogos?page=0&size=1&sort=nome,asc&generoId=494`

Sem corpo de requisicao.

Resposta: **HTTP 200**.

```json
{
  "conteudo": [
    {
      "id": 335,
      "codigo": "JOGO-1793833264528",
      "nome": "The Witcher 3 atualizado",
      "preco": 129.9,
      "estoqueMinimo": 20,
      "generoId": 494,
      "generoNome": "Aventura 1793833264528",
      "fornecedorId": 172,
      "fornecedorRazaoSocial": "Distribuidora 1793833264528",
      "unidadesDisponiveis": 13,
      "dataCadastro": "2026-11-04",
      "status": "INATIVO"
    }
  ],
  "pagina": 0,
  "tamanho": 1,
  "totalElementos": 1,
  "totalPaginas": 1,
  "primeira": true,
  "ultima": true
}
```

## 26 - Ordenação inválida

`GET http://localhost:18080/api/jogos?sort=inexistente,asc`

Sem corpo de requisicao.

Resposta: **HTTP 400**.

```json
{
  "timestamp": "2026-11-04T20:01:05.597404",
  "status": 400,
  "error": "Bad Request",
  "message": "Campo de ordenação inválido: inexistente",
  "path": "/api/jogos",
  "fields": {}
}
```

## 27 - Tentar excluir gênero em uso

`DELETE http://localhost:18080/api/generos/494`

Sem corpo de requisicao.

Resposta: **HTTP 409**.

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

## 28 - Excluir jogo

`DELETE http://localhost:18080/api/jogos/335`

Sem corpo de requisicao.

Resposta: **HTTP 204**.

Corpo vazio (`""`), confirmado pelo script de teste.

## 29 - Excluir gênero

`DELETE http://localhost:18080/api/generos/494`

Sem corpo de requisicao.

Resposta: **HTTP 204**.

Corpo vazio (`""`), confirmado pelo script de teste.
