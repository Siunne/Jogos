# Aula 08 — Validação final no desenvolvimento

Verificação executada em 03/11/2026, exclusivamente no projeto local.

## Configuração e conexão

O perfil `dev` utiliza as propriedades de conexão existentes e a importação local de `.env`. Os parâmetros foram resolvidos sem imprimir URL, usuário ou senha. Nenhuma configuração foi alterada. Permanecem `spring.jpa.hibernate.ddl-auto=validate` e `spring.jpa.open-in-view=false`.

Uma conexão JDBC de leitura confirmou acesso ao PostgreSQL 17 do desenvolvimento. O carregamento inicial do driver no verificador auxiliar precisou ser corrigido para utilizar o JAR executável em vez do JAR de fontes; não foi um problema do banco nem exigiu alteração no projeto.

## Liquibase

Antes da inicialização:

- Os 17 changesets das migrações 001, 002 e 003 estavam registrados como `EXECUTED`.
- O changeset `004-01-unique-nome-normalizado-genero-jogo` estava pendente.
- O índice `uk_genero_jogo_nome_lower` ainda não existia.
- A consulta de duplicidades por `LOWER(BTRIM(nome))` retornou zero grupos duplicados.

A aplicação foi iniciada pelo Maven Wrapper com o perfil `dev`, na porta temporária 18081, sem alterar os arquivos de configuração. O Liquibase aplicou a migração 004 normalmente em 19 ms. A inicialização concluiu com Tomcat ativo e a mensagem `Started JogosApplication`, sem erros, falhas de checksum ou necessidade de correções.

Depois da inicialização, uma nova consulta JDBC confirmou:

- 18 changesets registrados, todos como `EXECUTED`.
- A migração 004 registrada exatamente uma vez.
- Índice `uk_genero_jogo_nome_lower` presente no PostgreSQL.
- Zero grupos de nomes duplicados normalizados.

Não foram excluídos nem alterados cadastros de desenvolvimento. Não houve recriação, limpeza ou rollback desse banco. As alterações no desenvolvimento foram apenas as executadas normalmente pelo Liquibase para a migração autorizada.

## Endpoints no desenvolvimento

Foram executadas 23 requisições HTTP, todas GET, com assertions de status e conteúdo. Os IDs de consulta foram obtidos das listagens existentes.

| Verificação | Resultado |
|---|---|
| `GET /api/health` | 200, corpo `OK` |
| `GET /api/generos` e `/api/jogos` | 200, contrato de página e tamanho padrão 20 |
| `GET /api/fornecedores` | 200, array |
| GET por ID de gênero, jogo e fornecedor existentes | 200, identificadores correspondentes |
| Paginação das duas listagens, páginas 0 e 1, tamanho 1 | 200, metadados corretos |
| Ordenação `nome,asc` e `id,desc` nas duas listagens | 200 |
| Filtros ATIVO e INATIVO nas duas listagens | 200, todos os itens retornados com o status solicitado |
| Nome e status de gênero combinados | 200, registro esperado encontrado |
| Filtros combinados de jogo usando os atributos do registro consultado | 200, registro esperado encontrado |
| `abaixoEstoqueMinimo=true` | 200, todos os resultados com saldo menor que o mínimo |
| Campo de ordenação inexistente nas duas listagens | 400 esperado |
| Tamanho 101, página negativa e status inválido | 400 esperado |

O teste combinado de jogo utiliza nome, status, gênero, indicador de estoque e fornecedor quando o jogo consultado possui fornecedor. A cobertura automatizada no banco de testes verifica explicitamente todos os filtros combinados, incluindo fornecedor.

## Operações de escrita e suíte completa

As operações de escrita foram verificadas exclusivamente no banco de testes pelo comando solicitado:

```powershell
.\mvnw.cmd test
```

Resultado real: **BUILD SUCCESS — 59 testes, 0 falhas, 0 erros, 0 ignorados**.

A classe `Aula08ControllerTest` executou 21 testes, incluindo PUT de gênero e jogo, troca de gênero, remoção de fornecedor, preservação dos campos controlados, ativação e inativação, entrada e saída de estoque, recusa de saída acima do saldo, exclusões, conflitos e filtros. Também passaram os testes de domínio, persistência no PostgreSQL, consultas da Aula 07 e migração.

O teste existente de migração executa aplicação e rollback apenas em um schema temporário exclusivo no banco de testes. Ele não reverte migrações existentes nem altera o schema de desenvolvimento.

## Evidências locais

- `target/validacao-dev-inicializacao.log`: inicialização com perfil dev e aplicação da 004.
- `target/validacao-dev-endpoints.log`: resultado das 23 consultas HTTP.
- `target/validacao-dev-endpoints.json`: resumo estruturado, sem conteúdo dos cadastros.
- `target/validacao-final-maven.log`: execução completa do Maven Wrapper.
- `target/surefire-reports/`: relatórios individuais dos testes.

A instância temporária do perfil dev foi encerrada ao final. Código da aplicação, configurações de conexão e migrações não precisaram de alterações. Foram criados somente este relatório e auxiliares de verificação em `target/`. Nenhuma operação de escrita no Git ou no GitHub foi executada.

## Pendências

Nenhum erro ou pendência obrigatória identificado nas verificações executadas. As escritas foram validadas no banco de testes, preservando os cadastros reais do desenvolvimento.
