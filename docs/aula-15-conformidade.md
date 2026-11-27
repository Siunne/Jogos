# Aula 15 — Conformidade acadêmica

Matriz adaptada ao projeto Jogos. Os sete critérios são avaliados por evidência técnica frente à descrição do nível avançado. A avaliação individual cabe ao professor. [Material oficial, seções 2, 21 e 23–29](https://github.com/jeffersonarpasserini/suporteos2026/blob/main/docs/15aula/15aula.md).

Após a correção documental da justificativa BCrypt, a matriz contém 81 requisitos. São 78 atendidos, zero parciais, um não atendido por publicação pendente e dois não verificados por dependerem de comprovação humana.

## Comandos

| Código | Execução local |
|---|---|
| B | `.\mvnw.cmd clean verify` com relatório sem portão, nas fotografias inicial e da atividade orientada |
| U | `.\mvnw.cmd "-Dtest=ContaAcessoTest" test` e `.\mvnw.cmd "-Dtest=JogoServiceUnitTest" test` |
| T | `.\mvnw.cmd "-Dtest=ContaAcessoCustoBcryptTest" test` |
| V | `.\mvnw.cmd clean verify` com portão |
| G | `.\mvnw.cmd "-Djacoco.minimum.branch.coverage=0.90" verify` |
| N | Newman por aula, comparador Aula 14 e `node --test` após atualizar os artefatos |
| D | `git diff --check`, inspeção de arquivos novos e `git status --short --branch` |

## Resultados de aprendizagem

| ID | Requisito | Evidência | Arquivo ou comando | Status | Limitação |
|---|---|---|---|---|---|
| RA01 | Distinguir verificação, validação, erro, defeito e falha | Exemplo conta/Pessoa | Estratégia | Atendido | — |
| RA02 | Classificar por custo, escopo e dependências | 34 classes classificadas com tempo e exemplos | Inventário, B | Atendido | — |
| RA03 | Aplicar Arrange–Act–Assert | Saldo anterior, retirada, exceção e saldo preservado | JogoServiceUnitTest, U | Atendido | — |
| RA04 | Escrever unidade com JUnit Jupiter | Conta criada e transições exercitadas | ContaAcessoTest, U | Atendido | — |
| RA05 | Representar partições por parametrização | Login, hash e limites de custo no Surefire | ContaAcessoTest, ContaAcessoCustoBcryptTest, U/T | Atendido | — |
| RA06 | Explicar cinco papéis de dublês | Tabela vinculada a Jogos | Estratégia | Atendido | — |
| RA07 | Escolher mock ou colaborador real | Comparação de entrada de estoque isolada e persistida | Estratégia, JogoServiceTest, V | Atendido | — |
| RA08 | Explicar custo e cache de Spring | Chave do contexto, spy e diferenças de tempo | Estratégia e inventário | Atendido | — |
| RA09 | Exercitar banco e Liquibase | Constraint de quantidade e migrações em schemas próprios | PersistenciaJpaTest, MigracaoContaAcessoTest, V | Atendido | — |
| RA10 | Testar contrato e segurança HTTP interna | JSON/YAML, filtros e decisões 401/403 | OpenApiDocumentationTest, AutorizacaoTest, V | Atendido | — |
| RA11 | Executar Red–Green–Refactor | Falha de asserção, solução mínima e padrão pré-compilado | TDD, T/V | Atendido | — |
| RA12 | Gerar e ler JaCoCo | XML, CSV, HTML e interpretação por pacote | Cobertura, B/V | Atendido | — |
| RA13 | Distinguir linhas, branches, métodos e instruções | Contadores absolutos e explicação das unidades | Cobertura | Atendido | — |
| RA14 | Impor mínimos no Maven | Regra BUNDLE e falha a 90% | POM e evidências, G | Atendido | — |
| RA15 | Escolher testes orientados por risco | Estado da conta e integridade de hash | Cobertura e TDD | Atendido | — |

## Checkpoints e entrega mínima

| ID | Requisito | Evidência | Arquivo ou comando | Status | Limitação |
|---|---|---|---|---|---|
| CP1 | Inventariar a suíte | Contagens e classificação de nove perspectivas | Inventário, B | Atendido | — |
| CP2 | Executar ContaAcesso isoladamente | 21 casos sem contexto Spring | ContaAcessoTest, U | Atendido | — |
| CP3 | Medir sem portão | XML inicial preservado a partir de 366 casos | Cobertura, B | Atendido | — |
| CP4 | Aplicar plugin e portão | Relatório e check nos limites definitivos | POM, V | Atendido | — |
| CP5 | Provocar falha controlada | Suíte verde, check rejeita 90%, nova execução aprova | Evidências, G/V | Atendido | — |
| EM1 | Unidade de domínio | Partições, mudança de estado e preservação | ContaAcessoTest, U | Atendido | — |
| EM2 | Unidade de service com Mockito | Saldo, exceção e gênero inexistente | JogoServiceUnitTest, U | Atendido | — |
| EM3 | Integração com PostgreSQL | Flush, releitura e rollback real | JogoServiceTest, V | Atendido | — |
| EM4 | HTTP com MockMvc | Validação e contrato do corpo | Aula10ControllerTest, V | Atendido | — |
| EM5 | Autorização com negativa | Sem JWT 401, operador sem perfil 403 e autorizado | AutorizacaoTest, V | Atendido | — |
| EM6 | Configurar JaCoCo | Versão oficial, prepare-agent, report e check | POM, V | Atendido | — |
| EM7 | Documentar medição inicial | Métricas globais, por pacote e por classe | Cobertura, B | Atendido | — |
| EM8 | Justificar os limites | Piso reproduzível e leitura de risco local | Cobertura | Atendido | — |
| EM9 | Novo teste pelo risco | Invariantes da conta sem depender do transporte | ContaAcessoTest, U | Atendido | — |

## Atividades

| ID | Requisito | Evidência | Arquivo ou comando | Status | Limitação |
|---|---|---|---|---|---|
| O1 | Selecionar classe sem unidade dedicada e identificar invariante | ContaAcesso ausente do inventário de base | Inventário e cobertura | Atendido | — |
| O2 | Descrever risco | Bloqueio, perfil e estado da Pessoa | Cobertura | Atendido | — |
| O3 | Escrever cenário válido | Conta e Pessoa ativas autenticáveis | ContaAcessoTest, U | Atendido | — |
| O4 | Escrever cenário inválido | Bloqueio, Pessoa inativa e perfis vazios | ContaAcessoTest, U | Atendido | — |
| O5 | Executar classe isolada | 21 casos aprovados | Evidências, U | Atendido | — |
| O6 | Executar a suíte completa antes do TDD | 391 casos aprovados | Evidências, B | Atendido | — |
| O7 | Comparar cobertura da classe | 43/44 para 44/44 linhas, 22/30 para 30/30 branches | Cobertura | Atendido | — |
| O8 | Justificar ganho em até dez linhas | Oráculos de transição e preservação | Cobertura | Atendido | — |
| A1 | Propor regra nova e oráculo independente | Faixa de geração BCrypt, quatro limites e exceção de verificação legada documentada | TDD | Atendido | — |
| A2 | RED antes da implementação | Duas falhas esperadas, com compilação e código 1 | TDD, T | Atendido | — |
| A3 | GREEN mínimo e suíte completa | Checagem de intervalo e 395 aprovações | TDD, T/B | Atendido | — |
| A4 | Melhorar design preservando comportamento | Validação única com Pattern pré-compilado | TDD, V | Atendido | — |

## Questões de revisão

| ID | Requisito | Evidência | Arquivo ou comando | Status | Limitação |
|---|---|---|---|---|---|
| Q1 | Limites de 100% de cobertura | Resposta individual 1 | aula-15-questoes.md | Atendido | — |
| Q2 | Stub e PostgreSQL | Resposta individual 2 | aula-15-questoes.md | Atendido | — |
| Q3 | Uso de SpringBootTest | Resposta individual 3 | aula-15-questoes.md | Atendido | — |
| Q4 | Fronteira MockMvc | Resposta individual 4 | aula-15-questoes.md | Atendido | — |
| Q5 | Oráculo | Resposta individual 5 | aula-15-questoes.md | Atendido | — |
| Q6 | Stub e mock | Resposta individual 6 | aula-15-questoes.md | Atendido | — |
| Q7 | Razão do RED | Resposta individual 7 | aula-15-questoes.md | Atendido | — |
| Q8 | Branches versus linhas | Resposta individual 8 | aula-15-questoes.md | Atendido | — |
| Q9 | Política do mínimo | Resposta individual 9 | aula-15-questoes.md | Atendido | — |
| Q10 | Commit e rollback | Resposta individual 10 | aula-15-questoes.md | Atendido | — |
| Q11 | 401 e 403 | Resposta individual 11 | aula-15-questoes.md | Atendido | — |
| Q12 | Timeout concorrente | Resposta individual 12 | aula-15-questoes.md | Atendido | — |
| Q13 | Interação Mockito | Resposta individual 13 | aula-15-questoes.md | Atendido | — |
| Q14 | Triagem de linha vermelha | Resposta individual 14 | aula-15-questoes.md | Atendido | — |
| Q15 | Média global e classe crítica | Resposta individual 15 | aula-15-questoes.md | Atendido | — |

## Checklist, segurança e reprodução

| ID | Requisito | Evidência | Arquivo ou comando | Status | Limitação |
|---|---|---|---|---|---|
| CL01 | Unidade sem contexto Spring desnecessário | Java em memória com Mockito somente no serviço | ContaAcessoTest, JogoServiceUnitTest, U | Atendido | — |
| CL02 | Profile test e isolamento | Banco jogos_test e validação de nome | application-test.properties, AutorizacaoTest, V | Atendido | — |
| CL03 | Asserções sobre resultados observáveis | Exceção e saldo/hash preservados | Unidades, U/T | Atendido | — |
| CL04 | Suíte completa sem regressões | Contagem de Jogos adaptada em vez dos 79 casos de referência | Evidências, V | Atendido | — |
| CL05 | Versão, agente e relatório | 0.8.16, jacoco.exec, HTML, XML e CSV | POM, V | Atendido | — |
| CL06 | Linhas e branches acima dos pisos | Portão global em 70% e 40% | Cobertura, V | Atendido | — |
| CL07 | Relatórios ignorados e sem exclusões cosméticas | target ignorado, denominador integral | POM e evidências, D | Atendido | — |
| CL08 | README e material correspondente ao código | Comandos Windows e exemplos existentes | README e guia | Atendido | — |
| CL09 | Diff e status sem temporários ou credenciais | Conferência de rastreados e novos | Evidências, D | Atendido | — |
| CL10 | Publicar unidade lógica com tag sugerida | Publicação pendente de autorização expressa | Git, D | Não atendido | Marco futuro |
| S01 | Usar dados fictícios e proteger segredos | Fixtures sintáticas, admin efêmero, sanitização HTTP | Evidências, N | Atendido | — |
| S02 | Rodar persistência só em teste | Conexão PostgreSQL 17.11 e catálogo jogos_test | Evidências, B/V/N | Atendido | — |
| S03 | Preservar filtros e testar permissões e negativas | JWT, matriz 44 × 3 e proteção de serviços | AutorizacaoTest, JwtSecurityTest, V | Atendido | — |
| S04 | Preservar histórico | Migrações 001–006, contratos e Postman versionados | Git, D | Atendido | — |
| R01 | Documentar infraestrutura, versões e diagnóstico | Wrapper, Java 21, PostgreSQL, cache e falhas usuais | Evidências e estratégia | Atendido | — |
| R02 | Reproduzir fluxos externos e Node com dados atuais | Relatórios de cada versão servida e sanitizada | Evidências, N | Atendido | — |
| H01 | Participação em dupla | Evidência técnica da atividade orientada | Cobertura | Não verificado | Depende de observação humana |
| H02 | Demonstrar entendimento e validar adequação ao domínio | Questões, exemplos e oráculos disponíveis para apresentação | Questões e estratégia | Não verificado | Depende de comprovação acadêmica |

## Sete critérios da avaliação

| ID | Requisito | Evidência | Arquivo ou comando | Status | Limitação |
|---|---|---|---|---|---|
| AV1 | Fundamentação | Classificação por custo, escopo e risco ligada à arquitetura | Estratégia e inventário | Atendido | — |
| AV2 | Testes unitários | Oráculos independentes, limites e falhas com estado preservado | Unidades, U/T | Atendido | — |
| AV3 | Integração | PostgreSQL com commit, concorrência, flush e rollback explicados | Inventário, estratégia, V | Atendido | — |
| AV4 | TDD | RED antes do código e refatoração remove índice fixo e duplicação | TDD, T/V | Atendido | — |
| AV5 | Cobertura | Comparação absoluta e percentual, riscos e portão comprovado | Cobertura, G/V | Atendido | — |
| AV6 | Reprodutibilidade | Ambiente, comandos, relatórios, diagnóstico e evidências | Guia e evidências, V/N | Atendido | — |
| AV7 | Segurança | Negativas, isolamento, credenciais fictícias e sanitização | Estratégia e evidências, V/N | Atendido | — |
