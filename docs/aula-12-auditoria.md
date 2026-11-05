# Aula 12 — Matriz de conformidade

Comparação com [material oficial completo](https://github.com/jeffersonarpasserini/suporteos2026/blob/main/docs/12aula/12aula.md), incluindo quatro pontos de quebra, teoria, testes, transferência, dez questões, seis critérios e checklist. Classificação registra evidência, sem nota automática ou alegação de100%.

Total: **135 linhas**; **133 ATENDIDOS,0 PARCIAIS,0 NÃO ATENDIDOS e 2 NÃO VERIFICADOS**.

Classes principais em src/main/java/br/com/emilly/jogos; testes em src/test/java/br/com/emilly/jogos. Resultados:254 Maven,19 Node e Newman94/239, sem falhas. Casos de segurança têm filtros ativos; identidade simulada existe somente nos testes anteriores de controller. Consulte [guia](aula-12-autenticacao.md), [evidências](aula-12-evidencias.md), [dados consolidados](aula-12-resultados.json) e [inventário](aula-12-arquivos.md).

| ID | Requisito | Evidência concreta | Situação |
|---|---|---|---|
| 1 | Pré-requisitos: main/HEAD Aula11, origin e árvore limpa | Diagnóstico real; baseline em target/aula12-baseline-*; commit56ce736 | ATENDIDO |
| 2 | Executar baseline real antes de implementar | 198 Maven,14 Node, Newman81/135 sem falhas; logs baseline | ATENDIDO |
| 3 | Inspecionar domínio, services, controllers, profiles e migrações | Guia diagnóstico; baseline-hashes; testes anteriores preservados | ATENDIDO |
| 4 | Escopo: HTTP Basic com conta PostgreSQL, sem JWT/OAuth/MFA | SecurityConfig; ContaAcessoUserDetailsService; dependências e guia | ATENDIDO |
| 5 | Separar identificação, autenticação e autorização | aula-12-autenticacao.md fundamentos; dez questões | ATENDIDO |
| 6 | Distinguir função comercial de perfil de acesso | ContaAcesso e PerfilAcesso separados; security/ContaAcessoSecurityTest.autoridadesSaoDeAcessoNaoFuncaoProfissional | ATENDIDO |
| 7 | Ponto de quebra1: dependências compatíveis e compilação | pom.xml; target/aula12-compile.log e etapa-compile; starter-security-test necessário no Boot4 | ATENDIDO |
| 8 | Sem atualizações desnecessárias de Boot/Java/springdoc | POM mantém4.0.8/21/3.0.3; apenas dependências de segurança | ATENDIDO |
| 9 | 006 incremental incluída no master, sem editar001–005 | 006-contas-acesso.yaml; hashes preservados; MigracaoContaAcessoTest | ATENDIDO |
| 10 | Tabelas conta_acesso e conta_acesso_perfil | 006 changesets1/2; testes SQL em schemas limpos e incrementais | ATENDIDO |
| 11 | Conta uma por Pessoa obrigatória | uk_conta_acesso_pessoa; domínio OneToOne; teste pessoaSoRecebeUmaConta | ATENDIDO |
| 12 | Login único LOWER(BTRIM(login)) | uk_conta_acesso_login_lower; falha23505 com nome exato no teste SQL | ATENDIDO |
| 13 | PK composta conta/perfil | pk_conta_acesso_perfil; teste duplicidade real23505 | ATENDIDO |
| 14 | FKs e regras de exclusão | Pessoa RESTRICT, perfis CASCADE; teste23503 e deleção da conta sem Pessoa | ATENDIDO |
| 15 | CHECK status e perfil | ck_conta_acesso_status/perfil; provas23514 específicas | ATENDIDO |
| 16 | Rollback ordenado | MigracaoContaAcessoTest reverte7 em schema próprio e conserva42 anteriores/Pessoas | ATENDIDO |
| 17 | Ponto de quebra2: integridade real no PostgreSQL | MigracaoContaAcessoTest:2 execuções,49 changesets reais, não33 | ATENDIDO |
| 18 | Enums ATIVA/BLOQUEADA e OPERADOR/ADMINISTRADOR | domain/StatusContaAcesso.java e PerfilAcesso.java | ATENDIDO |
| 19 | ContaAcesso separada e composta com Pessoa | domain/ContaAcesso.java; diagrama do guia | ATENDIDO |
| 20 | Estado inicial ATIVA, perfil exclusivamente OPERADOR | Construtor ContaAcesso; teste clienteNaoEscolheAdministradorOuStatus | ATENDIDO |
| 21 | Conta bloqueada e Pessoa inativa não autenticam | ContaAcesso.podeAutenticar; testes contaBloqueadaNaoAutentica/pessoaInativaNaoAutentica | ATENDIDO |
| 22 | Não colocar senha em Pessoa/Cliente/Colaborador | Hashes das entidades anteriores preservados; novo campo somente em ContaAcesso | ATENDIDO |
| 23 | Repository normalizado com relações de autenticação carregadas | ContaAcessoRepository EntityGraph pessoa/perfis; UserDetailsService readOnly | ATENDIDO |
| 24 | DTOs criação/resposta/troca e mapper, sem entidade HTTP | dto/ContaAcessoRequest,ContaAcessoResponse,AlteracaoSenhaRequest; mapper/ContaAcessoMapper | ATENDIDO |
| 25 | Entrada pública não escolhe perfil/status | DTO tem3 campos; teste de injeção ADMINISTRADOR/BLOQUEADA retorna somente OPERADOR/ATIVA | ATENDIDO |
| 26 | Login entre3 e80 após normalização e alfabeto oficial | ContaAcesso.normalizarLogin; Pattern servido; testes domínio e HTTP parametrizados | ATENDIDO |
| 27 | Equivalência Java/BTRIM sem remover tabs/controles/NBSP | indicePostgresContratoEDominioConcordamNoLogin; Node login servido; documentação de locale | ATENDIDO |
| 28 | PasswordEncoderFactories e prefixo bcrypt | SecurityConfig delega à factory; somenteHashBcryptPersisteEVerifica | ATENDIDO |
| 29 | Mínimo12 caracteres e máximo72 bytes UTF-8 | PoliticaSenha e SenhaValidator; testes ASCII/Unicode/emoji/surrogate inválido | ATENDIDO |
| 30 | Hash salgado, senhas iguais com hashes diferentes | security/ContaAcessoSecurityTest.saltProduzHashesDiferentesParaMesmaSenha | ATENDIDO |
| 31 | Verificar senha com matches, não encode+equals | ContaAcessoService.alterarSenha; encoder seguro no Basic | ATENDIDO |
| 32 | Nova senha exige atual correta e diferente | senhaAtualErradaEReutilizacaoNaoAlteramHash; hash preservado após rejeições | ATENDIDO |
| 33 | Troca invalida antiga, nova autentica | alterarSenhaInvalidaAnteriorSemSessao; Newman12-08/09/10 | ATENDIDO |
| 34 | Limite72 também na autenticação, sem truncamento | PasswordEncoder adaptador; limiteUnicode72BytesSemTruncamento | ATENDIDO |
| 35 | Hash inválido não produz500 nem expõe valor | hashCorrompidoFalhaGenericamenteSemExporValor; matches retornafalse na corrupção | ATENDIDO |
| 36 | Ponto de quebra3: prova de hash efetivamente persistido | Consulta JDBC de senha_hash; prefixo, matches e salt nos testes | ATENDIDO |
| 37 | Regras nos services/domínio e transações de escrita | ContaAcessoService.cadastrar/alterarSenha Transactional; controller somente HTTP | ATENDIDO |
| 38 | Duplicidade amigável e proteção final pelo banco | Prechecks do service e unique constraints; HTTP409; teste23505 real | ATENDIDO |
| 39 | UserDetailsService consulta banco e monta autoridades ROLE_* | security/ContaAcessoUserDetailsService.java; testes de autoridades | ATENDIDO |
| 40 | Falha de autenticação genérica | EntryPoint status/message sem login/causa; senhaErradaELoginInexistenteSaoGenericos | ATENDIDO |
| 41 | SecurityFilterChain HTTP Basic | SecurityConfig; teste Basic real35 cenários | ATENDIDO |
| 42 | STATELESS, sem form login/request cache/logout de sessão | SecurityConfig; senha antiga e chamada sem credencial após sucesso retornam401 | ATENDIDO |
| 43 | CSRF compatível com clientes de API e limitação de navegador declarada | aula-12-autenticacao.md fluxo: stateless não elimina CSRF; reavaliar com credenciais automáticas | ATENDIDO |
| 44 | Públicos explícitos por método e caminho | GET Health/docs/UI; POST Pessoa/Conta; demais authenticated | ATENDIDO |
| 45 | Sem brecha anônima nas rotas protegidas | todasAsRotasProtegidasExigemIdentidade percorre38 operações protegidas com filtros ativos | ATENDIDO |
| 46 | Cadastro público provisório, não produção | aula-12-autenticacao.md bootstrap, transferência e ameaças | ATENDIDO |
| 47 | Sem privilégios diferentes nesta aula | papelAdministradorAindaNaoMudaAutorizacao; não há hasRole diferenciando rotas | ATENDIDO |
| 48 | POST conta201 e Location | Controller cadastrar; teste cadastro e Newman12-02 | ATENDIDO |
| 49 | GET /me200 autenticado, sem hash | Controller propria; teste basicCorretoConsultaContaSemExporHash | ATENDIDO |
| 50 | PUT /me/senha204 sem corpo | Controller alterar; teste de troca e Newman12-08 | ATENDIDO |
| 51 | Validação400, referências404, duplicidades409 | testes senha/login inválidos, referenciaInexistenteRetorna404 e duplicidades | ATENDIDO |
| 52 | Testes obrigatórios: Health público | security/ContaAcessoSecurityTest.healthSwaggerEOpenApiPublicos | ATENDIDO |
| 53 | Testes obrigatórios: Swagger/OpenAPI públicos | security/ContaAcessoSecurityTest.healthSwaggerEOpenApiPublicos | ATENDIDO |
| 54 | Testes obrigatórios: Pessoa inicial sem Basic | security/ContaAcessoSecurityTest.bootstrapPessoaEContaSemAutenticacao | ATENDIDO |
| 55 | Testes obrigatórios: Conta inicial sem Basic | security/ContaAcessoSecurityTest.bootstrapPessoaEContaSemAutenticacao | ATENDIDO |
| 56 | Testes obrigatórios: Rota protegida anônima401 | security/ContaAcessoSecurityTest.todasAsRotasProtegidasExigemIdentidade | ATENDIDO |
| 57 | Testes obrigatórios: Basic correto autentica | security/ContaAcessoSecurityTest.basicCorretoConsultaContaSemExporHash | ATENDIDO |
| 58 | Testes obrigatórios: Senha errada401 | security/ContaAcessoSecurityTest.senhaErradaELoginInexistenteSaoGenericos | ATENDIDO |
| 59 | Testes obrigatórios: Login inexistente401 | security/ContaAcessoSecurityTest.senhaErradaELoginInexistenteSaoGenericos | ATENDIDO |
| 60 | Testes obrigatórios: Conta BLOQUEADA401 | security/ContaAcessoSecurityTest.contaBloqueadaNaoAutentica | ATENDIDO |
| 61 | Testes obrigatórios: Pessoa INATIVO401 | security/ContaAcessoSecurityTest.pessoaInativaNaoAutentica | ATENDIDO |
| 62 | Testes obrigatórios: Cadastro somente OPERADOR | security/ContaAcessoSecurityTest.clienteNaoEscolheAdministradorOuStatus | ATENDIDO |
| 63 | Testes obrigatórios: Escolha pública ADMINISTRADOR impossível | security/ContaAcessoSecurityTest.clienteNaoEscolheAdministradorOuStatus | ATENDIDO |
| 64 | Testes obrigatórios: Senha aberta não persistida | security/ContaAcessoSecurityTest.somenteHashBcryptPersisteEVerifica | ATENDIDO |
| 65 | Testes obrigatórios: Prefixo bcrypt | security/ContaAcessoSecurityTest.somenteHashBcryptPersisteEVerifica | ATENDIDO |
| 66 | Testes obrigatórios: matches verifica credencial | basicBloqueiaEquivalentesNulDeContaLegadaSemAlterarHash; unicodeSemNulContinuaValidoEDiferentesNaoAutenticam: correta200/diferente401 | ATENDIDO |
| 67 | Testes obrigatórios: Salt diferente para mesma senha | security/ContaAcessoSecurityTest.saltProduzHashesDiferentesParaMesmaSenha | ATENDIDO |
| 68 | Testes obrigatórios: Troca invalida senha anterior | security/ContaAcessoSecurityTest.alterarSenhaInvalidaAnteriorSemSessao | ATENDIDO |
| 69 | Testes obrigatórios: Login duplicado rejeitado | security/ContaAcessoSecurityTest.loginDuplicadoIgnoraCaixaEEspacosU0020 | ATENDIDO |
| 70 | Testes obrigatórios: Teto72 bytes UTF-8 | security/ContaAcessoSecurityTest.limiteUnicode72BytesSemTruncamento | ATENDIDO |
| 71 | Testes obrigatórios: Nenhuma resposta expõe senha/hash | security/ContaAcessoSecurityTest.toStringsESerializacaoNaoExpoemSegredos; Newman teste global de respostas | ATENDIDO |
| 72 | Filtros ativos na prova de segurança, sem WithMockUser | ContaAcessoSecurityTest; OpenApiAutenticacaoTest; nenhuma desativação global de filtros | ATENDIDO |
| 73 | Testes antigos preservados com foco de controller | WithMockUser em10 classes antigas; filtros continuam ativos; sem remoção de casos | ATENDIDO |
| 74 | CRUD, estoque, vendas, filtros e paginação anteriores | 254 Maven e Newman94; services de Jogo/Venda idênticos à baseline | ATENDIDO |
| 75 | Rollback real e preço histórico anteriores | VendaServiceTest: rollback REQUIRES_NEW + clear e snapshot persistido | ATENDIDO |
| 76 | Concorrência pessimista preservada | 19 execuções ConcorrenciaEstoqueTest; nenhuma alteração na política de locking | ATENDIDO |
| 77 | Postman Basic com authLogin/authPassword | Jogos-Aula12.postman_collection.json; credenciais vazias no arquivo, geradas em runtime | ATENDIDO |
| 78 | Bootstrap repetível, IDs dinâmicos e sem credencial real | Pasta12-00/03; UUID+CPF válido; coleção anterior preservada | ATENDIDO |
| 79 | Newman real e casos401 explícitos | 94 requisições,239 verificações; senhas errada/antiga, login ausente, anônimo e Pessoa inativa | ATENDIDO |
| 80 | Relatório sem Authorization/senha utilizáveis | aula12-newman.cjs sanitiza antes de gravar; teste Node examina headers e corpos reais | ATENDIDO |
| 81 | OpenAPI basicAuth e rotas públicas sem requisito | OpenApiSegurancaConfig; somenteOperacoesPublicasNaoExigemBasic | ATENDIDO |
| 82 | Schemas novos, DTOs, erros e códigos HTTP documentados | OpenApiAutenticacaoTest e OpenApiDocumentationTest; exemplos400 confrontados com handler | ATENDIDO |
| 83 | Contrato Aula12 próprio e históricos preservados | docs/aula-12-openapi.yaml real; JSON/YAML/entrega comparados em3 testes novos | ATENDIDO |
| 84 | Swagger UI autentica e executa /me | Captura aula-12-swagger-autenticado.png; UI real200 e41 operações; AuthActions sem digitação manual | ATENDIDO |
| 85 | Profiles docs dev/test habilitados, prod bloqueado | OpenApiProfilesTest:3 execuções sem DataSource nem produção | ATENDIDO |
| 86 | Privacidade de responses e serialização | Mapper sem segredo; JsonIgnore; toStrings e teste Basic; schemas writeOnly | ATENDIDO |
| 87 | Privacidade de logs de persistência/autenticação | Onze provas de logs; SQLState/restrição preservados; nenhuma senha/hash/login na captura de falha | ATENDIDO |
| 88 | Sem conta administrativa fixa ou senha real no código | Credenciais dos testes/coleção são fictícias e geradas; role ADMIN somente teste controlado | ATENDIDO |
| 89 | Basic somente com TLS fora do laboratório | Guia fundamentos, Base64, HTTPS e riscos; sem promessa de segurança em HTTP | ATENDIDO |
| 90 | Ponto de quebra4: filtros, estados, OpenAPI e Postman | 39 provas com filtros reais;16 HTTP NUL; Basic inválido401, cadastro/troca400 sem persistir;254 testes finais | ATENDIDO |
| 91 | Documentos exigidos presentes | autenticacao/evidencias/auditoria/arquivos/openapi/questoes em docs; inventário conferido | ATENDIDO |
| 92 | §13.1: identificar entidade civil | Pessoa, guia transferência1 | ATENDIDO |
| 93 | §13.2: conta separada de papéis | ContaAcesso, guia transferência2 e diagrama | ATENDIDO |
| 94 | §13.3: dois perfis coerentes com Jogos | OPERADOR/ADMINISTRADOR, guia transferência3 | ATENDIDO |
| 95 | §13.4: persistência exclusiva de hash | JDBC e testes de hash, guia transferência4 | ATENDIDO |
| 96 | §13.5: bootstrap provisório | Guia transferência5 e ameaças | ATENDIDO |
| 97 | §13.6: proteger rota real do tema | GET/api/jogos; filtro e Newman | ATENDIDO |
| 98 | §13.7: anônimo/válido/bloqueado | ContaAcessoSecurityTest casos401/200/401 | ATENDIDO |
| 99 | §13.8: diagrama atualizado | Guia Mermaid Pessoa/Conta/Cliente/Colaborador/Venda/Jogo | ATENDIDO |
| 100 | §13.9: três ameaças pendentes | Guia transferência9: brute force, bootstrap, falta deTLS; CSRF adicional | ATENDIDO |
| 101 | §14: questão para discussão1 | aula-12-questoes.md, resposta1 na ordem do material oficial | ATENDIDO |
| 102 | §14: questão para discussão2 | aula-12-questoes.md, resposta2 na ordem do material oficial | ATENDIDO |
| 103 | §14: questão para discussão3 | aula-12-questoes.md, resposta3 na ordem do material oficial | ATENDIDO |
| 104 | §14: questão para discussão4 | aula-12-questoes.md, resposta4 na ordem do material oficial | ATENDIDO |
| 105 | §14: questão para discussão5 | aula-12-questoes.md, resposta5 na ordem do material oficial | ATENDIDO |
| 106 | §14: questão para discussão6 | aula-12-questoes.md, resposta6 na ordem do material oficial | ATENDIDO |
| 107 | §14: questão para discussão7 | aula-12-questoes.md, resposta7 na ordem do material oficial | ATENDIDO |
| 108 | §14: questão para discussão8 | aula-12-questoes.md, resposta8 na ordem do material oficial | ATENDIDO |
| 109 | §14: questão para discussão9 | aula-12-questoes.md, resposta9 na ordem do material oficial | ATENDIDO |
| 110 | §14: questão para discussão10 | aula-12-questoes.md, resposta10 na ordem do material oficial | ATENDIDO |
| 111 | §15 critério1: separação pessoa/papel/conta (20%) | Entidades, diagrama e teste autoridades; não atribui nota automática | ATENDIDO |
| 112 | §15 critério2: Liquibase/integridade (15%) | Migração006; dois testes limpo/incremental/rollback e constraints específicas | ATENDIDO |
| 113 | §15 critério3: hash/não exposição (20%) | Política rejeita NUL antes de encode/matches;11 provas de privacidade; consulta do hash em REQUIRES_NEW após rejeições | ATENDIDO |
| 114 | §15 critério4: integração Spring Security (20%) | ContaAcessoUserDetailsService/PostgreSQL; política compartilhada em cadastro/troca/Basic; fixture vulnerável legada não autentica | ATENDIDO |
| 115 | §15 critério5: testes de segurança (15%) | Seis execuções novas NUL:254 Maven/19 Node; padrões periódicos e corpus Unicode; Newman94/239 | ATENDIDO |
| 116 | §15 critério6: limitações e ameaças (10%) | Guia TLS/CSRF/bootstrap e transferência9 | ATENDIDO |
| 117 | Erro crítico: não há senha aberta | Hash em banco e sem segredo em response; testes reais | ATENDIDO |
| 118 | Erro crítico: rota protegida não aceita anônimo | Todas38 operações protegidas testadas401 | ATENDIDO |
| 119 | Erro crítico: cliente não escolhe ADMINISTRADOR | DTO3 campos e prova de tentativa rejeitada semanticamente: somente OPERADOR | ATENDIDO |
| 120 | Erro crítico: sem credencial real entre candidatos | Inventário e inspeção de fontes, Postman, contratos, captura e relatórios sanitizados | ATENDIDO |
| 121 | Erro crítico: constraints não removidas para passar testes | 001–005 preservados;006 integra constraints e prova SQLstate/nomes | ATENDIDO |
| 122 | Checklist: distinguir autenticação e autorização | Guia + questão7 | ATENDIDO |
| 123 | Checklist: não confundir função e perfil | Guia + questão1 + autoridades | ATENDIDO |
| 124 | Checklist: banco somente hash | Teste JDBC + CHECK formato | ATENDIDO |
| 125 | Checklist: salt e custo | Guia + questão3 + teste salt | ATENDIDO |
| 126 | Checklist: Basic exige TLS | Guia + questão2 | ATENDIDO |
| 127 | Checklist: públicos explícitos | SecurityConfig matchers | ATENDIDO |
| 128 | Checklist: anônimo401 | todasAsRotasProtegidasExigemIdentidade | ATENDIDO |
| 129 | Checklist: conta bloqueada não autentica | contaBloqueadaNaoAutentica | ATENDIDO |
| 130 | Checklist: OpenAPI/Postman com segurança | Contrato real e94 requisições Newman | ATENDIDO |
| 131 | Checklist: suíte aprovada, não forçar65 do professor | 254 executados,0/0/0; relatório final-validado | ATENDIDO |
| 132 | Checklist: bootstrap é provisório | Guia riscos e transferência5 | ATENDIDO |
| 133 | Encerramento: apenas local, sem operações de escrita Git | HEAD56ce736 preservado, staging vazio; comandos somente de leitura | ATENDIDO |
| 134 | Discussão/apresentação humana das respostas e conceitos | Respostas preparadas; participação da estudante não é comprovada por testes | NÃO VERIFICADO |
| 135 | §8: resposta401 renderizada visualmente no Swagger após logout | UI200 comprovada; autorização retirada e HTTP independente401; renderização do401 não comprovada | NÃO VERIFICADO |

Limites: cadastro público didático, HTTP local semTLS, sem diferenciação de permissões, rate limiting, MFA ou auditoria. CSRF de navegador exige reavaliação. Validador Node cobre subconjunto, não certifica integralmente OpenAPI. Captura somente da resposta200, sem inventar401 visual. Nenhuma publicação realizada.

## Reavaliação da correção NUL

Os itens66,90,113,114 e115 foram classificados PARCIAL na auditoria independente em conversa. Após a correção, voltam a ATENDIDO com as evidências abaixo. A contagem é133 ATENDIDOS,0 PARCIAIS,0 NÃO ATENDIDOS e2 NÃO VERIFICADOS, sem mudar os135 itens nem encerrar as pendências134/135.

- Item66: basicBloqueiaEquivalentesNulDeContaLegadaSemAlterarHash; unicodeSemNulContinuaValidoEDiferentesNaoAutenticam: correta200/diferente401.
- Item90: 39 provas com filtros reais;16 HTTP NUL; Basic inválido401, cadastro/troca400 sem persistir;254 testes finais.
- Item113: Política rejeita NUL antes de encode/matches;11 provas de privacidade; consulta do hash em REQUIRES_NEW após rejeições.
- Item114: ContaAcessoUserDetailsService/PostgreSQL; política compartilhada em cadastro/troca/Basic; fixture vulnerável legada não autentica.
- Item115: Seis execuções novas NUL:254 Maven/19 Node; padrões periódicos e corpus Unicode; Newman94/239.

Detalhes e resultados reais: [correção NUL](aula-12-correcao-nul.md). Nenhuma alteração nas migrações001–006 ou nos contratos históricos.
