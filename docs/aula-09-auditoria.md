# Aula 09 — Auditoria final requisito por requisito

## Escopo e evidências

Material oficial relido integralmente: https://github.com/jeffersonarpasserini/suporteos2026/blob/main/docs/09aula/09aula.md. A cópia de consulta fica somente em target/aula09-especificacao.md. Requisitos repetidos foram relacionados às mesmas evidências; extensões opcionais e sugestões de falhas para o professor não foram convertidas em funcionalidades obrigatórias.

Referências locais: [diagnóstico inicial](aula-09-diagnostico.md), [guia/ficha/laboratório](aula-09-openapi.md), [evidências reais](aula-09-evidencias.md), [contrato gerado](aula-09-openapi.yaml), testes `OpenApiDocumentationTest` (19 casos) e `OpenApiProfilesTest` (3 casos). Os 63 testes anteriores permanecem intactos.

ATENDIDO indica evidência confirmada por inspeção e execução. NÃO VERIFICADO não é presumido como concluído. O link de commit é deliberadamente NÃO ATENDIDO porque a autora proibiu publicação e qualquer operação de escrita no Git nesta etapa.

## Matriz completa

| Nº | Requisito e seção do professor/pedido | Implementação ou evidência concreta | Situação |
|---|---|---|---|
| 1 | Apresentação: tornar o contrato explícito | YAML real, operações e schemas publicados e usados no laboratório | ATENDIDO |
| 2 | Escopo: JSON/YAML/UI, parâmetros, paginação e erros | endpoints HTTP 200; testes documentais; guia | ATENDIDO |
| 3 | Aprendizagem 1: distinguir OpenAPI/documento/UI/springdoc | guia, seção F, com os quatro papéis | ATENDIDO |
| 4 | Aprendizagem 2: paths/operações/parâmetros/respostas/schemas | guia e matriz das 18 operações; teste de referências | ATENDIDO |
| 5 | Aprendizagem 3: geração Spring MVC | starter webmvc-ui, sem WebFlux; geração real | ATENDIDO |
| 6 | Aprendizagem 4: metadados globais | OpenApiConfig e publicarMetadadosGlobaisSemInventarContatoPessoal | ATENDIDO |
| 7 | Aprendizagem 5: intenção/sucesso/erro | todasOperacoesPossuemIntencaoETagCorreta; matriz de respostas | ATENDIDO |
| 8 | Aprendizagem 6: enriquecer DTOs | onze DTOs e ApiError anotados; exemplos validados | ATENDIDO |
| 9 | Aprendizagem 7: filtros/página compreensíveis | parâmetros reais, duas especializações de página, laboratório de pesquisa | ATENDIDO |
| 10 | Aprendizagem 8: teste de contrato | OpenApiDocumentationTest, 19 testes estruturais | ATENDIDO |
| 11 | Aprendizagem 9: diagnosticar divergências | evidência inicial e tabela E do guia | ATENDIDO |
| 12 | Aprendizagem 10: transferência ao tema | Jogos API, gêneros, fornecedores e regras reais, sem Produto/GrupoProduto | ATENDIDO |
| 13 | Pré-requisitos: projeto e baseline | Java 21, Boot 4.0.8, Maven 3.9.16; Git limpo inicialmente; 63 testes aprovados | ATENDIDO |
| 14 | Etapa 1: examinar camadas/rotas/tipos/profiles | diagnóstico e matriz inicial; ApiError LocalDateTime preservado | ATENDIDO |
| 15 | Estratégia/sequência: code-first e observação prévia | integração e 63 testes antes de anotar; captura inicial info v0/pageable obrigatório | ATENDIDO |
| 16 | Checkpoint 1: dependência apropriada | springdoc-openapi-starter-webmvc-ui 3.0.3 no pom; compatibilidade justificada sem atualizar Boot | ATENDIDO |
| 17 | Checkpoint 2: documento JSON | /v3/api-docs 200, JSON interpretado em testes e HTTP | ATENDIDO |
| 18 | Checkpoint 2: documento YAML | /v3/api-docs.yaml 200, parser YAML e igualdade estrutural | ATENDIDO |
| 19 | Checkpoint 2: UI acessível e redirecionamento | /swagger-ui.html redireciona; HTML/JS 200; 18 operações renderizadas no Chrome | ATENDIDO |
| 20 | Checkpoint 3: título/descrição/versão | Jogos API, descrição do domínio, info.version 1.0.0; teste exato | ATENDIDO |
| 21 | Checkpoint 3: contato e documentação externa | contato acadêmico sem email inventado; externalDocs aponta a Siunne/Jogos | ATENDIDO |
| 22 | Checkpoint 4: tags em todos os Controllers | Jogos, Gêneros de jogos, Fornecedores, Infraestrutura; teste por operação | ATENDIDO |
| 23 | Checkpoint 5: resumo/descrição de cada operação | 18 Operation com operationIds únicos, summaries e descriptions | ATENDIDO |
| 24 | Checkpoint 5: parâmetros/corpo obrigatórios | Long id, filtros, page/size/sort e RequestBody explícitos | ATENDIDO |
| 25 | §11: todas as operações da classificação | seis operações de gênero, códigos e schemas conferidos contra a matriz | ATENDIDO |
| 26 | §11: todas as operações principais/estoque | oito operações; 404 de cadastro compara referência inexistente com handler real; exemplos 400 conferidos por operação | ATENDIDO |
| 27 | §11: fornecedores e infraestrutura reais | três operações de fornecedor e health existente; nenhum endpoint fictício/PATCH inventado | ATENDIDO |
| 28 | §11: 201 com DTO e Location | headers documentados; HTTP/Postman e navegador confirmaram Location | ATENDIDO |
| 29 | §11: 200 com schema concreto | JogoResponse/GeneroJogoResponse/FornecedorResponse e páginas corretos em application/json | ATENDIDO |
| 30 | §11: 204 sem corpo | content ausente no OpenAPI; testes existentes e DELETEs reais confirmam corpo vazio | ATENDIDO |
| 31 | §11/14: 400/404/409 com ApiError | todas as respostas documentadas referenciam ApiError; exemplos por código e comparações HTTP | ATENDIDO |
| 32 | Regras do tema: data automática | DTO de criação sem data, descrição explícita; teste anterior de cadastro preservado | ATENDIDO |
| 33 | Regras do tema: fornecedor opcional | não required, união integer/null; teste estrutural e cadastro visual null | ATENDIDO |
| 34 | Regras do tema: PUT protege campos | DTO só editável; navegador comparou código/data/saldo/status | ATENDIDO |
| 35 | Regras do tema: movimentação não idempotente | descrição e entrada repetida real 10→15→20 | ATENDIDO |
| 36 | Regras do tema: quantidades/saldo | minimum exclusivo zero documentado; testes Bean Validation e negócio; 400 observado | ATENDIDO |
| 37 | Regras do tema: status/exclusão e gênero em uso | descrições distintas; navegador obteve 409 antes de excluir jogo | ATENDIDO |
| 38 | Checkpoint 6: todos os DTOs de entrada/saída | 13 schemas; textos NotBlank com minLength=1/pattern, conferidos com validações reais e espaços Unicode | ATENDIDO |
| 39 | Checkpoint 6: preservar Bean Validation | comparação normalizada dos 16 Java existentes removeu somente Swagger e comprovou conteúdo original preservado | ATENDIDO |
| 40 | Checkpoint 6: exemplos fictícios válidos | exemplosDeTodosOsRequestsRespeitamBeanValidation; CNPJ exemplo 00000000000000 | ATENDIDO |
| 41 | Checkpoint 7: sete campos de PaginaResponse | propriedades exatas e itens concretos, teste paginasPublicamExatamenteSeteCamposETipoDosItens | ATENDIDO |
| 42 | Checkpoint 7: base zero/padrão 20/máximo 100 | schemas de parâmetros e testes antigos de comportamento preservados | ATENDIDO |
| 43 | Checkpoint 7: filtros reais/ordenação | dois filtros de gênero e cinco de jogo; lista permitida; não inventado filtro de preço | ATENDIDO |
| 44 | Checkpoint 8: ApiError real | seis campos/tipos; Map de mensagens; formato local-date-time sem falso Instant | ATENDIDO |
| 45 | Checkpoint 9: organização da UI | operations-sorter=method e tags-sorter=alpha; swagger-config testado | ATENDIDO |
| 46 | Segurança: dev/test disponíveis | OpenApiProfilesTest carrega os arquivos reais, sem DataSource | ATENDIDO |
| 47 | Segurança: produção desabilitada | JSON/YAML/UI/HTML interno/swagger-config retornam 404; nenhuma conexão prod | ATENDIDO |
| 48 | Checkpoint 10: teste publicado, não superficial | 19 testes; três novos confrontam exemplos 400/404 com MockMvc e schemas NotBlank com Bean Validation | ATENDIDO |
| 49 | Testes de profiles independentes de credenciais prod | três testes separados, auto-config de banco excluída e ausência de DataSource afirmada | ATENDIDO |
| 50 | Laboratório §18: cadastrar gênero | Swagger UI 201, ID e Location capturados | ATENDIDO |
| 51 | Laboratório §18: cadastrar jogo | Swagger UI 201, gênero capturado e fornecedor null | ATENDIDO |
| 52 | Laboratório §18: consultar jogo | Swagger UI GET 200, resposta comparada à criação | ATENDIDO |
| 53 | Laboratório §18: alterar protegido | Swagger UI PUT 200, campos protegidos preservados | ATENDIDO |
| 54 | Laboratório §18: repetir entrada | dois Execute, saldos 15/20; screenshot e respostas reais | ATENDIDO |
| 55 | Laboratório §18: pesquisar | filtros + page=0/size=5/sort=nome,asc; jogo e sete metadados conferidos | ATENDIDO |
| 56 | Laboratório §18: provocar 400/404/409 | três respostas reais pelo navegador, campos comparados ao schema ApiError | ATENDIDO |
| 57 | Laboratório §18: coleção Postman e comparação | Newman 6.2.3: 30 requisições/43 assertions/0 falhas; executor complementar verifica subconjunto explícito de schemas, incluindo multipleOf | ATENDIDO |
| 58 | Exportação §19: temporário e entrega | target/aula09-openapi.json/yaml e docs/aula-09-openapi.yaml recebidos da aplicação | ATENDIDO |
| 59 | Exportação §19: validade e consistência | JSON/YAML interpretados, igualdade estrutural e todas as referências resolvidas | ATENDIDO |
| 60 | Atividade orientada §21: ficha de oito itens | guia, seção C, retirada de estoque com evidência exata no contrato | ATENDIDO |
| 61 | Atividade orientada §21: revisão independente em dupla | ficha preparada; não houve outro estudante ou simulação de revisão humana | NÃO VERIFICADO |
| 62 | Atividade autônoma §22: adaptação completa | todas as operações documentadas, exemplos de jogos e medidas reais; laboratório usa o documento | ATENDIDO |
| 63 | Atividade autônoma §22: divergência e correção | seis divergências reais com evidência antes/depois e teste | ATENDIDO |
| 64 | Atividade autônoma §22: política de produção | guia, seção G, e testes de bloqueio por profile | ATENDIDO |
| 65 | Entregável §22: link de commit da Aula 09 | não existe: commit/publicação expressamente proibidos pela autora; não usar link da Aula 08 como substituto | NÃO ATENDIDO |
| 66 | Entregável §22: captura/gravação da interface | duas imagens antigas preservadas e nova aula-09-entrada-resposta.png com HTTP 200/saldo 20 | ATENDIDO |
| 67 | Entregáveis §22: YAML, testes, matriz e limite | YAML real, 85 testes aprovados, matriz de 18 operações e parágrafo de revisão humana | ATENDIDO |
| 68 | Rubrica: compreensão e transferência | quatro conceitos, code-first/design-first e adaptação sem renomear mecanicamente Produto | ATENDIDO |
| 69 | Rubrica: contrato, schemas, paginação | correções de exemplos e NotBlank verificadas; 18 operações/13 schemas/sete campos de página; limites do validador explicitados | ATENDIDO |
| 70 | Rubrica: verificação e segurança | testes + laboratório + exemplos fictícios + perfis; nível máximo de revisão por pares não é presumido | ATENDIDO |
| 71 | Checklist: suíte anterior e nova no PostgreSQL | 85 testes; baseline 63, todos preservados; 49 do exemplo não usado como meta | ATENDIDO |
| 72 | Checklist: execução a partir de fontes limpas | cópia sem .git/.env/classes compilou e passou os 85 testes com configuração externa; não foi executado clone Git | ATENDIDO |
| 73 | Checklist: Git diff/check/status e novos arquivos | comandos somente de leitura, inspeção adicional dos não rastreados, sem whitespace indevido | ATENDIDO |
| 74 | Checklist: segredo/target/migrações/preservação | .env/target ignorados, verificação de padrões e artefatos; nenhuma migração, Service, entidade, Mapper, Repository ou teste anterior alterado | ATENDIDO |
| 75 | Regra da autora: somente local, nenhum Git de escrita | HEAD continua 44984b1; sem staging, commit, push, tag, branch, merge ou rebase | ATENDIDO |

