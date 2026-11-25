# Aula 13 — Auditoria de conformidade

Fonte: [material oficial completo](https://github.com/jeffersonarpasserini/suporteos2026/blob/main/docs/13aula/13aula.md), incluindo quatro pontos de quebra, transferência, questões, seis critérios, erros críticos e checklist. Requisitos técnicos adaptados a Jogos; números de testes do SuporteOS não são metas deste projeto. A contagem resulta desta decomposição, não de uma quantidade imposta pelo professor.

ATENDIDO indica evidência técnica/material entregue, não nota atribuída nem prova de domínio oral. Evidências de execução estão em aula-13-evidencias.md e aula-13-resultados.json.

Total: 131. 129 ATENDIDO; 0 PARCIAL; 1 NÃO ATENDIDO; 1 NÃO VERIFICADO.

| Item | Seção/requisito adaptado | Evidência concreta | Situação |
|---|---|---|---|
| 1 | Pré-requisitos e diagnóstico: Aula12 identificada, branch e origem corretas | main; HEAD400063010adbd7a8b87d2c93f15a2abe5bd85811; origin Siunne/Jogos; comandos de leitura | ATENDIDO |
| 2 | Pré-requisitos e diagnóstico: Árvore inicialmente limpa e preservação do estado | git status inicial vazio; snapshot target/aula13-baseline-hashes.json | ATENDIDO |
| 3 | Pré-requisitos e diagnóstico: Baseline executada antes da alteração | Maven254; Node19; Newman94/239; aula-13-evidencias.md | ATENDIDO |
| 4 | Pré-requisitos e diagnóstico: Adaptação para Jogo/Pessoa/ContaAcesso | Services e domínio reais inspecionados; nenhuma classe Produto/SuporteOS criada | ATENDIDO |
| 5 | Pré-requisitos e diagnóstico: Execuções de persistência somente no banco isolado | profile test; asserts current_database=jogos_test; nenhuma conexão dev/prod | ATENDIDO |
| 6 | Pré-requisitos e diagnóstico: Números do projeto de referência não usados como meta | 254 anteriores preservados; contagem atual real em aula-13-resultados.json | ATENDIDO |
| 7 | Escopo e teoria (seções1–3): Três segmentos do JWT e significado | aula-13-jwt.md; JwtSecurityTest.loginEmiteClaimsExatosSemDadosPessoais | ATENDIDO |
| 8 | Escopo e teoria (seções1–3): Base64URL separado de criptografia e assinatura | guia e questões1/2/10; testes de payload adulterado | ATENDIDO |
| 9 | Escopo e teoria (seções1–3): HS256 simétrico e implicação de compartilhar chave | guia e questões3/9; JwtConfig fixa HS256 | ATENDIDO |
| 10 | Escopo e teoria (seções1–3): Comparação com RS256 sem implementar RSA/JWK | guia; inspeção das dependências e JwtConfig | ATENDIDO |
| 11 | Escopo e teoria (seções1–3): Modelo stateless sem sessão HTTP | SecurityConfig STATELESS, requestCache/formLogin/logout desabilitados | ATENDIDO |
| 12 | Escopo e teoria (seções1–3): Sem distinção de autorização por perfil nesta aula | SecurityConfig anyRequest.authenticated; testes de perfis e endpoints | ATENDIDO |
| 13 | Escopo e teoria (seções1–3): Sem refresh, revogação, JWT persistido ou Authorization Server completo | inspeção de rotas/migrações; guia de limites | ATENDIDO |
| 14 | Escopo e teoria (seções1–3): Regras e concorrência comerciais preservadas | 21 arquivos críticos com SHA-256 igual; ConcorrenciaEstoqueTest19; VendaServiceTest | ATENDIDO |
| 15 | Ponto de quebra1 — dependência: Starter oficial Resource Server compatível | pom.xml Boot4.0.8; compile e suíte completa | ATENDIDO |
| 16 | Ponto de quebra1 — dependência: JOSE pelo starter, sem filtro JWT próprio | NimbusJwtEncoder/Decoder oficiais em JwtConfig; inspeção de fontes | ATENDIDO |
| 17 | Ponto de quebra1 — dependência: Compilação antes de integrar o fluxo externo | target/aula13-compile.log BUILD SUCCESS | ATENDIDO |
| 18 | Ponto de quebra2 — chave e configuração: Chave própria aleatória de pelo menos32 bytes | RNG criptográfico em .env local; inspeção sem divulgar valor | ATENDIDO |
| 19 | Ponto de quebra2 — chave e configuração: Chave Base64 externa, sem segredo real no código | app.security.jwt.secret=${JWT_SECRET}; varredura dos candidatos | ATENDIDO |
| 20 | Ponto de quebra2 — chave e configuração: .env carregado e ignorado, exemplo inválido | dev/test import; git check-ignore; .env.example placeholder | ATENDIDO |
| 21 | Ponto de quebra2 — chave e configuração: Fixture conhecida somente no profile test | application-test.properties; chave local distinta; sem fallback no main | ATENDIDO |
| 22 | Ponto de quebra2 — chave e configuração: Base64 inválida e chave curta falham com segurança | JwtConfigurationTest: quatro casos; falha de inicialização pelo bean jwtSecretKey | ATENDIDO |
| 23 | Ponto de quebra2 — chave e configuração: Segredo ausente impede contexto, sem valor no erro | JwtConfigurationTest; mensagens fixas; JwtProperties.toString redigido | ATENDIDO |
| 24 | Ponto de quebra2 — chave e configuração: Propriedades tipadas com emissor Jogos e PT30M | JwtProperties; application.properties; teste de configuração | ATENDIDO |
| 25 | Ponto de quebra2 — chave e configuração: SecretKey, JwtEncoder e JwtDecoder oficiais | JwtConfig; emissão/validação HTTP real | ATENDIDO |
| 26 | Ponto de quebra2 — chave e configuração: Encoder e decoder aceitam somente HS256 | algoritmoHS384ERejeitadoMesmoComMesmaChave; algoritmoNoneNaoEAutenticacao | ATENDIDO |
| 27 | Ponto de quebra2 — chave e configuração: Assinatura e emissor validados | assinaturaComOutraChaveERejeitada; emissorIncorretoERejeitadoMesmoComAssinaturaValida | ATENDIDO |
| 28 | Ponto de quebra2 — chave e configuração: Clock injetado e prazo exato | TokenService; expiraExatamenteEm1800SegundosSemRenovacaoPorUso | ATENDIDO |
| 29 | Emissão e ponto de quebra3: AuthenticationManager reutilizado sem duplicar BCrypt | AutenticacaoService; ContaAcessoUserDetailsService; PasswordEncoder existente | ATENDIDO |
| 30 | Emissão e ponto de quebra3: Senha incorreta e login inexistente com erro genérico | credenciaisErradasTemMesmoErroGenerico; ApiExceptionHandler.tratarCredenciais | ATENDIDO |
| 31 | Emissão e ponto de quebra3: Conta bloqueada não obtém novo JWT | bloqueioImpedeNovoLoginMasNaoRevogaJWT | ATENDIDO |
| 32 | Emissão e ponto de quebra3: Pessoa inativa não obtém novo JWT | pessoaInativaImpedeNovoLoginMasNaoRevogaJWT | ATENDIDO |
| 33 | Emissão e ponto de quebra3: Política de senha da Aula12 preservada | PoliticaSenha byte a byte; ContaAcessoSecurityTest: mínimo12,72 bytes,Unicode,NUL | ATENDIDO |
| 34 | Emissão e ponto de quebra3: U+0000 não autentica; hash permanece igual | nulEBcryptEquivalenteNaoAutenticam; REQUIRES_NEW/JDBC | ATENDIDO |
| 35 | Emissão e ponto de quebra3: Endpoint POST /api/autenticacao/login público | AutenticacaoController; testes reais e Newman | ATENDIDO |
| 36 | Emissão e ponto de quebra3: Entrada com login e senha, sem perfis administrativos | LoginRequest; perfisDeclaradosPeloClienteSaoIgnorados | ATENDIDO |
| 37 | Emissão e ponto de quebra3: Campos/JSON inválidos400 sem senha no erro | camposAusentesRetornam400SemValoresRejeitados; handler existente | ATENDIDO |
| 38 | Emissão e ponto de quebra3: Login inválido401 ApiError | credenciaisErradasTemMesmoErroGenerico; exemplos OpenAPI verificados | ATENDIDO |
| 39 | Emissão e ponto de quebra3: Resposta accessToken, tokenType, expiresIn, expiresAt | TokenResponse; teste dos quatro campos e validade | ATENDIDO |
| 40 | Emissão e ponto de quebra3: iss próprio e sub com login autenticado | loginEmiteClaimsExatosSemDadosPessoais | ATENDIDO |
| 41 | Emissão e ponto de quebra3: iat e exp separados por1800 segundos | TokenService; teste de claims e limite temporal | ATENDIDO |
| 42 | Emissão e ponto de quebra3: jti exclusivo em dois logins | loginEmiteClaimsExatosSemDadosPessoais compara UUIDs | ATENDIDO |
| 43 | Emissão e ponto de quebra3: Perfis obtidos exclusivamente de Authentication | TokenService; teste contra perfis enviados no JSON | ATENDIDO |
| 44 | Emissão e ponto de quebra3: Senha, hash e dados fiscais ausentes do payload | teste exige conjunto exato de seis claims | ATENDIDO |
| 45 | Emissão e ponto de quebra3: Token e credenciais fora dos logs/toString | tokenSenhaChaveEHashNaoAparecemEmLogsOuErros; filtro de privacidade | ATENDIDO |
| 46 | Emissão e ponto de quebra3: HTTP Basic desabilitado na rota protegida | SecurityConfig; basicEAnonimoNaoLiberamRota; Newman13-03 | ATENDIDO |
| 47 | Emissão e ponto de quebra3: Bearer válido identifica conta pelo sub | JwtAuthenticationToken/Principal; /me200 real; Swagger | ATENDIDO |
| 48 | Emissão e ponto de quebra3: Sem token401 | JwtSecurityTest; ContaAcessoSecurityTest percorre todas as rotas protegidas | ATENDIDO |
| 49 | Emissão e ponto de quebra3: Assinatura alterada401 | assinaturaAlteradaERejeitada; Newman13-04 | ATENDIDO |
| 50 | Emissão e ponto de quebra3: Payload alterado401 | payloadAlteradoERejeitado | ATENDIDO |
| 51 | Emissão e ponto de quebra3: Outra chave401 | assinaturaComOutraChaveERejeitada | ATENDIDO |
| 52 | Emissão e ponto de quebra3: Token expirado401 e algoritmo inesperado401 | testes de expiração/HS384/none | ATENDIDO |
| 53 | Emissão e ponto de quebra3: Token malformado401 sem erro500 | formatosInvalidosNaoProduzem500: quatro casos | ATENDIDO |
| 54 | Emissão e ponto de quebra3: iat/nbf futuros e claims ausentes rejeitados | ClaimsOriginaisJwtDecoder: tipo/faixa/instantes originais; JwtSecurityTest:36 casos temporais, pares ±2^61, sub tipado, futuro e frações; Maven346 sem falhas | ATENDIDO |
| 55 | Emissão e ponto de quebra3: Somente endpoints públicos previstos | SecurityConfig matchers por método; OpenApiAutenticacaoTest; teste anônimo de todas as demais rotas | ATENDIDO |
| 56 | Emissão e ponto de quebra3: Stateless e CSRF coerentes com header, sem cookie de autenticação | SecurityConfig; justificativa no guia; sem promessa para futuro cliente com cookies | ATENDIDO |
| 57 | Emissão e ponto de quebra3: Sem verificação de conta em cada filtro JWT | decoder local; token antigo continua200 após bloqueio/inativação | ATENDIDO |
| 58 | Ponto de quebra4 — fluxo externo: OpenAPI troca basicAuth por bearerAuth/JWT | OpenApiSegurancaConfig; OpenApiAutenticacaoTest; Node | ATENDIDO |
| 59 | Ponto de quebra4 — fluxo externo: Login público sem requisito de segurança OpenAPI | OpenApiAutenticacaoTest; Node; testes com filtros ativos | ATENDIDO |
| 60 | Ponto de quebra4 — fluxo externo: Rotas protegidas documentam Bearer e401 | testes OpenAPI percorrem todas as operações | ATENDIDO |
| 61 | Ponto de quebra4 — fluxo externo: JSON e YAML reais exportados, históricos preservados | docs/aula-13-openapi.yaml; testes de equivalência; hashes09–12 | ATENDIDO |
| 62 | Ponto de quebra4 — fluxo externo: Mappings reais correspondem ao contrato | OpenApiDocumentationTest compara conjuntos:42 operações/28 caminhos | ATENDIDO |
| 63 | Ponto de quebra4 — fluxo externo: Swagger login/Authorize/consulta protegida | aula13-swagger-ui.cjs: login200 e /me200; captura sem token inspecionada | ATENDIDO |
| 64 | Ponto de quebra4 — fluxo externo: Postman captura accessToken e usa variável Bearer | Jogos-Aula13.postman_collection.json; Newman13-01 | ATENDIDO |
| 65 | Ponto de quebra4 — fluxo externo: Coleção executa operações protegidas | Newman98 requisições/250 assertions;42 operações distintas | ATENDIDO |
| 66 | Ponto de quebra4 — fluxo externo: Credenciais inválidas, Basic, ausência e token alterado no cliente externo | Newman12-04/05/06 e13-03/04 | ATENDIDO |
| 67 | Ponto de quebra4 — fluxo externo: Relatório Newman sem senha/header/token utilizável | aula13-sanitizar.cjs; testes com Buffer real e bytes de resposta | ATENDIDO |
| 68 | Ponto de quebra4 — fluxo externo: Variáveis versionadas vazias; repetir login após expiração | Node verifica coleção; instruções do guia | ATENDIDO |
| 69 | Ponto de quebra4 — fluxo externo: Validador próprio com alcance declarado | aula13-conferir-newman.cjs: subconjunto JSON Schema; criptografia em testes separados | ATENDIDO |
| 70 | Ponto de quebra4 — fluxo externo: Suíte completa do projeto preservada, sem meta67 artificial | aula-13-resultados.json e relatórios Surefire | ATENDIDO |
| 71 | Ponto de quebra4 — fluxo externo: Limitações de expiração e revogação explicadas | guia; testes de bloqueio e troca de senha sem revogação | ATENDIDO |
| 72 | Expiração e armazenamento (seções14–16): Validade absoluta não aumenta com atividade | teste aos1799/1800 segundos, mesmo exp | ATENDIDO |
| 73 | Expiração e armazenamento (seções14–16): Diferenciação de idle timeout e estado compartilhado | guia; atividade de transferência | ATENDIDO |
| 74 | Expiração e armazenamento (seções14–16): Diferenciação de access/refresh e rotação | guia: explicação, sem implementação fora do escopo | ATENDIDO |
| 75 | Expiração e armazenamento (seções14–16): Bloqueio impede novo login, sem revogar o emitido | teste real com conta persistida e mesmo JWT | ATENDIDO |
| 76 | Expiração e armazenamento (seções14–16): Troca de senha impede credencial anterior, sem revogar JWT | trocarSenhaNaoRevogaTokenEmitido; testes de senha anteriores | ATENDIDO |
| 77 | Expiração e armazenamento (seções14–16): Logout não promete invalidação inexistente | nenhum endpoint logout; guia explica exclusão apenas no cliente | ATENDIDO |
| 78 | Expiração e armazenamento (seções14–16): Estratégias futuras de revogação | transferência item9: jti/cache, versão, provedor central | ATENDIDO |
| 79 | Expiração e armazenamento (seções14–16): Armazenamento do cliente e risco XSS/CSRF | guia: memória, Web Storage, cookie HttpOnly e dispositivo móvel | ATENDIDO |
| 80 | Expiração e armazenamento (seções14–16): Diagnóstico de falhas sem revelar segredo | guia; testes chave inválida, algoritmo, emissor, tempo e assinatura | ATENDIDO |
| 81 | Expiração e armazenamento (seções14–16): HTTPS e bootstrap público didático identificados como limitações | guia e descrições de operações; sem alegação de segurança produtiva pronta | ATENDIDO |
| 82 | Atividade de transferência: Item1 aplicado ao domínio Jogos | aula-13-transferencia.md, linha do item1 e evidência específica nela indicada | ATENDIDO |
| 83 | Atividade de transferência: Item2 aplicado ao domínio Jogos | aula-13-transferencia.md, linha do item2 e evidência específica nela indicada | ATENDIDO |
| 84 | Atividade de transferência: Item3 aplicado ao domínio Jogos | aula-13-transferencia.md, linha do item3 e evidência específica nela indicada | ATENDIDO |
| 85 | Atividade de transferência: Item4 aplicado ao domínio Jogos | aula-13-transferencia.md, linha do item4 e evidência específica nela indicada | ATENDIDO |
| 86 | Atividade de transferência: Item5 aplicado ao domínio Jogos | aula-13-transferencia.md, linha do item5 e evidência específica nela indicada | ATENDIDO |
| 87 | Atividade de transferência: Item6 aplicado ao domínio Jogos | aula-13-transferencia.md, linha do item6 e evidência específica nela indicada | ATENDIDO |
| 88 | Atividade de transferência: Item7 aplicado ao domínio Jogos | aula-13-transferencia.md, linha do item7 e evidência específica nela indicada | ATENDIDO |
| 89 | Atividade de transferência: Item8 aplicado ao domínio Jogos | aula-13-transferencia.md, linha do item8 e evidência específica nela indicada | ATENDIDO |
| 90 | Atividade de transferência: Item9 aplicado ao domínio Jogos | aula-13-transferencia.md, linha do item9 e evidência específica nela indicada | ATENDIDO |
| 91 | Questões de discussão: Questão1 respondida na ordem do material | aula-13-questoes.md, resposta1; material escrito, não prova de participação | ATENDIDO |
| 92 | Questões de discussão: Questão2 respondida na ordem do material | aula-13-questoes.md, resposta2; material escrito, não prova de participação | ATENDIDO |
| 93 | Questões de discussão: Questão3 respondida na ordem do material | aula-13-questoes.md, resposta3; material escrito, não prova de participação | ATENDIDO |
| 94 | Questões de discussão: Questão4 respondida na ordem do material | aula-13-questoes.md, resposta4; material escrito, não prova de participação | ATENDIDO |
| 95 | Questões de discussão: Questão5 respondida na ordem do material | aula-13-questoes.md, resposta5; material escrito, não prova de participação | ATENDIDO |
| 96 | Questões de discussão: Questão6 respondida na ordem do material | aula-13-questoes.md, resposta6; material escrito, não prova de participação | ATENDIDO |
| 97 | Questões de discussão: Questão7 respondida na ordem do material | aula-13-questoes.md, resposta7; material escrito, não prova de participação | ATENDIDO |
| 98 | Questões de discussão: Questão8 respondida na ordem do material | aula-13-questoes.md, resposta8; material escrito, não prova de participação | ATENDIDO |
| 99 | Questões de discussão: Questão9 respondida na ordem do material | aula-13-questoes.md, resposta9; material escrito, não prova de participação | ATENDIDO |
| 100 | Questões de discussão: Questão10 respondida na ordem do material | aula-13-questoes.md, resposta10; material escrito, não prova de participação | ATENDIDO |
| 101 | Seis critérios de avaliação: Fundamentação JWT/assinatura (20%) | guia, diagrama, questões; testes de adulteração e HS256 | ATENDIDO |
| 102 | Seis critérios de avaliação: Gestão da chave (20%) | .env ignorado; exemplo inválido; configuração fail-closed e inspeção de candidatos | ATENDIDO |
| 103 | Seis critérios de avaliação: Emissão/claims (20%) | TokenService; testes reais de seis claims, UUID e1800 segundos | ATENDIDO |
| 104 | Seis critérios de avaliação: Resource Server/Bearer (20%) | Resource Server e Nimbus HS256 preservados; preflight dos claims originais sem DB; Maven346, Newman98/250 e51 requisições adversariais sem falhas | ATENDIDO |
| 105 | Seis critérios de avaliação: Testes/Postman (15%) | suíte Maven, Node, Newman reais e comparação do contrato | ATENDIDO |
| 106 | Seis critérios de avaliação: Análise de limitações (5%) | guia e transferência: expiração, revogação, armazenamento, TLS | ATENDIDO |
| 107 | Erros críticos e checklist: Nenhuma chave real versionada/candidata | inspeção de conteúdo; .env ignorado; fixture conhecida somente em src/test | ATENDIDO |
| 108 | Erros críticos e checklist: Nenhuma senha no JWT | conjunto exato de claims e testes de privacidade | ATENDIDO |
| 109 | Erros críticos e checklist: Token sempre tem expiração | exp obrigatório e duração1800; teste sem exp rejeitado | ATENDIDO |
| 110 | Erros críticos e checklist: Algoritmo controlado pelo servidor | HS256 fixo; HS384/none rejeitados | ATENDIDO |
| 111 | Erros críticos e checklist: Perfis não confiados ao request | teste de tentativa ADMINISTRADOR no login e cadastro anterior | ATENDIDO |
| 112 | Erros críticos e checklist: Base64 não apresentada como criptografia | guia e respostas1/10 | ATENDIDO |
| 113 | Erros críticos e checklist: Sem promessa de logout/revogação | guia; SecurityConfig logout desabilitado | ATENDIDO |
| 114 | Checklist final (material/evidência técnica): Separação das três partes do JWT | guia; teste de três segmentos e claims | ATENDIDO |
| 115 | Checklist final (material/evidência técnica): JWT assinado não é criptografado | guia e questões1/2/10 | ATENDIDO |
| 116 | Checklist final (material/evidência técnica): Chave aleatória externa ao Git | chave local RNG; .env ignorado; inspeção de candidatos | ATENDIDO |
| 117 | Checklist final (material/evidência técnica): Login reutiliza AuthenticationManager | AutenticacaoService; login HTTP real | ATENDIDO |
| 118 | Checklist final (material/evidência técnica): Token possui emissor, sujeito, emissão e expiração | teste dos seis claims e duração exata | ATENDIDO |
| 119 | Checklist final (material/evidência técnica): Perfis vêm da conta autenticada | teste contra perfis declarados pelo cliente | ATENDIDO |
| 120 | Checklist final (material/evidência técnica): Resource Server valida Bearer | Assinatura/emissor/tempo no Nimbus e guard antes da coerção; tokens válidos200, tipos/extremos/futuros401; testes reais com filtros ativos | ATENDIDO |
| 121 | Checklist final (material/evidência técnica): Basic não libera mais as rotas | HTTP Basic rejeitado401 real e Newman | ATENDIDO |
| 122 | Checklist final (material/evidência técnica): OpenAPI e Postman utilizam JWT | contrato exportado; Newman98/250; Swagger200 | ATENDIDO |
| 123 | Checklist final (material/evidência técnica): Limitação de revogação explicada | guia e teste bloqueio/mesmo JWT | ATENDIDO |
| 124 | Checklist final (material/evidência técnica): Suíte real aprovada em lugar da meta67 do exemplo | Maven290; classes anteriores preservadas | ATENDIDO |
| 125 | Erros críticos e checklist: Migrações001–006 sem alteração e sem JWT no banco | 49 changesets existentes; hashes preservados; nenhum007 | ATENDIDO |
| 126 | Erros críticos e checklist: CRUD/estoque/rollback/preço histórico/concorrência mantidos | suíte completa;19 execuções de ConcorrenciaEstoqueTest; VendaServiceTest | ATENDIDO |
| 127 | Erros críticos e checklist: Perfis dev/test/prod documentais verificados sem banco de produção | OpenApiProfilesTest: três contextos sem DataSource; filtros reais exercitados em test | ATENDIDO |
| 128 | Erros críticos e checklist: Arquivos inspecionados, diff check e histórico preservados | aula-13-arquivos.md; git diff --check; HEAD sem alteração | ATENDIDO |
| 129 | Erros críticos e checklist: Inventário e evidências sem artefatos temporários | aula-13-arquivos.md; target/.env/.idea ignorados | ATENDIDO |
| 130 | Encerramento: Marco de commit/tag/publicação sugerido no material | Adiado por proibição expressa do usuário; nada publicado | NÃO ATENDIDO |
| 131 | Aprendizagem humana: Compreensão oral e apresentação pessoal pelo estudante | Material escrito e interface automatizada não comprovam participação humana | NÃO VERIFICADO |

## Todas as operações efetivamente publicadas no contrato

| Método | Caminho | OperationId | Respostas | Acesso |
|---|---|---|---|---|
| GET | /api/pessoas/{id} | consultarPessoa | 200, 400, 401, 404 | Bearer |
| PUT | /api/pessoas/{id} | alterarPessoa | 200, 400, 401, 404, 409 | Bearer |
| PUT | /api/pessoas/{id}/status | alterarStatusPessoa | 200, 400, 401, 404 | Bearer |
| GET | /api/jogos/{id} | consultarJogo | 200, 400, 401, 404 | Bearer |
| PUT | /api/jogos/{id} | alterarJogo | 200, 400, 401, 404, 409 | Bearer |
| DELETE | /api/jogos/{id} | excluirJogo | 204, 400, 401, 404, 409 | Bearer |
| PUT | /api/jogos/{id}/status | alterarStatusJogo | 200, 400, 401, 404 | Bearer |
| GET | /api/generos/{id} | consultarGenero | 200, 400, 401, 404 | Bearer |
| PUT | /api/generos/{id} | alterarGenero | 200, 400, 401, 404, 409 | Bearer |
| DELETE | /api/generos/{id} | excluirGenero | 204, 400, 401, 404, 409 | Bearer |
| PUT | /api/generos/{id}/status | alterarStatusGenero | 200, 400, 401, 404 | Bearer |
| PUT | /api/contas-acesso/me/senha | alterarMinhaSenha | 204, 400, 401 | Bearer |
| GET | /api/colaboradores/{id} | consultarColaborador | 200, 400, 401, 404 | Bearer |
| PUT | /api/colaboradores/{id} | alterarColaborador | 200, 400, 401, 404, 409 | Bearer |
| GET | /api/clientes/{id} | consultarCliente | 200, 400, 401, 404 | Bearer |
| PUT | /api/clientes/{id} | alterarCliente | 200, 400, 401, 404 | Bearer |
| GET | /api/vendas | pesquisarVendas | 200, 400, 401 | Bearer |
| POST | /api/vendas | abrirVenda | 201, 400, 401, 404 | Bearer |
| POST | /api/vendas/{id}/itens | adicionarItemVenda | 200, 400, 401, 404, 409 | Bearer |
| POST | /api/vendas/{id}/finalizacao | finalizarVenda | 200, 400, 401, 404, 409 | Bearer |
| POST | /api/vendas/{id}/cancelamento | cancelarVenda | 200, 400, 401, 404, 409 | Bearer |
| GET | /api/pessoas | pesquisarPessoas | 200, 400, 401 | Bearer |
| POST | /api/pessoas | cadastrarPessoa | 201, 400, 409 | Público |
| GET | /api/jogos | pesquisarJogos | 200, 400, 401 | Bearer |
| POST | /api/jogos | cadastrarJogo | 201, 400, 401, 404, 409 | Bearer |
| POST | /api/jogos/{id}/estoque/saidas | saidaEstoque | 200, 400, 401, 404, 409 | Bearer |
| POST | /api/jogos/{id}/estoque/entradas | entradaEstoque | 200, 400, 401, 404, 409 | Bearer |
| GET | /api/generos | pesquisarGeneros | 200, 400, 401 | Bearer |
| POST | /api/generos | cadastrarGenero | 201, 400, 401, 409 | Bearer |
| GET | /api/fornecedores | listarFornecedores | 200, 401 | Bearer |
| POST | /api/fornecedores | cadastrarFornecedor | 201, 400, 401, 409 | Bearer |
| POST | /api/contas-acesso | criarContaAcesso | 201, 400, 404, 409 | Público |
| GET | /api/colaboradores | pesquisarColaboradores | 200, 400, 401 | Bearer |
| POST | /api/colaboradores | cadastrarColaborador | 201, 400, 401, 404, 409 | Bearer |
| GET | /api/clientes | pesquisarClientes | 200, 400, 401 | Bearer |
| POST | /api/clientes | cadastrarCliente | 201, 400, 401, 404, 409 | Bearer |
| POST | /api/autenticacao/login | autenticar | 200, 400, 401 | Público |
| GET | /api/vendas/{id} | consultarVenda | 200, 400, 401, 404 | Bearer |
| GET | /api/health | verificarHealth | 200 | Público |
| GET | /api/fornecedores/{id} | consultarFornecedor | 200, 400, 401, 404 | Bearer |
| GET | /api/contas-acesso/me | consultarMinhaConta | 200, 401 | Bearer |
| DELETE | /api/vendas/{vendaId}/itens/{itemId} | removerItemVenda | 204, 400, 401, 404, 409 | Bearer |

## Decisão e limites

O incremento técnico está preparado para auditoria independente após os resultados finais registrados. A publicação está deliberadamente pendente, fora da autorização atual; não é defeito técnico. A apresentação/compreensão humana continua NÃO VERIFICADA. A avaliação e o nível alcançado pertencem ao professor. Não se afirma conformidade integral de produção nem validação integral OpenAPI pelo validador próprio.

## Reavaliação após a auditoria independente

A auditoria independente reclassificou54,104 e120 como PARCIAL. Após a correção dos claims originais, as fases vermelhas reproduziram os defeitos e a suíte final aprovou346 testes. Newman98/250, Node27 e51 requisições adversariais passaram sem falhas. Esses três itens voltam a ATENDIDO com as evidências novas acima, não apenas por alteração de rótulo. A contagem permanece131:129 ATENDIDOS, zero PARCIAIS, um NÃO ATENDIDO (publicação expressamente pendente) e um NÃO VERIFICADO (comprovação humana). O relatório da auditoria anterior continua sendo evidência histórica; não representa o código corrigido.
