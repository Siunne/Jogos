# Aula 08 — Correções após a auditoria de 04/11/2026

## Escopo e resultado real

Foram corrigidas as pendências de implementação, cobertura e documentação identificadas na auditoria. As funcionalidades corretas de manutenção, filtros e paginação foram preservadas. Nenhuma configuração ou migração foi alterada nesta etapa. O banco de desenvolvimento não foi acessado nem modificado.

| Verificação executada | Resultado observado |
|---|---|
| Classes alteradas: HTTP, Service, persistência e consultas da Aula 07 | 37 testes, zero falhas/erros/ignorados |
| `.\mvnw.cmd test` | **63 testes, zero falhas, zero erros, zero ignorados; BUILD SUCCESS** |
| Aplicação no perfil `test`, porta 18080 | Inicialização concluída com Tomcat e `Started JogosApplication` |
| Liquibase no PostgreSQL de testes | 18 changesets reconhecidos, nenhuma migração pendente |
| Migração 004 | Índice funcional, rollback e precondição de duplicidade aprovados nos testes |
| Coleção atualizada | **30 requisições e 43 verificações aprovadas** |
| `git diff --check` | Sem erro de whitespace; somente avisos de conversão LF/CRLF |

Os XMLs em `target/surefire-reports` foram conferidos. A coleção foi executada contra HTTP real por um executor Node que executa seus scripts Postman. Não foi utilizada a interface do Postman nem o Newman. A captura completa dos cenários selecionados está em [evidências HTTP](aula-08-evidencias-http.md), incluindo IDs e timestamps reais. A instância temporária foi encerrada ao final.

## Encerramento das pendências

| Pendência da auditoria | Correção e comprovação |
|---|---|
| Data de cadastro enviada pelo cliente | Campo removido de JogoRequest; Mapper chama Jogo.novoCadastro, que usa LocalDate.now no servidor. MockMvc confirma 201 e data persistida, inclusive na tentativa de fornecer data antiga. |
| Preservação de datas existentes | PUT é testado com fixture datada de 2020, inclusive com repetição. Não existe atualização retroativa nem alteração de coluna. |
| Paginação de jogos pouco demonstrada | Teste próprio com quatro jogos, duas páginas, IDs, nomes, ordem crescente/decrescente e os sete metadados; padrão e limites também verificados. |
| Dirty checking só em memória | Teste existente passou a executar flush/clear antes da releitura. |
| Teste denominado rollback sem alteração prévia | Renomeado; novo teste comprova UPDATE enviado ao PostgreSQL e rollback posterior, com leitura independente. |
| Assertions genéricas de persistência | Substituídas por ConstraintViolationException, SQLState e nome da constraint. |
| Listagens verificavam qualquer registro | Agora exigem os IDs criados pelas próprias fixtures. |
| Filtro false não discriminava dados | Cenário com saldo abaixo, igual e acima do mínimo, comparando true, false e ausência do parâmetro. |
| Saída válida sem releitura | Teste consulta o saldo persistido após a resposta HTTP. |
| Exclusões no Postman | Scripts verificam 204 e corpo vazio. |
| Documentação incompleta | Contratos com entrada/saída, exemplos completos, justificativas de métodos/códigos e erros ApiError observados. Evidências HTTP salvas em docs, sem depender somente de target. |

## Nova comparação com os requisitos obrigatórios

Esta matriz agrupa exigências repetidas da especificação oficial, preservando a adaptação ao tema Jogos. A referência é `docs/08aula/08aula.md` do repositório do professor, consultada novamente nesta etapa.