Contagem final das 75 unidades acima, após corrigir os cinco parciais da auditoria independente: **73 ATENDIDOS, 0 PARCIAIS, 1 NÃO ATENDIDO e 1 NÃO VERIFICADO**. Os itens 61 e 65 são pendências humanas/de entrega explicitadas, não defeitos de código a mascarar. Assim, não se declara “100% da entrega final” nem se publica automaticamente.

## Resultado final da implementação local

Não foi identificado requisito obrigatório de implementação ou teste técnico pendente neste escopo. Os sete cenários foram executados visualmente; não existe pendência de captura. A ficha aguarda revisão humana independente e a publicação/link de commit aguarda autorização futura da autora. Não há motivo para alterar código para esses dois itens.

A validação de dev/prod foi feita por testes web com propriedades reais sem banco; não se afirma ter iniciado uma conexão de produção. A reprodução foi uma cópia limpa das fontes, não uma operação Git. A checagem de padrões de segredo não equivale a auditoria de todo o histórico do repositório.

## Arquivos criados

- `src/main/java/br/com/emilly/jogos/config/OpenApiConfig.java`
- `src/test/java/br/com/emilly/jogos/api/OpenApiDocumentationTest.java`
- `src/test/java/br/com/emilly/jogos/api/OpenApiProfilesTest.java`
- `scripts/aula09-validar-http.cjs`
- `scripts/aula09-laboratorio-ui.cjs`
- `docs/aula-09-diagnostico.md`
- `docs/aula-09-openapi.md`
- `docs/aula-09-openapi.yaml`
- `docs/aula-09-evidencias.md`
- `docs/aula-09-auditoria.md`
- `docs/evidencias/aula-09-swagger-ui.png`
- `docs/evidencias/aula-09-entrada-repetida.png`

