# Aula 10 — Correções da auditoria independente

Verificação local de 05/11/2026. Os seis achados foram confirmados nos arquivos antes das alterações. Nenhuma regra comercial foi relaxada e nenhuma migração aplicada foi editada. As operações com escrita utilizaram somente `jogos_test`.

## Correções e provas

| Achado | Correção | Evidência executada |
|---|---|---|
| Unicidade de e-mail com falso positivo possível | Duas inserções com CPFs distintos e mesmo e-mail normalizado; CHECK testado sem depender de duplicidade | `MigracaoComercialTest.emailNormalizadoDuplicadoViolaExatamenteIndiceUnico`: SQLState 23505 e índice esperado. `emailNaoNormalizadoViolaExatamenteCheck`: 23514 e CHECK esperado; contagem confirma rejeição |
| Unicode no contrato | Constante `SchemaPatterns.NAO_BRANCO`, compartilhada pelos DTOs novos e pela estratégia anterior | `OpenApiComercialTest.notBlankComercialEquivaleAoContratoNasFronteirasUnicode`; teste Node interpreta o mesmo padrão em JavaScript. Inclui U+00A0, U+2007 e U+202F, que não são whitespace para `String.isBlank`, além de espaços que são rejeitados |
| Dados pessoais em diagnósticos JDBC | `PersistencePrivacyFilter` registrado globalmente em `logback-spring.xml`; suprime mensagem/argumentos/stack trace brutos, emite SQLState e somente nomes de restrições conhecidos | Três testes unitários e dois integrados com violações reais no PostgreSQL; captura sem CPF, CNPJ, e-mail, nome e razão social. Metadados 23505 e restrições continuam presentes |
| Fornecedor documentado incorretamente | Exemplo fiscal válido; descrição informa que não existe operação de alteração de status | Bean fiscal verificado no teste OpenAPI; contrato real reexportado |
| CPF Postman com sequência repetida | Bases repetidas descartadas antes dos verificadores; preenchimento de bases curtas e tratamento de CPFs consecutivos | Testes determinísticos do script real, incluindo epoch 1793000000000, todos os dez dígitos repetidos e amostra de horários; Newman real aprovado |
| Nome de restrição divergente | Anotação JPA alinhada ao nome existente no Liquibase | `parVendaJogoPossuiMesmoNomeNoJpaEBloqueiaDuplicidadeNoBanco`: confere anotação e duplicação real do par com SQLState 23505; 005 não alterada |

## Resultados da primeira rodada de correções (histórico)

- Compilação: `BUILD SUCCESS`.
- Maven: **161 testes, 0 falhas, 0 erros, 0 ignorados**; 22 suítes. Relatório `target/aula10-correcao-suite-final.log` e XMLs Surefire. São nove casos adicionais aos 152 anteriores; os 85 das Aulas 07–09 permanecem na suíte.
- Node: **11 testes, 11 aprovados, 0 falhas**: sete anteriores e quatro novos. Log `target/aula10-correcao-node.log`.
- Newman real: **81 requisições, 135 verificações, 0 falhas**. Relatórios `target/aula10-correcao-newman.log` e `.json`. Preservadas as requisições e verificações anteriores.
- Contrato: **38 operações, 24 caminhos, 30 schemas, oito grupos**. JSON e YAML gerados pela aplicação de testes; igualdade estrutural confirmada no Maven. O contrato histórico da Aula 09 permanece intacto.
- `scripts/aula10-conferir-newman.cjs`: 81 respostas reais, cobrindo as 38 operações, verificadas pelo subconjunto explícito de JSON Schema implementado. Não constitui certificação integral OpenAPI.
- Swagger UI, JSON e YAML: HTTP 200. Profiles dev/test/prod verificados pelos três testes existentes, sem DataSource de produção. A instância HTTP iniciou com profile test; não foi iniciada uma instância dev/prod.
- Os oito testes de `VendaServiceTest`, incluindo rollback após flush e preço histórico persistido, continuam aprovados.
- Reprodução em cópia de fontes sem `.git`, `.env` ou classes compiladas, com configuração externa somente DB_TEST: **161 testes, 0 falhas, 0 erros, 0 ignorados**; `target/aula10-correcao-reproducao.log`.
- `git diff --check`: sem erros. Há avisos de conversão LF/CRLF, sem correção automática de finais de linha.

