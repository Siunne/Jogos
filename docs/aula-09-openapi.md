# Aula 09 — Contrato OpenAPI da Jogos API

> Registro histórico da Aula 09. A API atual evoluiu na [Aula 10](aula-10-pessoas-vendas.md), com contrato próprio em [aula-10-openapi.yaml](aula-10-openapi.yaml). As 18 operações anteriores permanecem, mas novas entradas de fornecedor agora exigem CNPJ com dígitos verificadores válidos. Os resultados e exemplos abaixo registram o comportamento daquela aula; o YAML histórico não foi sobrescrito.

## Resultado e fontes

Incremento local da Aula 09, preservando as Aulas 07/08. Material oficial: https://github.com/jeffersonarpasserini/suporteos2026/blob/main/docs/09aula/09aula.md. A leitura abrangeu checkpoints, laboratório, atividades, entregáveis, rubrica e checklist, e foi repetida na auditoria final.

Spring Boot permanece em **4.0.8**, Java em **21** e springdoc em **3.0.3**. A escolha e o estado inicial estão no [diagnóstico e matriz inicial](aula-09-diagnostico.md). A [matriz oficial de compatibilidade](https://springdoc.org/#what-is-the-compatibility-matrix-of-springdoc-openapi-with-spring-boot) associa Boot 4.0.x à linha 3.0.x. A versão 3.1.1 do exemplo existe, mas seu POM pai utiliza Boot 4.1.0; não houve atualização do framework para reproduzir esse número.

O contrato real gerado é [aula-09-openapi.yaml](aula-09-openapi.yaml). Possui **OpenAPI 3.1.0**, **info.version 1.0.0**, **11 caminhos**, **18 operações** e **13 schemas**. A versão da especificação define a linguagem do documento; info.version identifica o contrato Jogos API, não a versão do Spring Boot, Java ou aplicação Maven.

Ver também [evidências reais](aula-09-evidencias.md) e [auditoria completa](aula-09-auditoria.md). Nenhum commit, push ou tag da Aula 09 foi realizado. O link do commit da Aula 09 fica pendente por proibição expressa da autora; o commit anterior da Aula 08 não representa este incremento.

## A. Guia de execução

Pré-requisitos: Java 21, Maven Wrapper do projeto, PostgreSQL de testes disponível e variáveis locais preenchidas conforme `.env.example`. Nunca copie os valores reais para os exemplos ou documentos. O profile padrão não foi alterado.

Para explorar sem alterar desenvolvimento ou produção, execute na raiz:

```powershell
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=test' '-Dspring-boot.run.arguments=--server.port=18080 --spring.config.additional-location=file:./src/test/resources/'
```

O local adicional é necessário porque `application-test.properties` fica nos recursos de testes, fora do classpath normal da aplicação. O comando usa as credenciais DB_TEST existentes, sem mudar arquivos de conexão. Todos os cadastros do laboratório devem usar essa instância.

| Recurso | Endereço usado na validação |
|---|---|
| Swagger UI, com redirecionamento esperado | http://localhost:18080/swagger-ui.html |
| HTML final | http://localhost:18080/swagger-ui/index.html |
| JSON | http://localhost:18080/v3/api-docs |
| YAML | http://localhost:18080/v3/api-docs.yaml |
| Configuração carregada pela interface | http://localhost:18080/v3/api-docs/swagger-config |

Não há context path configurado. No perfil dev, iniciado pela autora com `.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=dev'`, a porta padrão é 8080. A disponibilidade documental de dev foi verificada automaticamente sem conexão com seu banco; o laboratório não utiliza dev.

Para executar a suíte:

```powershell
.\mvnw.cmd test
```

Para executar a coleção existente e comparar os corpos com o contrato real, mantenha a aplicação de testes aberta em outro terminal:

```powershell
node scripts/aula09-validar-http.cjs
```

O script exige Node 22+, usa por padrão localhost:18080, preserva as 30 requisições do Postman e executa seus scripts de teste. Não é Newman nem a interface do Postman. Ao usar a interface do Postman, importe `Jogos - Aula 07.postman_collection.json`, configure baseUrl para a porta da instância de testes e execute na ordem. A coleção cria suas próprias fixtures; não a execute contra desenvolvimento/produção.

Para exportar novamente a partir da aplicação, sem editar o YAML manualmente:

```powershell
Invoke-WebRequest -Uri http://localhost:18080/v3/api-docs.yaml -OutFile target/aula09-openapi.yaml
Invoke-WebRequest -Uri http://localhost:18080/v3/api-docs -OutFile target/aula09-openapi.json
```

O script HTTP também atualiza `docs/aula-09-openapi.yaml` com o YAML real recebido. A exportação versionável é conferida estruturalmente pelo teste: os metadados, paths, schemas, tags e documentação externa devem corresponder à aplicação. Apenas a URL do servidor pode variar conforme a porta da captura.

## B. Matriz completa de operações

Toda resposta de erro abaixo possui content application/json com `$ref: '#/components/schemas/ApiError'`. As respostas 204 não possuem content. Os códigos 400 dos IDs cobrem conversões inválidas para Long, além do mínimo mostrado na matriz do professor. O 409 de exclusão/alteração de jogo é condicional à detecção de conflito pelo banco; não foi inventado um relacionamento de “jogo em uso”.

| Método | Caminho | Objetivo | Sucesso | Erros | Evidência no OpenAPI: operationId |
|---|---|---|---|---|---|
| POST | `/api/generos` | Cadastrar gênero ATIVO | 201, GeneroJogoResponse e Location | 400, 409 | cadastrarGenero |
| GET | `/api/generos/{id}` | Consultar gênero | 200, GeneroJogoResponse | 400, 404 | consultarGenero |
| GET | `/api/generos` | Filtrar e paginar gêneros | 200, PaginaResponseGeneroJogoResponse | 400 | pesquisarGeneros |
| PUT | `/api/generos/{id}` | Substituir nome | 200, GeneroJogoResponse | 400, 404, 409 | alterarGenero |
| PUT | `/api/generos/{id}/status` | Ativar/inativar | 200, GeneroJogoResponse | 400, 404 | alterarStatusGenero |
| DELETE | `/api/generos/{id}` | Excluir gênero sem jogos | 204 vazio | 400, 404, 409 | excluirGenero |
| POST | `/api/jogos` | Cadastrar jogo e associações | 201, JogoResponse e Location | 400, 404, 409 | cadastrarJogo |
| GET | `/api/jogos/{id}` | Consultar jogo completo | 200, JogoResponse | 400, 404 | consultarJogo |
| GET | `/api/jogos` | Combinar filtros e página | 200, PaginaResponseJogoResponse | 400 | pesquisarJogos |
| PUT | `/api/jogos/{id}` | Substituir campos editáveis | 200, JogoResponse | 400, 404, 409 | alterarJogo |
| PUT | `/api/jogos/{id}/status` | Ativar/inativar | 200, JogoResponse | 400, 404 | alterarStatusJogo |
| POST | `/api/jogos/{id}/estoque/entradas` | Somar quantidade positiva | 200, JogoResponse | 400, 404 | entradaEstoque |
| POST | `/api/jogos/{id}/estoque/saidas` | Retirar sem saldo negativo | 200, JogoResponse | 400, 404 | saidaEstoque |
| DELETE | `/api/jogos/{id}` | Excluir fisicamente | 204 vazio | 400, 404, 409 | excluirJogo |
| POST | `/api/fornecedores` | Cadastrar fornecedor | 201, FornecedorResponse e Location | 400, 409 | cadastrarFornecedor |
| GET | `/api/fornecedores` | Listar array de fornecedores | 200, array de FornecedorResponse | — | listarFornecedores |
| GET | `/api/fornecedores/{id}` | Consultar fornecedor | 200, FornecedorResponse | 400, 404 | consultarFornecedor |
| GET | `/api/health` | Verificar atendimento HTTP | 200, text/plain `OK` | — | verificarHealth |

Para localizar uma operação no JSON, siga `paths`, o caminho exato e o método em minúsculas. No YAML use o operationId da última coluna. Não existe PATCH nem PUT/DELETE de fornecedor. Não foi criado controller de health novo; o existente foi documentado.

## Schemas, exemplos e invariantes

Os sete DTOs de entrada são JogoRequest, JogoAtualizacaoRequest, GeneroJogoRequest, GeneroJogoAtualizacaoRequest, FornecedorRequest, StatusRequest e MovimentacaoEstoqueRequest. As saídas são JogoResponse, GeneroJogoResponse, FornecedorResponse, as duas especializações de PaginaResponse e ApiError. Anotações Bean Validation anteriores foram preservadas.

Exemplo de criação (substitua generoId pelo ID criado no laboratório):

```json
{"codigo":"JOGO-ESTRELAS-001","nome":"Reino das Estrelas","unidadesDisponiveis":10,"preco":149.90,"estoqueMinimo":3,"generoId":1,"fornecedorId":null}
```

Exemplo de atualização completa:

```json
{"nome":"Reino das Estrelas — Edição Completa","preco":129.90,"estoqueMinimo":3,"generoId":1,"fornecedorId":null}
```

A data de cadastro é gerada por Jogo.novoCadastro e nunca pertence ao DTO de entrada. Código, saldo, data e status não são editáveis no PUT geral. FornecedorId é opcional e anulável; null ou ausência remove/dispensa a associação. A mudança de gênero mantém os dois lados da associação, sem expor entidades JPA.

Nome de gênero permite 100 caracteres no cadastro e 120 na atualização, exatamente como as validações existentes. A criação de jogo preserva seu DTO legado: alguns limites refletem as colunas do PostgreSQL, sem adicionar novas validações nesta aula. A atualização limita nome a 150 caracteres, preço a 16 dígitos inteiros e 2 decimais e mínimo a 15 inteiros e 3 decimais. Não foi alterado o comportamento das aulas anteriores para simplificar a documentação.

Quantidades de movimentação são obrigatórias, estritamente positivas e possuem no máximo três casas decimais. A quantidade zero é rejeitada. A saída não pode superar o saldo: retorna 400 e mantém o saldo anterior. Repetir uma entrada aplica novamente sua quantidade; é POST não idempotente. Alteração é PUT idempotente; exclusão física é DELETE. A inativação usa PUT de status para preservar cadastro e referências. Gênero com jogos não pode ser excluído: 409.

Os exemplos de fornecedor usam `00000000000000`, deliberadamente fictício. O código atual valida formato de 14 dígitos e unicidade, não dígitos verificadores de CNPJ. Nenhum dado de produção foi usado como exemplo.

## Paginação e filtros

Gêneros: nome e status. Jogos: nome, status, generoId, fornecedorId e abaixoEstoqueMinimo. Não existe filtro de preço; preco é somente um campo permitido de ordenação.

`page` começa em zero, padrão 0. `size` padrão 20, entre 1 e 100. `sort` aceita campo,asc ou campo,desc; para vários critérios repita o parâmetro, por exemplo `sort=status,asc&sort=nome,asc`. Padrão id,asc. Campos de gênero: id/nome/status. Campos de jogo: id/nome/codigo/preco/estoqueMinimo/unidadesDisponiveis/dataCadastro/status. Campo desconhecido, caminho arbitrário, direção inválida, tamanho fora do limite ou índice negativo retorna 400.

Exemplo: `/api/jogos?nome=estrelas&generoId=1&page=0&size=5&sort=nome,asc`. Os filtros são combinados no PostgreSQL, não em stream de uma lista inteira. `abaixoEstoqueMinimo=true` compara saldo estritamente menor que mínimo; false/ausência não restringe o saldo.

| Campo | Significado | Exemplo |
|---|---|---|
| conteudo | Registros desta página, no schema concreto da resposta | array de JogoResponse |
| pagina | Índice iniciado em zero | 0 |
| tamanho | Quantidade solicitada por página | 20 |
| totalElementos | Total correspondente aos filtros | 37 |
| totalPaginas | Total de páginas para esse tamanho | 2 |
| primeira | Indica primeira página | true |
| ultima | Indica última página | false |

O contrato mantém exatamente esses sete campos e não expõe Page/Pageable como corpo ou parâmetro obrigatório. Os testes validam o tipo dos elementos das duas páginas especializadas.

## Erros

ApiError preserva **LocalDateTime timestamp**, **int status**, **String error/message/path** e **Map<String,String> fields**. Seu timestamp é local, sem offset: a documentação usa o formato explícito `local-date-time`, sem fingir que o tipo é Instant ou acrescentar Z. Os exemplos por resposta possuem status/error correspondentes: o exemplo de 404 não reaproveita indevidamente status 400 do schema genérico.

400 cobre validação, parâmetros e regras inválidas; 404 cobre ausência de recurso/referência; 409 cobre nome/código/CNPJ duplicado e recursos em uso/integridade. Fields contém mensagens por campo em Bean Validation e objeto vazio nos outros cenários. Sucessos têm seu DTO e erros têm ApiError. As respostas completas observadas no laboratório e no Postman estão em [evidências](aula-09-evidencias.md).

## C. Ficha de contrato — retirada de estoque

1. **Objetivo:** retirar uma quantidade de unidades disponíveis, preservando saldo não negativo.
2. **Método e caminho:** POST `/api/jogos/{id}/estoque/saidas`.
3. **Parâmetros:** id na rota, tipo Long, obrigatório; representa um jogo existente.
4. **Entrada:** application/json, `{"quantidade":5.000}`; positiva, obrigatória, até três casas decimais.
5. **Sucesso:** 200 com JogoResponse completo; unidadesDisponiveis reduzido, código/data/status preservados.
6. **Erros:** 400 para quantidade inválida ou saldo insuficiente; 404 para jogo inexistente. Identificador malformado também é tratado como 400 pelo handler de parâmetros.
7. **Regra além dos tipos:** uma quantidade positiva pode superar o saldo. A comparação deve ocorrer antes da subtração; a rejeição não altera o saldo. Repetir POST retira novamente quando houver saldo, portanto não é idempotente.
8. **Evidência:** `paths['/api/jogos/{id}/estoque/saidas'].post`, operationId saidaEstoque; requestBody referencia MovimentacaoEstoqueRequest; 200 referencia JogoResponse; 400/404 referenciam ApiError. OpenApiDocumentationTest verifica essas relações e a suíte anterior verifica saldo persistido.

A ficha está pronta para revisão pela autora/por outro estudante. Não foi inventada uma revisão independente em dupla; essa participação humana permanece não verificada.

## D. Laboratório — sete cenários executados

O laboratório foi realizado em Chrome headless por interação real com o DOM do Swagger UI: expandir a operação, clicar Try it out, preencher campos e clicar Execute. Não se limitou a fazer fetch no contexto da página. O script descobriu as operações pelo documento carregado no Swagger UI e capturou as requisições HTTP efetivamente originadas pelos botões.

| Cenário | Execução observada | Resultado |
|---|---|---|
| 1 | POST de gênero Aventura Lab com nome exclusivo | 201, corpo e Location |
| 2 | POST de Reino das Estrelas ligado ao gênero, fornecedor null | 201, data automática e saldo 10 |
| 3 | GET por ID capturado | 200, representação igual à criação |
| 4 | PUT editável | 200, código/data/saldo/status preservados |
| 5 | Entrada de 5, repetida | 200/200, saldo 10 → 15 → 20 |
| 6 | Nome/gênero, page 0, size 5 e sort nome,asc | 200, jogo esperado e sete metadados |
| 7 | Quantidade zero; ID inexistente; nome de gênero repetido | 400/404/409, corpos comparados a ApiError |

Adicionalmente, o navegador testou gênero em uso (409), excluiu o jogo (204) e o gênero de laboratório (204), somente suas próprias fixtures. Foram **13 chamadas HTTP pela interface**, com **18 operações renderizadas**. Browser observado: Chrome 156.0.8078.80. Duas capturas originais preservadas: [visão geral](evidencias/aula-09-swagger-ui.png) e [entrada executada](evidencias/aula-09-entrada-repetida.png).

O Swagger preenche parâmetros com os exemplos do contrato. Na pesquisa do laboratório, foi necessário limpar fornecedorId e selecionar abaixoEstoqueMinimo=false, pois o jogo criado não possui fornecedor e seu saldo final está acima do mínimo. Exemplos de IDs não garantem existência de cadastros no banco. Sempre substitua pelos IDs capturados.

Para repetir visualmente, siga os sete cenários acima em banco de testes. Para automação reproduzível no Windows, abra um Chrome isolado (nunca use o perfil pessoal) com remote debugging somente local:

```powershell
$perfilAula09 = Join-Path $PWD 'target/aula09-chrome-profile'
Start-Process -FilePath 'C:/Program Files/Google/Chrome/Application/chrome.exe' -ArgumentList @('--headless=new','--remote-debugging-port=9222','--remote-debugging-address=127.0.0.1',('--user-data-dir='+$perfilAula09),'--no-first-run','--no-default-browser-check','--disable-background-networking','--disable-extensions','about:blank') -WindowStyle Hidden
node scripts/aula09-laboratorio-ui.cjs
```

O script fecha esse navegador isolado quando conclui. Não há pendência de captura automática nesta execução. A autora ainda pode produzir uma gravação narrada opcional e realizar a revisão humana da ficha.

## E. Divergências reais observadas e correções

| Evidência inicial | Consequência | Correção confirmada |
|---|---|---|
| info.title OpenAPI definition, version v0 | API não identificada | OpenApiConfig: Jogos API / 1.0.0 |
| Entrada sem summary/description, somente resposta 200 | Consumidor desconhece repetição, quantidade inválida e recurso ausente | Operation e ApiResponses explícitos; erros com ApiError |
| Parâmetro pageable obrigatório | Oculta a forma real de construir uma consulta | Parameter(hidden=true) no objeto e parâmetros page/size/sort explícitos |
| Media type application/json sem schema ao combinar inferência e anotação | Corpo correto aparecia somente sob wildcard | default-produces-media-type configurado para JSON; teste exige schema em application/json |
| Limite exclusivo zero omitido pela anotação do swagger-core | Schema aceitava zero apesar de @Positive rejeitá-lo | OpenApiCustomizer publica exclusiveMinimum numérico zero em OpenAPI 3.1 |
| Exemplo genérico de ApiError com status 400 em respostas 404/409 | Exemplo visual contradiz código da resposta | ExampleObject próprio por status; teste confere status/error |

As capturas iniciais sem anotações foram feitas antes dos metadados. A evidência portátil inclui os trechos efetivamente observados, sem fabricar uma especificação inicial.

## F. Limite da geração automática e conceitos para apresentação

OpenAPI é a linguagem padronizada; o documento é uma instância JSON/YAML dessa linguagem; springdoc examina a aplicação e produz o documento; Swagger UI o renderiza e permite experimentar. Code-first aproveita uma API existente, mas pode transformar detalhes acidentais em contrato. Design-first negocia o contrato antes do código e ajuda a preparar clientes/mocks, mas também precisa verificar que a implementação o cumpre. Nenhuma das abordagens dispensa revisão.

A geração automática reconhece tipos e parte das validações, mas não deduz regras como não idempotência, saldo insuficiente, motivo do 409 ou significado da inativação. Os testes estruturais detectam metadados, rotas e schemas ausentes, mas não provam que todo texto é pedagogicamente suficiente nem que um consumidor independente entendeu o contrato. Por isso foram combinados testes anteriores de negócio, novos testes estruturais, validação de exemplos, comparação com HTTP real e exploração visual. A revisão por outra pessoa ainda é distinta dessas verificações.

Para uma rota ausente no documento, confira primeiro seu registro/mapeamento e component scan. Para 404 do springdoc, confira dependência e configuração do perfil. Para exemplo inválido, confronte Bean Validation e o schema; não remova validação para acomodar o exemplo. Mudar somente uma anotação de resposta não altera o retorno do Controller.

## G. Segurança e profiles

`application.properties` organiza operações por método e tags alfabeticamente. Não há autenticação adicionada nesta aula. `application-prod.properties` desliga api-docs e swagger-ui. A documentação detalha a superfície da API e não deve ser publicada por acidente. Habilitá-la futuramente em produção exigiria decisão sobre público, autenticação, endpoints internos e manutenção da versão; desligá-la não substitui segurança da aplicação.

OpenApiProfilesTest carrega as propriedades reais de dev/test/prod em um contexto web sem DataSource. Dev/test respondem 200 para JSON/YAML e disponibilizam UI; prod responde 404 também para swagger-config e HTML interno. Não foram usadas credenciais ou conexões de produção. O profile padrão, variáveis de conexão, ddl-auto=validate e open-in-view=false foram preservados.

## H. Evidências e reprodução

- Baseline: 63 testes aprovados.
- Integração sem anotações: 63 aprovados; JSON/YAML/UI HTML acessíveis.
- Testes documentais e profiles: 22 testes documentais e de profiles ao final (19 + 3), incluindo três adicionados nesta correção.
- Suíte completa: **85 testes, 0 falhas, 0 erros, 0 ignorados**.
- Postman via executor HTTP: **30 requisições, 43 verificações**, corpos conferidos nas restrições implementadas pelo validador parcial.
- Laboratório visual: **7 cenários, 13 chamadas**, duas capturas originais preservadas e uma nova mostrando a resposta.
- Reprodução em cópia sem .git/.env/classes: **85 testes aprovados**, utilizando somente configuração externa do banco de testes por importação local; não foi executado git clone.

Em um clone obtido manualmente pela autora, preencher a configuração local conforme .env.example e executar o Maven Wrapper é suficiente. O YAML de entrega está em docs e participa do teste de equivalência; scripts e capturas também são arquivos de entrega. Não dependem de classes pré-compiladas de target. Logs temporários: `target/aula09-baseline.log`, `aula09-integracao-sem-anotacoes.log`, `aula09-suite-completa.log`, `aula09-postman.log`, `aula09-laboratorio-ui.log`, `aula09-reproducao.log` e relatórios Surefire.

Os requisitos locais implementáveis foram verificados. Permanecem conscientemente fora de uma declaração de “100% da entrega”: link de commit/publicação da Aula 09, proibidos pela autora, e revisão humana independente da ficha. Não é necessário modificar código para esses itens. Nenhum commit, push, tag, troca de branch ou alteração de histórico foi realizado.

## Correções da auditoria independente

A auditoria independente classificou 68 itens como atendidos, 5 parciais, 1 não atendido e 1 não verificado. As correções locais preservam os 82 testes anteriores e acrescentam três testes Java, totalizando **85**, além de sete testes Node.

- POST /api/jogos: exemplo 404 agora informa **Gênero de jogo não encontrado**, verificado contra uma requisição real com gênero inexistente.
- Exemplos 400: comparados por operação com o ApiExceptionHandler real, incluindo mensagem, path e fields. Movimentação com quantidade zero informa fields.quantidade. Exemplos de mensagens padrão de Bean Validation usam **Accept-Language: pt-BR**; outras localidades podem traduzir essas mensagens. A resposta continua com os mesmos campos e regras.
- Textos @NotBlank: minLength=1 e pattern que exige ao menos um caractere que não seja whitespace segundo Character.isWhitespace. O customizador corrige a inferência minLength=0 de @Size; nenhuma anotação de validação de negócio foi removida ou alterada. Os testes comparam o padrão com Bean Validation, inclusive espaços Unicode.
- Validador HTTP: módulo scripts/aula09-schema-validator.cjs com sete testes em scripts/aula09-schema-validator.test.cjs. Rejeita **1.2345** para multipleOf=0.001 e aceita 1.234. A verificação de múltiplos usa inteiros BigInt sobre a representação decimal de Number, evitando divisão binária imprecisa.

### Alcance real da validação HTTP

O executor próprio verifica tipos, null, enum/const, limites inclusivos e exclusivos, multipleOf, minLength/maxLength por codepoint, pattern, required, referências locais, allOf, itens/limites/unicidade de arrays, additionalProperties e os formatos date e local-date-time. Verifica também Location e ausência de corpo no 204. Nos pedidos deliberadamente inválidos, valida a resposta de erro; não exige que o pedido inválido passe no schema.

**Não é validação integral de OpenAPI/JSON Schema.** Não verifica todos os formatos, parâmetros HTTP, cabeçalhos, regras de composição, palavras-chave ou semântica de negócio. Recusa explicitamente algumas construções ainda não suportadas, como oneOf/anyOf/not, quando encontradas. Números grandes continuam sujeitos à precisão de Number ao interpretar JSON; a aritmética BigInt não recupera precisão já perdida no parsing. Os testes Java, o Newman e a inspeção humana complementam esse subconjunto.

### Newman real, somente no banco de testes

Foi executado Newman **6.2.3**, com **30 requisições, 43 assertions e zero falhas**. Não se trata apenas do executor próprio. Relatório completo temporário: target/aula09-newman.json; saída: target/aula09-newman.log. Para reproduzir, confirme antes a instância iniciada com profile test na porta 18080:

```powershell
npm.cmd exec --yes --package=newman -- newman run 'Jogos - Aula 07.postman_collection.json' --env-var baseUrl=http://localhost:18080 --reporters cli,json --reporter-json-export target/aula09-newman.json
node --test scripts/aula09-schema-validator.test.cjs
```

O npm pode precisar de acesso à rede/cache para obter o Newman. Não execute a coleção em dev/prod: ela cria fixtures e executa exclusões ao final.

### Evidência visual adicional

As duas imagens anteriores foram preservadas byte a byte. A nova [captura da resposta](evidencias/aula-09-entrada-resposta.png) mostra **HTTP 200 e unidadesDisponiveis=20** após duas entradas de 5 sobre saldo 10. Não substitui nem altera as imagens antigas. O laboratório repetiu os sete cenários e 13 chamadas pela interface real.

### Pendências de entrega preservadas

A revisão da ficha por outra pessoa permanece **NÃO VERIFICADA**. O link do commit permanece **NÃO ATENDIDO**, pois publicação/commit/tag continuam proibidos. Nenhuma dessas pendências foi convertida artificialmente em atendimento.
