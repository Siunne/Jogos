# Aula 09 — Diagnóstico e matriz inicial

## Estado antes de alterar a aplicação

Em 04/11/2026, o diretório de trabalho estava limpo e o último commit era `44984b1` (Aula 08). Nenhuma operação de escrita no Git foi autorizada ou executada na Aula 09. Java Temurin 21.0.12, Spring Boot 4.0.8, Maven Wrapper 3.9.16, Spring MVC, Bean Validation, Spring Data JPA, PostgreSQL e Liquibase 5.0.3.

Baseline executada: `.\mvnw.cmd test` — **63 testes, 0 falhas, 0 erros, 0 ignorados, BUILD SUCCESS**. Evidência local: `target/aula09-baseline.log`. Os testes anteriores foram inspecionados, inclusive persistência, associação bidirecional, paginação, rollback e contrato de criação sem data.

Pacotes: `api` (health), `controller`, `dto`, `application`, `mapper`, `domain`, `repository`, `repository.specification`, `exception` e `config`. Não há segurança/autenticação ou WebFlux no incremento.

## Tipos e regras confirmados

- Jogo: Long id; String codigo/nome; BigDecimal unidadesDisponiveis/preco/estoqueMinimo; LocalDate dataCadastro; Status; gênero obrigatório e fornecedor opcional. Data definida no servidor por Jogo.novoCadastro. PUT exclui código, saldo, data e status.
- GeneroJogo: Long id, String nome, Status, coleção bidirecional de jogos. Exclusão em uso retorna 409.
- Fornecedor: Long id, String razaoSocial/cnpj e Status. Apenas POST e GETs públicos.
- PaginaResponse: conteudo, pagina, tamanho, totalElementos, totalPaginas, primeira e ultima.
- ApiError: LocalDateTime timestamp, int status, String error/message/path e Map<String,String> fields. Não usar exemplo com UTC/Z como se o tipo fosse Instant.
- Nome de gênero: cadastro limita a 100 caracteres; atualização a 120. Esse detalhe real deve ser documentado sem uniformizar artificialmente as validações.
- Pesquisa: gênero por nome/status; jogo por nome/status/generoId/fornecedorId/abaixoEstoqueMinimo. Não existe filtro por preço. Pageable usa base zero, tamanho padrão 20, máximo 100 e valida ordenação.

## Ambientes

`dev` importa `.env` e usa variáveis DB_DEV; `test` usa DB_TEST e o PostgreSQL de testes; `prod` usa DB_URL/DB_USERNAME/DB_PASSWORD. Valores de conexão não são reproduzidos. `ddl-auto=validate` e `open-in-view=false` permanecem. O profile especial de referência de schema não é ambiente público e não será iniciado. Nenhum banco dev/prod será usado no laboratório.

## Dependência

Escolhida `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3`. A [matriz oficial](https://springdoc.org/#what-is-the-compatibility-matrix-of-springdoc-openapi-with-spring-boot) relaciona Boot 4.0.x à linha 3.0.x. Ambos os artefatos consultados existem no Maven Central (HTTP 200): o POM pai do springdoc 3.1.1 usa Boot 4.1.0; o de 3.0.3 usa Boot 4.0.5. A opção preserva Boot 4.0.8 em vez de atualizar o framework para reproduzir um número do exemplo.

## Matriz inicial de requisitos e arquivos reais

| Requisito | Local real | Plano de verificação |
|---|---|---|
| Integração MVC | pom.xml | compilar/testar e capturar contrato sem anotações |
| Metadados globais | config/OpenApiConfig.java, novo | info, contato didático, externalDocs |
| Tags | GeneroJogoController, JogoController, FornecedorController, api/HealthController | conferir todas as 18 operações |
| Operações e erros | Controllers e ApiExceptionHandler existentes | matriz de HTTP e refs ApiError |
| Criação de gênero | POST /api/generos | 201/400/409 e Location |
| Gênero por ID | GET /api/generos/{id} | 200/404 |
| Pesquisa de gêneros | GET /api/generos | 200/400, filtros nome/status e página |
| Atualização de gênero | PUT /api/generos/{id} | 200/400/404/409 |
| Status de gênero | PUT /api/generos/{id}/status | 200/400/404 |
| Exclusão de gênero | DELETE /api/generos/{id} | 204 vazio/404/409 |
| Criação de jogo | POST /api/jogos | 201/400/404/409, data do servidor e fornecedor opcional |
| Jogo por ID | GET /api/jogos/{id} | 200/404 |
| Pesquisa de jogos | GET /api/jogos | 200/400 e cinco filtros reais |
| Atualização de jogo | PUT /api/jogos/{id} | 200/400/404/409; campos protegidos |
| Status de jogo | PUT /api/jogos/{id}/status | 200/400/404 |
| Entrada/saída | POST /api/jogos/{id}/estoque/entradas e /saidas | 200/400/404, efeito acumulativo e saldo preservado no erro |
| Exclusão de jogo | DELETE /api/jogos/{id} | 204 vazio/404/409 de integridade, sem inventar vínculo inexistente |
| Fornecedores | POST e GET /api/fornecedores; GET /api/fornecedores/{id} | 201/400/409; 200; 200/404 |
| Infraestrutura | GET /api/health | 200 text/plain, corpo OK |
| Schemas | todos os 11 DTOs em dto e ApiError | propriedades, required, nulabilidade, exemplos e validações |
| Paginação | PaginaResponse e PaginacaoConfig | sete campos; page/size/sort explícitos |
| Profiles | application*.properties | dev/test habilitados; prod desabilitado sem banco de produção |
| Regressão | testes existentes e OpenApiDocumentationTest novo | preservar 63 testes e validar estrutura JSON/YAML |
| Laboratório | Swagger UI em banco de testes | sete cenários; registrar eventual limitação visual sem inventar execução |
| Exportação | target/ e docs/aula-09-openapi.yaml | exportação real e igualdade estrutural JSON/YAML |
| Entregáveis/auditoria | novos documentos da Aula 09 | ficha de oito itens, matriz, divergência observada, segurança e evidências |

Estado inicial: ainda não havia springdoc, OpenApiConfig, anotações de documentação, exportação ou testes documentais. O diagnóstico é histórico, e o estado final será registrado separadamente.