| Seções da especificação | Requisito verificado | Evidência atual | Situação |
|---|---|---|---|
| 1–3, 10 | PUT, status e DELETE de gênero/jogo | Controllers, Services e testes HTTP de sucesso/erro | ATENDIDO |
| 1.3, 5 | DTO editável sem código, saldo, data e status | JogoAtualizacaoRequest e putPreservaCamposControlados | ATENDIDO |
| 1.3 | Data de criação definida no servidor | Jogo.novoCadastro e cadastroGeraDataNoServidorSemAceitarDataDoCliente | ATENDIDO |
| 2, 17 | Semântica PUT/POST/DELETE e códigos | Documentação central e capturas de 200/201/204/400/404/409 | ATENDIDO |
| 3 | Comportamentos, validações e associação consistente | Métodos das entidades, testes de domínio e HTTP | ATENDIDO |
| 3.3 | Fornecedor opcional e removível | associarFornecedor(null), DTO e testes | ATENDIDO |
| 3.4, 7.5 | Movimentação positiva, rejeição de saldo insuficiente | DTO, entidade e testes de saldo persistido | ATENDIDO |
| 4 | Índice normalizado com rollback e diagnóstico prévio | Migração 004, UnicidadeGeneroTest e MigracaoGeneroTest | ATENDIDO |
| 5 | DTOs próprios de alteração/status/estoque | DTOs específicos e @Valid nos Controllers | ATENDIDO |
| 6 | Repositories ampliados e controle de uso/duplicidade | JpaSpecificationExecutor, consultas de existência e exclusão do próprio ID | ATENDIDO |
| 6.3, 15 | Relações carregadas sem OSIV | EntityGraph e testes HTTP sem transação externa | ATENDIDO |
| 7, 16C | Transações e dirty checking | Anotações nos Services, flush/clear e teste de rollback real | ATENDIDO |
| 7.3 | Gênero com jogos retorna 409 | existsByGeneroId, RecursoEmUsoException e teste HTTP | ATENDIDO |
| 8, 16D | Dois filtros de gênero e cinco de jogo, combináveis no banco | Specifications e cenário com cinco registros incompatíveis | ATENDIDO |
| 9 | Pageable: base zero, padrão 20 e máximo 100 | Resolver existente e testes de limites | ATENDIDO |
| 9.3 | Ordenação por lista permitida | Paginacao.validar e testes de campo/caminho/direção inválidos | ATENDIDO |
| 9.4 | PaginaResponse com exatamente sete campos | Record e teste de duas páginas de jogos, além do teste de gêneros | ATENDIDO |
| 10 | Controllers HTTP e Mappers | Sem regras de negócio nos Controllers; respostas DTO | ATENDIDO |
| 11 | Erros padronizados sem converter tudo em 400 | ApiExceptionHandler e exemplos reais de ApiError | ATENDIDO |
| 13 | Domínio, Service, PostgreSQL e MockMvc | Suíte completa: 63 testes aprovados | ATENDIDO |
| 13.4–13.6 | Filtros discriminantes, metadados, estado após erro e exclusões | Assertions específicas e consultas independentes | ATENDIDO |
| 14 | Coleção com cenários negativos, IDs capturados e exclusões ao final | 30 pedidos preservados, 43 verificações aprovadas | ATENDIDO |
| 16A/E, 17 | Contratos, exemplos, justificativas e respostas observadas | Documentação central e evidências HTTP em docs | ATENDIDO |
| 17 | Tema próprio e pelo menos dez testes novos | Entidades Jogos/Gêneros e cinco classes novas com 37 testes, mais um teste novo de Service | ATENDIDO |
| 20 | Suíte e conferência de diff | Maven e git diff --check executados | ATENDIDO |
| Complemento Aula 07 | Quatro GETs implementados, documentados e testados | ConsultasAula07Test e pedidos 10–13 do Postman | ATENDIDO |

As sugestões de extensão da seção 21, como histórico de movimentações e locking otimista, não são obrigatórias e não foram adicionadas. A ordem histórica dos checkpoints não pode ser reconstruída retroativamente pelo código; isso foi um limite de evidência da auditoria, não uma funcionalidade ausente a ser recriada.

## Limites e proteção de dados

A migração foi revalidada no banco de testes. O resultado de desenvolvimento de 03/11 permanece um registro histórico separado, sem nova consulta ou alteração nesta etapa. A suíte aplica rollback/limpeza apenas sobre suas fixtures ou schema temporário no PostgreSQL de testes.

`.env` continua ignorado e não rastreado; a família rastreada contém somente `.env.example`. Uma varredura dos arquivos atuais elegíveis ao versionamento não detectou os padrões de tokens conhecidos e chaves privadas inspecionados. Isso não equivale a auditoria completa de segredos em todo o histórico Git, que não foi realizada. Nenhuma credencial foi copiada para a documentação ou para as evidências HTTP.

Não restou pendência obrigatória de implementação, testes ou documentação identificada neste escopo. Todas as alterações permanecem locais, para revisão e envio manual pela autora.

## Arquivos alterados nesta correção

- `src/main/java/br/com/emilly/jogos/dto/JogoRequest.java`
- `src/main/java/br/com/emilly/jogos/domain/Jogo.java`
- `src/main/java/br/com/emilly/jogos/mapper/JogoMapper.java`
- `src/test/java/br/com/emilly/jogos/controller/Aula08ControllerTest.java`
- `src/test/java/br/com/emilly/jogos/controller/ConsultasAula07Test.java`
- `src/test/java/br/com/emilly/jogos/application/JogoServiceTest.java`
- `src/test/java/br/com/emilly/jogos/PersistenciaJpaTest.java`
- `Jogos - Aula 07.postman_collection.json`
- `docs/aula-07-api-rest.md`
- `docs/aula-08-crud-estoque-paginacao.md`
- `docs/aula-08-verificacao.md` (identificado como registro histórico)

Criados: este relatório e `docs/aula-08-evidencias-http.md`. Logs e auxiliares temporários estão em `target/`.