## Arquivos modificados

- `pom.xml` e `README.md`.
- `src/main/resources/application.properties` e `application-prod.properties`.
- `src/main/java/br/com/emilly/jogos/api/HealthController.java`.
- `src/main/java/br/com/emilly/jogos/controller/GeneroJogoController.java`, `JogoController.java`, `FornecedorController.java`.
- Os onze DTOs existentes em `src/main/java/br/com/emilly/jogos/dto/`: FornecedorRequest, FornecedorResponse, GeneroJogoRequest, GeneroJogoAtualizacaoRequest, GeneroJogoResponse, JogoRequest, JogoAtualizacaoRequest, JogoResponse, StatusRequest, MovimentacaoEstoqueRequest e PaginaResponse.
- `src/main/java/br/com/emilly/jogos/exception/ApiError.java`.

O inventário original abaixo descreve a implementação inicial; os arquivos adicionais desta correção estão listados ao final. Artefatos temporários, perfis de navegador, cópia de reprodução e logs estão somente em target, ignorado. A coleção existente do Postman não foi modificada nesta aula.

## Revisão após a auditoria independente

Ponto de partida: **68 ATENDIDOS, 5 PARCIAIS, 1 NÃO ATENDIDO e 1 NÃO VERIFICADO**. Os itens 26, 38, 48, 57 e 69 voltaram a ATENDIDO somente após as correções e as execuções registradas em aula-09-evidencias.md. Resultado: **73 ATENDIDOS, 0 PARCIAIS, 1 NÃO ATENDIDO, 1 NÃO VERIFICADO**. Isso não equivale a 100% da entrega.

