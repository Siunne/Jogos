# Aula 08 — Resultado da implementação local

Este documento registra a execução histórica de 03/11/2026. Após a auditoria de 04/11/2026 foram corrigidos o cadastro da data, a cobertura de paginação/rollback e as assertions. O resultado atualizado e as respostas reais estão em [documentação da Aula 08](aula-08-crud-estoque-paginacao.md) e [evidências HTTP](aula-08-evidencias-http.md).

## Validação em 03/11/2026

Ambiente: Java 21.0.12, Spring Boot 4.0.8, PostgreSQL 17.11, banco `jogos_test`, profile `test`.

| Etapa | Resultado |
|---|---|
| Suíte original | 25 testes aprovados |
| Consultas da Aula 07 | 5 testes novos aprovados |
| Domínio | 18 testes aprovados, incluindo 6 novos |
| Persistência e índice | 5 testes aprovados |
| HTTP da Aula 08 | 21 testes novos aprovados |
| Suíte completa com Maven Wrapper | 59 testes, 0 falhas, 0 erros, 0 ignorados |
| Coleção Postman via HTTP local | 30 requisições e 37 verificações aprovadas |

Comando final: `.\mvnw.cmd test`. Relatório bruto em `target/aula08-full-tests.log`; relatórios individuais em `target/surefire-reports`. O total inclui 34 testes novos de domínio, consultas HTTP e PostgreSQL, inclusive aplicação, rollback e bloqueio por duplicidade na migração.

A coleção foi exercitada por um executor Node local que envia as requisições na ordem e executa seus scripts de teste, contra uma instância temporária da aplicação na porta 18080 usando o banco de testes. Não foi utilizada a interface do Postman nem o Newman. O resultado está em `target/postman-validation.log`. A instância temporária foi encerrada. Gênero e jogo criados pela coleção foram excluídos pelas próprias requisições; os fornecedores de teste permanecem cadastrados, conforme o fluxo documentado.

Os logs de testes contêm violações de integridade e uma falha de precondição esperadas pelos cenários negativos. Elas foram verificadas por assertions e não representam falha da suíte.

## Requisitos atendidos

- Consultas de gêneros e fornecedores verificadas, documentadas e disponíveis no Postman.
- PUT, status e DELETE de gêneros e jogos; gênero em uso retorna 409.
- Entrada e saída por POST; saldo insuficiente retorna 400 sem alterar o saldo.
- Atualização de jogo sem código, saldo, data ou status; fornecedor opcional e removível da associação.
- Troca bidirecional de gênero com comportamentos explícitos de domínio.
- DTOs separados, Mappers, Services transacionais e erros padronizados.
- Índice funcional único com precondição de duplicidades e rollback verificados no PostgreSQL.
- Specifications e Pageable, filtros combinados no banco, tamanho padrão 20 e máximo 100.
- Lista permitida de ordenação e contrato próprio PaginaResponse.
- EntityGraph e Open Session in View desativado.
- Coleção anterior preservada, IDs capturados e exclusões ordenadas.
- Documentação da Aula 07 atualizada e documentação da Aula 08 criada.

Rotas, parâmetros, corpos JSON, códigos HTTP, regras e ordem dos pedidos estão em [Aula 08](aula-08-crud-estoque-paginacao.md).

## Arquivos modificados

Caminhos Java abaixo relativos a `src/main/java/br/com/emilly/jogos/`:

- `application/GeneroJogoService.java`
- `application/JogoService.java`
- `controller/GeneroJogoController.java`
- `controller/JogoController.java`
- `domain/GeneroJogo.java`
- `domain/Jogo.java`
- `dto/GeneroJogoResponse.java`
- `dto/JogoResponse.java`
- `exception/ApiExceptionHandler.java`
- `mapper/GeneroJogoMapper.java`
- `mapper/JogoMapper.java`
- `repository/GeneroJogoRepository.java`
- `repository/JogoRepository.java`

Outros arquivos modificados:

- `src/main/resources/db/changelog/db.changelog-master.yaml`
- `src/test/java/br/com/emilly/jogos/PersistenciaJpaTest.java`
- `docs/aula-07-api-rest.md`
- `Jogos - Aula 07.postman_collection.json`

## Arquivos criados

Caminhos Java abaixo relativos a `src/main/java/br/com/emilly/jogos/`:

- `application/Paginacao.java`
- `config/PaginacaoConfig.java`
- `dto/GeneroJogoAtualizacaoRequest.java`
- `dto/JogoAtualizacaoRequest.java`
- `dto/MovimentacaoEstoqueRequest.java`
- `dto/PaginaResponse.java`
- `dto/StatusRequest.java`
- `exception/RecursoEmUsoException.java`
- `repository/specification/Filtros.java`

Migração, testes e documentação:

- `src/main/resources/db/changelog/changes/004-unique-nome-genero-jogo.yaml`
- `src/test/java/br/com/emilly/jogos/MigracaoGeneroTest.java`
- `src/test/java/br/com/emilly/jogos/UnicidadeGeneroTest.java`
- `src/test/java/br/com/emilly/jogos/controller/Aula08ControllerTest.java`
- `src/test/java/br/com/emilly/jogos/controller/ConsultasAula07Test.java`
- `src/test/java/br/com/emilly/jogos/domain/Aula08DomainTest.java`
- `docs/aula-08-crud-estoque-paginacao.md`
- `docs/aula-08-verificacao.md`

Arquivos auxiliares de execução e logs foram gerados somente em `target/`, já ignorado pelo projeto.

## Atenção na revisão

A conclusão desta execução histórica foi revista pela auditoria posterior. Consulte os documentos atualizados acima para as correções e a validação atual. A verificação de desenvolvimento de 03/11/2026 permanece registrada separadamente; as correções atuais foram executadas apenas no banco de testes. Duplicidades em qualquer outro banco exigem revisão manual, sem exclusão automática.

Todas as mudanças estão apenas nos arquivos locais. Nenhum commit, branch, tag, envio ou alteração remota foi realizado.