## Alcance da proteção de logs

O filtro intercepta os diagnósticos JDBC atuais e antigos do Hibernate e eventos com `SQLException` encadeada, inclusive quando transmitida como argumento. Ele não tenta mascarar cada valor por expressão regular: descarta o conteúdo bruto e publica apenas metadados permitidos. Nomes desconhecidos de restrições são omitidos. SQL, bind e extração do Hibernate também são bloqueados, mesmo em DEBUG/TRACE. A impressão de SQL diretamente em stdout foi desabilitada em dev; isso não altera conexão nem dados do banco. Erros não JDBC continuam sendo registrados normalmente.

Os testes integrados provocam conflitos em transações descartadas no banco de testes; seus documentos sintéticos não são reproduzidos neste relatório. A resposta HTTP de integridade continua genérica no `ApiExceptionHandler`, sem mudança de contrato. Nenhuma dependência foi acrescentada.

Essa proteção vale para a configuração de logging entregue e os caminhos testados. Não substitui revisão de novos loggers, auditoria de dependências ou controle de acesso a logs. Uma configuração externa que substitua o Logback, impressão manual de dados ou um logger que pré-formate uma exceção JDBC como string fora desses caminhos precisa de análise própria. Não é uma garantia universal sobre qualquer código futuro.

## Auditoria reavaliada

Os itens 52, 75, 79, 93 e 96 passam a ATENDIDO pelas provas acima. Resultado: **101 ATENDIDOS, 0 PARCIAIS, 0 NÃO ATENDIDOS, 1 NÃO VERIFICADO**. O item 101 continua sem comprovação de participação/apresentação humana. Essa classificação não atribui nota nem substitui nova auditoria independente.

Permanecem: saneamento autorizado dos CNPJs legados, participação humana e os limites de concorrência/segurança já explicitados na aula. Não foi aplicado saneamento em desenvolvimento ou produção. Nenhum commit, push, tag ou operação de escrita no Git foi realizado.

## Reprodução dos testes das correções

```powershell
.\mvnw.cmd test
node --test scripts/aula09-schema-validator.test.cjs scripts/aula10-contrato-gerador.test.cjs
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=test' '-Dspring-boot.run.arguments=--server.port=18080 --spring.config.additional-location=file:./src/test/resources/'
```

Em outro terminal, com a instância comprovadamente em `jogos_test`:

```powershell
node scripts/aula10-exportar-contrato.cjs
npm.cmd exec --yes --package=newman -- newman run postman/Jogos-Aula10.postman_collection.json --env-var baseUrl=http://localhost:18080 --reporters cli,json --reporter-json-export target/aula10-correcao-newman.json
node scripts/aula10-conferir-newman.cjs
```

Não executar a coleção em dev/prod: ela cria e modifica fixtures e preserva vendas de teste como histórico. Logs e resultados temporários ficam em `target/`, ignorado pelo Git.

## Correção posterior: validação após normalização

A conferência seguinte encontrou texto obrigatório vazio após trim. A correção abrange Pessoa, Colaborador e os mesmos normalizadores de Jogo, GeneroJogo e Fornecedor, além do telefone opcional e do limite de e-mail após minúsculas. [Provas e arquivos](aula-10-validacao-textos.md). Resultado atual:176 testes Maven,12 Node e Newman: 81 requisições/135 verificações sem falhas;253 verificações HTTP reais concluídas. Itens5 e93 reavaliados, item101 permanece NÃO VERIFICADO. O comentário do teste de geração fiscal foi corrigido sem alterar seu algoritmo.

## Ajuste posterior do padrão OpenAPI Unicode

[Correção pontual e resultados atuais](aula-10-openapi-unicode.md): padrão alinhado à ordem real de trim/isBlank, sem alterar entidades. Foram comparadas 5.539 entradas no Java e ECMAScript, com padrões reais de nove campos. Maven: 178 testes e Node: 14 testes aprovados; Newman: 81 requisições/135 verificações sem falhas. A atividade humana continua NÃO VERIFICADA.