Arquivo/método e comprovação das correções:

| Correção | Arquivo/método | Verificação concreta |
|---|---|---|
| 404 de criação | JogoController.cadastrar | exemplo404DeCadastroRefleteReferenciaInexistenteEmVezDeJogo |
| Campos não brancos | OpenApiConfig.textosObrigatorios | textosNotBlankNoContratoRejeitamVazioEEspacosComoOsDtos |
| Exemplos 400 | três Controllers existentes | exemplos400DasOperacoesCorrespondemAoHandlerReal; compara campos reais, sem comparar timestamp |
| Limites e múltiplos decimais | scripts/aula09-schema-validator.cjs | sete testes Node; rejeição explícita de 1.2345/0.001 |
| Coleção no cliente oficial de linha de comando | Newman 6.2.3 | 30 pedidos, 43 assertions, zero falhas; relatório e resumo portátil nas evidências |
| Captura do retorno de estoque | scripts/aula09-laboratorio-ui.cjs | PNG adicional mostra HTTP 200 e saldo 20; sete cenários/13 chamadas |
| Contrato real atualizado | docs/aula-09-openapi.yaml | recebido da instância test e conferido por igualdade JSON/YAML nos testes |

Arquivos desta revisão: OpenApiConfig.java; JogoController.java; GeneroJogoController.java; FornecedorController.java; OpenApiDocumentationTest.java; scripts/aula09-validar-http.cjs; scripts/aula09-laboratorio-ui.cjs; novos scripts/aula09-schema-validator.cjs e scripts/aula09-schema-validator.test.cjs; docs/aula-09-openapi.yaml; os três documentos de guia/evidência/auditoria; nova imagem docs/evidencias/aula-09-entrada-resposta.png. Nenhum Service, entidade, Repository, Mapper, migração ou teste de negócio das aulas anteriores foi alterado nesta revisão.

Os ensaios intermediários detectaram a diferença de localidade das mensagens padrão de Bean Validation e um contexto reduzido sem schemas nos testes de profiles. Os testes agora solicitam explicitamente pt-BR para comparar exemplos em português e o customizador tolera schemas ausentes. A exportação antiga foi substituída pela geração real antes da comparação final. Os resultados intermediários não são apresentados como sucesso.

O validador próprio permanece deliberadamente parcial, como explicado no guia; não há alegação de certificação integral do OpenAPI. A produção foi verificada sem DataSource, sem conectar a banco de produção. Permanecem os itens 61 e 65, dependentes de participação humana e autorização futura, respectivamente.
