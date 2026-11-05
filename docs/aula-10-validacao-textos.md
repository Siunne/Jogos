# Aula 10 — Validação definitiva após normalização

Correção local de 05/11/2026, posterior à conferência pré-publicação. Nenhum comando de escrita no Git foi executado. Somente `jogos_test` recebeu operações de teste.

## Causa e solução

`String.isBlank()` e `String.trim()` não usam exatamente o mesmo conjunto de caracteres. Um texto formado somente por determinados controles podia passar por `isBlank()` e tornar-se vazio após `trim()`. A validação agora verifica tanto o valor original quanto o resultado normalizado, antes da atribuição à entidade. Mantidos os limites existentes, as mensagens, o uso de trim e o comportamento de U+00A0. Não foi criada uma proibição genérica de todos os caracteres de controle: texto válido nas extremidades continua sendo normalizado normalmente.

| Campo inspecionado | Tratamento |
|---|---|
| Pessoa.nome | `RegrasComerciais.texto`: rejeita branco original e branco após trim |
| Pessoa.email | Mesmo guard; revalida também depois de `toLowerCase(Locale.ROOT)` |
| Colaborador.matricula | Regra compartilhada no domínio; service valida antes de consultar duplicidade |
| Jogo.codigo e Jogo.nome | Guard de texto obrigatório também verifica o resultado de trim |
| GeneroJogo.nome | Mesmo guard; preservado limite existente de 120 no domínio e limites dos DTOs. Service valida antes da consulta de duplicidade |
| Fornecedor.razaoSocial | Mesmo guard; fornecedor candidato validado antes da consulta de duplicidade |
| Cliente.telefone | Opcional: trim seguido de branco transforma ausência em null; limite de 30 e NBSP preservados |
| CPF/CNPJ | Não são normalizados silenciosamente: formato e verificadores continuam exigidos |
| Filtros e sort | Normalização não persiste dados; nenhuma alteração necessária |

Foi comprovado um segundo caso real: certos caracteres Unicode aumentam de tamanho ao converter para minúsculas. Um e-mail com tamanho original permitido podia ultrapassar 180 caracteres depois da conversão. A entidade agora revalida o limite normalizado antes de alterar dados. O teste HTTP confirma que a entrada passa pelo Bean Validation original, mas é rejeitada pelo domínio com HTTP 400, sem persistência. O limite público não mudou.

## Testes e evidências executadas

- `TextoNormalizadoTest`: **6 testes**, com criação/alteração, todos os controles U+0000–U+0020, nulos, vazio, espaços Unicode, combinações, NBSP, limites e preservação do estado anterior.
- `TextoNormalizadoHttpTest`: **8 testes** MockMvc com PostgreSQL. Casos negativos sem transação externa de teste; leituras posteriores usam REQUIRES_NEW e persistence context limpo. Confirma ausência de pessoa/papel após criação rejeitada e preservação de nome, e-mail, CPF, matrícula e função após atualização rejeitada.
- O teste válido de matrícula NBSP isolada usa rollback explícito da transação de teste para não consumir uma matrícula única entre execuções; faz flush/clear e consulta no PostgreSQL. Os casos negativos mantêm consultas em transações independentes.
- Teste OpenAPI adicional verifica o padrão servido para os campos obrigatórios corrigidos. O padrão do contrato combina whitespace de Java com caracteres removidos por trim. Bean Validation foi preservado; o contrato também descreve a regra de domínio após normalização.
- Node: sete testes anteriores, quatro da primeira correção e um novo teste de normalização: **12 aprovados**.
- Reprodução em cópia limpa das fontes, incluindo arquivos não rastreados e configuração externa DB_TEST: **176 testes, 0 falhas, 0 erros, 0 ignorados** (`target/aula10-textos-reproducao.log`).
- Suíte Maven: **176 testes, 0 falhas, 0 erros, 0 ignorados**, 24 suítes. Os 161 anteriores, incluindo os 85 das aulas anteriores, permanecem.
- Newman real: **81 requisições, 135 verificações, 0 falhas**.
- `scripts/aula10-textos-http.cjs`: **253 chamadas HTTP reais**, 40 entradas inválidas em quatro operações, mais verificações de e-mail/normalização e consultas. Resultado: 162 respostas400, 87 respostas200 e quatro respostas201 esperadas. NBSP e valores anteriores preservados; o sucesso posterior de cadastro comprova que o CPF/papel não foi consumido pelas tentativas inválidas.
- As cinco provas de privacidade, os oito testes de VendaService, rollback real e preço histórico continuam aprovados na suíte completa.

