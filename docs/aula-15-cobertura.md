# Aula 15 — Cobertura e risco

JaCoCo 0.8.16 anexou o agente na JVM do Surefire com `prepare-agent` e registrou execução de bytecode. Na fase `verify`, o goal `report` combina execução, classes e fontes. O goal `check` calcula o piso global com os dados de execução e classes. As medições inicial e final usam `clean verify`, com portão apenas nas execuções finais. Nenhuma exclusão foi definida. [Histórico de versões JaCoCo](https://www.jacoco.org/jacoco/trunk/doc/changes.html).

Linhas contam fontes com bytecode exercitado. Branches representam alternativas de decisões e podem permanecer descobertos dentro de uma linha executada. Instruções contam bytecode, métodos e classes contam execução ao menos uma vez e complexidade estima caminhos independentes de decisão. Branches não medem MC/DC, exaustividade nem qualidade do oráculo.

## Limites do portão

O `CheckMojo` do JaCoCo 0.8.16 pode pular a análise se o arquivo de dados de execução ou o diretório de classes estiver ausente. Uma razão indefinida pode ficar sem violação. A execução auditada tinha dados válidos, classes mensuráveis e confirmação no log de que `jacoco:check` avaliou os limites. A configuração atende ao material acadêmico. Em uma futura automação, é recomendável exigir artefatos atualizados, denominadores positivos e confirmação explícita da execução do portão. [CheckMojo 0.8.16](https://github.com/jacoco/jacoco/blob/v0.8.16/jacoco-maven-plugin/src/org/jacoco/maven/CheckMojo.java) · [Avaliação de limites](https://github.com/jacoco/jacoco/blob/v0.8.16/org.jacoco.report/src/org/jacoco/report/check/Limit.java)

## Agregado

| Métrica | Total inicial | Coberto inicial | Ausente inicial | % inicial | Total final | Coberto final | Ausente final | % final | Δ pp |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| Instruções | 7321 | 7061 | 260 | 96,45% | 7326 | 7081 | 245 | 96,66% | 0,21 |
| Branches | 660 | 573 | 87 | 86,82% | 660 | 581 | 79 | 88,03% | 1,21 |
| Linhas | 1592 | 1545 | 47 | 97,05% | 1594 | 1548 | 46 | 97,11% | 0,07 |
| Complexidade | 792 | 691 | 101 | 87,25% | 793 | 700 | 93 | 88,27% | 1,02 |
| Métodos | 462 | 446 | 16 | 96,54% | 463 | 447 | 16 | 96,54% | 0,01 |
| Classes | 108 | 107 | 1 | 99,07% | 108 | 107 | 1 | 99,07% | 0,00 |

## Pacotes

Os nomes estão relativos a `br.com.emilly.jogos`. `n/a` indica contador ausente ou sem denominador.

| Unidade | Linhas iniciais | Linhas finais | Δ pp | Branches iniciais | Branches finais | Δ pp |
|---|---:|---:|---:|---:|---:|---:|
| `raiz` | 1/3 (33,33%) | 1/3 (33,33%) | 0,00 | n/a | n/a | n/a |
| `api` | 2/2 (100,00%) | 2/2 (100,00%) | 0,00 | n/a | n/a | n/a |
| `api.validation` | 6/6 (100,00%) | 6/6 (100,00%) | 0,00 | 12/12 (100,00%) | 12/12 (100,00%) | 0,00 |
| `application` | 338/354 (95,48%) | 338/354 (95,48%) | 0,00 | 78/96 (81,25%) | 78/96 (81,25%) | 0,00 |
| `config` | 358/358 (100,00%) | 358/358 (100,00%) | 0,00 | 213/238 (89,50%) | 213/238 (89,50%) | 0,00 |
| `controller` | 118/126 (93,65%) | 118/126 (93,65%) | 0,00 | n/a | n/a | n/a |
| `domain` | 352/356 (98,88%) | 355/358 (99,16%) | 0,29 | 159/182 (87,36%) | 167/182 (91,76%) | 4,40 |
| `dto` | 52/52 (100,00%) | 52/52 (100,00%) | 0,00 | 13/14 (92,86%) | 13/14 (92,86%) | 0,00 |
| `exception` | 47/47 (100,00%) | 47/47 (100,00%) | 0,00 | 2/2 (100,00%) | 2/2 (100,00%) | 0,00 |
| `mapper` | 90/94 (95,74%) | 90/94 (95,74%) | 0,00 | 4/4 (100,00%) | 4/4 (100,00%) | 0,00 |
| `repository` | n/a | n/a | n/a | n/a | n/a | n/a |
| `repository.specification` | 31/39 (79,49%) | 31/39 (79,49%) | 0,00 | 31/38 (81,58%) | 31/38 (81,58%) | 0,00 |
| `security` | 150/155 (96,77%) | 150/155 (96,77%) | 0,00 | 61/74 (82,43%) | 61/74 (82,43%) | 0,00 |

## Classes críticas

| Unidade | Linhas iniciais | Linhas finais | Δ pp | Branches iniciais | Branches finais | Δ pp |
|---|---:|---:|---:|---:|---:|---:|
| `Venda` | 51/52 (98,08%) | 51/52 (98,08%) | 0,00 | 19/26 (73,08%) | 19/26 (73,08%) | 0,00 |
| `ItemVenda` | 12/13 (92,31%) | 12/13 (92,31%) | 0,00 | n/a | n/a | n/a |
| `Jogo` | 71/72 (98,61%) | 71/72 (98,61%) | 0,00 | 17/20 (85,00%) | 17/20 (85,00%) | 0,00 |
| `BloqueioEstoque` | 8/8 (100,00%) | 8/8 (100,00%) | 0,00 | 1/2 (50,00%) | 1/2 (50,00%) | 0,00 |
| `ContaAcesso` | 43/44 (97,73%) | 46/46 (100,00%) | 2,27 | 22/30 (73,33%) | 30/30 (100,00%) | 26,67 |
| `ContaAcessoService` | 47/47 (100,00%) | 47/47 (100,00%) | 0,00 | 16/20 (80,00%) | 16/20 (80,00%) | 0,00 |
| `AutenticacaoService` | 9/9 (100,00%) | 9/9 (100,00%) | 0,00 | n/a | n/a | n/a |
| `TokenService` | 25/25 (100,00%) | 25/25 (100,00%) | 0,00 | n/a | n/a | n/a |
| `JwtConfig` | 39/39 (100,00%) | 39/39 (100,00%) | 0,00 | 29/38 (76,32%) | 29/38 (76,32%) | 0,00 |
| `ClaimsOriginaisJwtDecoder` | 39/39 (100,00%) | 39/39 (100,00%) | 0,00 | 20/22 (90,91%) | 20/22 (90,91%) | 0,00 |
| `SecurityConfig` | 51/51 (100,00%) | 51/51 (100,00%) | 0,00 | 6/6 (100,00%) | 6/6 (100,00%) | 0,00 |
| `ContaAcessoUserDetailsService` | 13/15 (86,67%) | 13/15 (86,67%) | 0,00 | 2/2 (100,00%) | 2/2 (100,00%) | 0,00 |
| `AdministracaoContas` | 14/14 (100,00%) | 14/14 (100,00%) | 0,00 | 8/10 (80,00%) | 8/10 (80,00%) | 0,00 |
| `AdministradorInicialRunner` | 0/7 (0,00%) | 0/7 (0,00%) | 0,00 | 0/2 (0,00%) | 0/2 (0,00%) | 0,00 |
| `AdministradorInicialService` | 17/17 (100,00%) | 17/17 (100,00%) | 0,00 | 5/6 (83,33%) | 5/6 (83,33%) | 0,00 |
| `JogoService` | 48/54 (88,89%) | 48/54 (88,89%) | 0,00 | 6/8 (75,00%) | 6/8 (75,00%) | 0,00 |
| `VendaService` | 48/48 (100,00%) | 48/48 (100,00%) | 0,00 | 7/8 (87,50%) | 7/8 (87,50%) | 0,00 |
| `PessoaService` | 35/35 (100,00%) | 35/35 (100,00%) | 0,00 | 11/12 (91,67%) | 11/12 (91,67%) | 0,00 |
| `ApiExceptionHandler` | 39/39 (100,00%) | 39/39 (100,00%) | 0,00 | 2/2 (100,00%) | 2/2 (100,00%) | 0,00 |

## Atividade orientada

A classe `ContaAcesso` ainda não tinha unidade dedicada. O cenário válido cria conta ativa, com Pessoa ativa e perfil OPERADOR. A conta bloqueada, a Pessoa inativa e o conjunto vazio de perfis representam decisões com risco de acesso indevido ou conta inutilizável. O oráculo é a matriz explícita de estados da Aula 14 e a exigência de pelo menos um perfil. Os testes também protegem normalização de login e manutenção de estado após rejeição.

`ContaAcessoTest` passou isoladamente com 21 casos. Depois dele e da unidade do serviço, a suíte de 391 casos passou. Antes do TDD, `ContaAcesso` avançou de 43/44 para 44/44 linhas (97,73% → 100%) e de 22/30 para 30/30 branches (73,33% → 100%). Instruções foram de 185/200 para 200/200 e complexidade de 23/31 para 31/31. Métodos permaneceram em 16/16 e classes em 1/1. `baseline-jacoco` e `orientada-jacoco` preservam a comparação.

O ganho de confiança vem de verificar transições e estado preservado diretamente, com falhas localizadas e sem depender do transporte. O caso de Pessoa inativa demonstra que reativar a conta isoladamente não restaura a autenticação. Os cenários de perfil e hash deixam explícito qual estado deve sobreviver à rejeição. A verificação criptográfica BCrypt e a barreira HTTP continuam exercitadas pela integração.

## Lacunas orientadas ao risco

| Decisão | Impacto e probabilidade | Custo e evidência disponível |
|---|---|---|
| `AdministradorInicialRunner.run` | Alto se a primeira conta deixar de ser criada. Ativação rara, condicionada a provisionamento. | Custo alto em contexto com runner ativado. A suíte integra o serviço, inclusive rollback e idempotência. Newman usa bootstrap real na aplicação empacotada, fora do agente do Surefire. |
| `JwtConfig` e `ClaimsOriginaisJwtDecoder` | Alto em caso de aceitação incorreta. As checagens internas incluem alternativas defensivas após validação de assinatura, presença, classe ou normalização de claim. | Custo médio a alto para evidência isolada com valor efetivamente alcançável. A suíte HTTP preservada cobre 86 casos JWT. A revisão de alcançabilidade deve preceder novos testes para alternativas mascaradas por uma validação anterior. |
| `BloqueioEstoque.adquirir` | Alto se alterações de estoque escaparem do lock. A volta antecipada para entidade já bloqueada protege uma composição menos frequente. | Custo médio com transação real e estado de lock explícito. Os 19 cenários anteriores comprovam disputa, espera e timeout. O ramo auxiliar permanece descoberto no JaCoCo. |
| `Venda.adicionarItem` e `removerItem` | Médio a alto em duplicação comercial. São relevantes comparações por referência, identificador persistido e item sem ID. | Custo baixo para objetos novos e maior para identidade persistida ou proxy real. Testes atuais de domínio e integração exercitam duplicidade, snapshot, remoção e finalização. |
| `AdministracaoContas` e `ContaAcessoService` | Alto para o último administrador ativo. Ausência de autenticação na checagem interna e IDs nulos ou não positivos têm menor alcance através da API. | Custo baixo a médio. Matriz HTTP e chamadas ao proxy comprovam a barreira, testes concorrentes confirmam a invariante no banco. |
| `ItemVenda` e `ApiExceptionHandler` | Alto para preço histórico e privacidade. A linha ausente em ItemVenda é `getVenda`. | Evidência de total e snapshot integra domínio e persistência. Handler tem linhas e branches exercitados. Contratos, conteúdo de respostas e logs sanitizados fornecem oráculos adicionais. |

O piso de 70% de linhas e 40% de branches fica abaixo da medição inicial do Jogos e reproduz a política pedida na aula. Ele é uma proteção contra perda ampla de evidência, com margem para novos ramos legítimos. Um BUNDLE pode ocultar lacunas em classes pequenas. A escolha dos incrementos considerou perda de autenticação, perfil e consistência de estoque, portanto os testes verificam saídas e efeitos específicos. O percentual alto, sozinho, não indica se as asserções detectam um defeito.
