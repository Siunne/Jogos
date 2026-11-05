# Aula 11 — Inventário local

Conferido em 05/11/2026: **9 arquivos modificados e 12 novos**, total **21**. Nenhum staging ou publicação. HEAD main permanece `0a7b201c5b61ae13a2832f734aac70bcd196e301`; origin aponta para `https://github.com/Siunne/Jogos.git`.

## Modificados

- `README.md`
- `scripts/aula10-exportar-contrato.cjs`
- `src/main/java/br/com/emilly/jogos/application/JogoService.java`
- `src/main/java/br/com/emilly/jogos/application/VendaService.java`
- `src/main/java/br/com/emilly/jogos/controller/VendaController.java`
- `src/main/java/br/com/emilly/jogos/domain/Jogo.java`
- `src/main/java/br/com/emilly/jogos/exception/ApiExceptionHandler.java`
- `src/test/java/br/com/emilly/jogos/api/OpenApiComercialTest.java`
- `src/test/java/br/com/emilly/jogos/api/OpenApiDocumentationTest.java`

## Novos

- `docs/aula-11-arquivos.md`
- `docs/aula-11-auditoria.md`
- `docs/aula-11-concorrencia.md`
- `docs/aula-11-evidencias.md`
- `docs/aula-11-openapi.yaml`
- `docs/aula-11-resultados.json`
- `scripts/aula11-concorrencia-http.cjs`
- `scripts/aula11-exportar-contrato.cjs`
- `src/main/java/br/com/emilly/jogos/application/BloqueioEstoque.java`
- `src/main/java/br/com/emilly/jogos/config/OpenApiConcorrenciaConfig.java`
- `src/test/java/br/com/emilly/jogos/api/OpenApiConcorrenciaTest.java`
- `src/test/java/br/com/emilly/jogos/application/ConcorrenciaEstoqueTest.java`

## Fora dos candidatos

`.env`, `target/`, relatórios Surefire/Newman, logs, cópia experimental sem locks, caches npm e `.idea/` são temporários ou locais. `git check-ignore` confirmou os caminhos de .env, target e workspace do IntelliJ. Nenhum novo arquivo de configuração de banco foi criado. A inspeção dos candidatos não encontrou chaves privadas, tokens reconhecíveis ou credenciais literais; exemplos fiscais e nomes usados pelos testes são fictícios. Inspeção por padrões é complementar à revisão do conteúdo, não garantia universal.

## Preservação

Migrações 001–005 e master, documentos e YAMLs históricos Aula09/Aula10 e coleção Postman Aula10 permanecem idênticos ao baseline (24 arquivos comparados por SHA-256). Dois testes documentais apontam agora para a entrega Aula11, mantendo as verificações anteriores e os novos409. O exportador Aula10 ganhou somente uma proteção contra sobrescrita histórica. README foi atualizado para identificar a Aula10 publicada e a Aula11 local. Nenhuma dependência, DTO, rota ou configuração de isolamento foi alterada.
