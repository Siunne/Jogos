# Aula 14 — Matriz de acesso

A tabela define a autorização. Validações de entrada, existência e regras do domínio continuam aplicáveis depois dessa decisão.

| Método e caminho | Anônimo | OPERADOR | ADMINISTRADOR |
|---|---|---|---|
| POST /api/autenticacao/login | Sim | Sim | Sim |
| GET /api/health | Sim | Sim | Sim |
| GET /v3/api-docs, /v3/api-docs.yaml, /v3/api-docs/**, /swagger-ui.html e /swagger-ui/** | Onde habilitado | Onde habilitado | Onde habilitado |
| POST /api/contas-acesso | 401 | 403 | Sim |
| PUT /api/contas-acesso/{id}/perfis | 401 | 403 | Sim |
| PUT /api/contas-acesso/{id}/status | 401 | 403 | Sim |
| GET /api/contas-acesso/me | 401 | Sim | Sim |
| PUT /api/contas-acesso/me/senha | 401 | Sim | Sim |
| GET e POST /api/generos | 401 | Sim | Sim |
| GET e PUT /api/generos/{id} | 401 | Sim | Sim |
| PUT /api/generos/{id}/status | 401 | Sim | Sim |
| DELETE /api/generos/{id} | 401 | 403 | Sim |
| GET e POST /api/jogos | 401 | Sim | Sim |
| GET e PUT /api/jogos/{id} | 401 | Sim | Sim |
| PUT /api/jogos/{id}/status | 401 | Sim | Sim |
| POST /api/jogos/{id}/estoque/entradas e /saidas | 401 | Sim | Sim |
| DELETE /api/jogos/{id} | 401 | 403 | Sim |
| GET e POST /api/fornecedores | 401 | Sim | Sim |
| GET /api/fornecedores/{id} | 401 | Sim | Sim |
| GET e POST /api/pessoas | 401 | Sim | Sim |
| GET e PUT /api/pessoas/{id} | 401 | Sim | Sim |
| PUT /api/pessoas/{id}/status | 401 | 403 | Sim |
| GET e POST /api/clientes | 401 | Sim | Sim |
| GET e PUT /api/clientes/{id} | 401 | Sim | Sim |
| GET e POST /api/colaboradores | 401 | Sim | Sim |
| GET e PUT /api/colaboradores/{id} | 401 | Sim | Sim |
| GET e POST /api/vendas | 401 | Sim | Sim |
| GET /api/vendas/{id} | 401 | Sim | Sim |
| POST /api/vendas/{id}/itens | 401 | Sim | Sim |
| DELETE /api/vendas/{vendaId}/itens/{itemId} | 401 | 403 | Sim |
| POST /api/vendas/{id}/finalizacao e /cancelamento | 401 | Sim | Sim |

A inativação de Pessoa pode impedir o login da conta associada e exige administração. A API não oferece exclusão de Pessoa, e a FK protege Pessoas vinculadas. A inativação de jogo ou gênero conserva o cadastro e é reversível. A saída de estoque ajusta o saldo, preservando o jogo. O cancelamento é uma transição de venda aberta que preserva o cadastro e seus itens. A remoção de item elimina um registro e exige administração.