Relatórios temporários ignorados pelo Git: `target/aula10-textos-suite-final.log`, `target/aula10-textos-node.log`, `target/aula10-textos-newman.json`, `target/aula10-textos-newman.log`, `target/aula10-textos-http.json` e `.log`.

O teste de crescimento do e-mail foi executado antes da correção e falhou como esperado, comprovando o achado (`target/aula10-textos-email-antes.log`). Depois da correção, passou nos testes direcionados e na suíte completa. Essa falha intermediária não foi escondida nem o teste desabilitado.

## OpenAPI e reprodução

Contrato real reexportado para `docs/aula-10-openapi.yaml`: 38 operações, 24 caminhos, 30 schemas e oito grupos. JSON/YAML conferidos pelos testes, incluindo igualdade estrutural com o arquivo exportado. O YAML histórico da Aula 09 e as migrações 001–005 não foram alterados.

Com instância local iniciada em profile test e configurada para `jogos_test`:

```powershell
.\mvnw.cmd test
node --test scripts/aula09-schema-validator.test.cjs scripts/aula10-contrato-gerador.test.cjs
node scripts/aula10-textos-http.cjs
```

A coleção Newman usa as mesmas instruções já documentadas. O validador próprio continua sendo somente um subconjunto de JSON Schema/OpenAPI, sem certificação integral da especificação.

## Auditoria e limites

Itens 5 e 93 reavaliados com provas de domínio, HTTP e persistência: ATENDIDOS. Matriz: **101 ATENDIDOS, 0 PARCIAIS, 0 NÃO ATENDIDOS e 1 NÃO VERIFICADO**. O item 101 continua pendente de comprovação humana.

Não houve saneamento automático dos registros inválidos criados em auditorias anteriores de `jogos_test`; a prova de ausência de persistência se refere às novas tentativas rejeitadas e à preservação dos registros usados nos testes. Não se afirma que todo o legado já foi corrigido. Não houve acesso com escrita a desenvolvimento/produção, alteração de fornecedores legados de desenvolvimento, mudança de migração aplicada ou publicação. Concorrência, autenticação e estorno continuam fora do escopo.

## Arquivos desta correção

- src/main/java/br/com/emilly/jogos/application/FornecedorService.java
- src/main/java/br/com/emilly/jogos/application/GeneroJogoService.java
- src/main/java/br/com/emilly/jogos/config/OpenApiConfig.java
- src/main/java/br/com/emilly/jogos/domain/Fornecedor.java
- src/main/java/br/com/emilly/jogos/domain/GeneroJogo.java
- src/main/java/br/com/emilly/jogos/domain/Jogo.java
- docs/aula-10-arquivos.md
- docs/aula-10-auditoria.md
- docs/aula-10-correcoes-auditoria.md
- docs/aula-10-evidencias.md
- docs/aula-10-openapi.yaml
- docs/aula-10-pessoas-vendas.md
- docs/aula-10-validacao-textos.md
- scripts/aula10-contrato-gerador.test.cjs
- scripts/aula10-textos-http.cjs
- src/main/java/br/com/emilly/jogos/application/ColaboradorService.java
- src/main/java/br/com/emilly/jogos/config/SchemaPatterns.java
- src/main/java/br/com/emilly/jogos/domain/Cliente.java
- src/main/java/br/com/emilly/jogos/domain/Colaborador.java
- src/main/java/br/com/emilly/jogos/domain/Pessoa.java
- src/main/java/br/com/emilly/jogos/domain/RegrasComerciais.java
- src/main/java/br/com/emilly/jogos/dto/ColaboradorAtualizacaoRequest.java
- src/main/java/br/com/emilly/jogos/dto/ColaboradorRequest.java
- src/main/java/br/com/emilly/jogos/dto/PessoaRequest.java
- src/test/java/br/com/emilly/jogos/api/OpenApiComercialTest.java
- src/test/java/br/com/emilly/jogos/controller/TextoNormalizadoHttpTest.java
- src/test/java/br/com/emilly/jogos/domain/TextoNormalizadoTest.java
- src/test/java/br/com/emilly/jogos/support/TextosTeste.java

O inventário completo da Aula 10, incluindo alterações preexistentes, está em aula-10-arquivos.md.

## Ajuste posterior do padrão OpenAPI Unicode

[Correção pontual e resultados atuais](aula-10-openapi-unicode.md): padrão alinhado à ordem real de trim/isBlank, sem alterar entidades. Foram comparadas 5.539 entradas no Java e ECMAScript, com padrões reais de nove campos. Maven: 178 testes e Node: 14 testes aprovados; Newman: 81 requisições/135 verificações sem falhas. A atividade humana continua NÃO VERIFICADA.
