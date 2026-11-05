# Aula 10 — Equivalência do padrão de texto normalizado

Correção pontual de 05/11/2026. Somente o contrato foi ajustado; domínio, serviços e regras de negócio permanecem intactos. Nenhum comando de escrita no Git foi executado.

## Causa

A união dos caracteres removidos por trim com o whitespace Java era mais restritiva que a execução real. `trim()` só remove caracteres das extremidades. Por isso U+2003 U+001B U+2003 permanece inteiro e não é branco para `String.isBlank()`, embora fosse rejeitado pela antiga expressão.

## Regra representada

Se T é o conjunto U+0000–U+0020 removido por `String.trim()` e W é o conjunto de `Character.isWhitespace` do Java 21, um texto normalizado fica branco exatamente quando pertence à linguagem **T* W* T***. Todo texto originalmente branco também pertence a essa linguagem. O padrão novo rejeita essa linguagem e aceita os demais textos não vazios.

A expressão usa lookahead negativo e classes explícitas, compatíveis com Java e ECMAScript. O fim absoluto é expresso por `(?![\s\S])`: `$` poderia reconhecer uma posição anterior ao último separador de linha e rejeitar indevidamente certas combinações. Não foi introduzida proibição genérica de controles internos. NBSP continua aceito, e os limites de tamanho não foram alterados.

Essa equivalência refere-se **à obrigatoriedade antes/depois de trim**. Não substitui formato de e-mail, comprimento após minúsculas Unicode, documentos fiscais, unicidade, integridade referencial nem restrições próprias da persistência. Não é promessa de equivalência integral entre toda validação HTTP/JPA e uma regex.

## Provas executadas

- `OpenApiUnicodeEquivalenceTest.padroesExportadosEquivalemAsEntidadesEmCombinacoesUnicode`: **5.539 entradas distintas**, incluindo combinações de comprimento até quatro em alfabeto de controles/espaços/texto, todos os controles U+0000–U+0020, todos os whitespaces BMP reconhecidos pelo Java 21, NBSP, separadores de linha, texto comum e caractere suplementar.
- Cada entrada é executada contra as entidades reais e comparada ao padrão efetivamente obtido em `/v3/api-docs`, para **nove campos de oito DTOs**: pessoa, criação/atualização de colaborador, gênero e jogo, além de fornecedor. O e-mail tem seu padrão de obrigatoriedade, formato e limite conferidos separadamente.
- O teste grava somente textos do corpus, resultados booleanos e padrões em `target/aula10-unicode-equivalencia.json`. Não grava documentos pessoais.
- `scripts/aula10-unicode-equivalencia.test.cjs` interpreta o padrão do **JSON servido pela aplicação**, confere que corresponde ao padrão MockMvc e compara os resultados ECMAScript com as entidades Java. Não duplica uma regex esperada hardcoded.
- `combinacoesConferemComPostPutELeituraJdbcIndependente`: 14 casos representativos em POST e PUT, com consultas SQL por conexão independente, sem transação externa de teste. Confirma persistência dos valores aceitos, ausência de registro nas rejeições e preservação do valor anterior. Casos de persistência utilizam caracteres representáveis pelo PostgreSQL.
- Caso original em HTTP real: padrão aceita; POST201; GET200 confirma o valor. Evidência sem CPF/e-mail em `target/aula10-unicode-caso-original.json`.
- O contrato anterior, preservado como artefato temporário da auditoria, divergia em **494 das 5.539 entradas**. O contrato novo apresentou zero divergências nesse corpus. Isso comprova que os testes detectam o defeito anterior, sem modificar as entidades.

## Resultados de regressão

| Verificação | Resultado |
|---|---|
| Maven completo | 178 testes, 0 falhas, 0 erros, 0 ignorados; 25 suítes |
| Node | 14 aprovados, incluindo os 12 anteriores |
| Cópia limpa de fontes | Maven: 178 testes e Node: 14 testes aprovados, sem .env/.git/classes, com DB_TEST externo e exportação real do contrato |
| Newman real | 81 requisições, 135 verificações, 0 falhas |
| HTTP de normalização anterior | 253 chamadas com resultados esperados: 162 respostas400, 87 respostas200, quatro respostas201 |
| Privacidade | Cinco provas anteriores aprovadas |
| Venda, rollback, preço histórico, estoque, filtros e paginação | Testes anteriores preservados e aprovados na suíte completa |
| OpenAPI | 38 operações, 24 caminhos, 30 schemas, oito grupos |
| YAML da Aula10 | Reexportado da aplicação; igualdade com o YAML servido confirmada |

Logs: `target/aula10-unicode-direcionados.log`, `aula10-unicode-suite-final.log`, `aula10-unicode-node.log`, `aula10-unicode-newman.log` e `aula10-unicode-http.log`. As contagens anteriores nas evidências históricas não representam esta execução.

## Execução reproduzível

1. Iniciar uma instância com profile test, exclusivamente em `jogos_test`, conforme o guia existente.
2. Executar `node scripts/aula10-exportar-contrato.cjs` para exportar o contrato real.
3. Executar ` .\mvnw.cmd test` para testar e gerar o corpus Java.
4. Executar os testes Node:

```powershell
node --test scripts/aula09-schema-validator.test.cjs scripts/aula10-contrato-gerador.test.cjs scripts/aula10-unicode-equivalencia.test.cjs
```

Os testes novos falham com instrução explícita se os artefatos necessários estiverem ausentes; não são ignorados silenciosamente. Newman e HTTP adicional seguem os comandos já documentados, sempre em testes. O validador próprio de respostas continua sendo um subconjunto de JSON Schema/OpenAPI.

## Auditoria e preservação

Itens 75 e 93 reavaliados pelas provas acima: ATENDIDOS. Matriz: **101 ATENDIDOS, 0 PARCIAIS, 0 NÃO ATENDIDOS e 1 NÃO VERIFICADO**. Item101 permanece pendente de participação humana.

Não houve saneamento de dados legados, escrita em desenvolvimento/produção, alteração das migrações 001–005 ou do YAML histórico da Aula09. Não foi implementada autenticação, concorrência ou estorno. A conclusão é de correção do achado e prontidão para nova auditoria, não de avaliação acadêmica automática.
