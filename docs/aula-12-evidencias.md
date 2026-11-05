# Aula 12 — Evidências reais

Execuções em05/11/2026, com PostgreSQL17.11 no banco **jogos_test**. Não houve escrita em desenvolvimento ou produção. Os números são resultados observados, não metas copiadas do SuporteOS.

## Baseline e execução final

| Verificação | Resultado real | Relatório temporário |
|---|---|---|
| Baseline Maven |198 executados, zero falhas/erros/ignorados |target/aula12-baseline-maven.log |
| Baseline Node |14 aprovados |target/aula12-baseline-node.log |
| Baseline Newman |81 requisições,135 verificações, zero falhas |target/aula12-baseline-newman.json |
| Maven final |**254 executados,0 falhas,0 erros,0 ignorados; BUILD SUCCESS;49,772s** |target/aula12-nul-maven.log; target/surefire-reports |
| Segurança com filtros ativos |**39 execuções aprovadas**; sem WithMockUser |ContaAcessoSecurityTest |
| Domínio de senha/login |**10 execuções aprovadas** |PoliticaSenhaTest |
| Migração limpa/incremental/rollback |**2 testes aprovados**, verificando constraints reais |MigracaoContaAcessoTest |
| Contrato de autenticação |**4 testes novos aprovados** |OpenApiAutenticacaoTest |
| Concorrência Aula11 |**19 execuções preservadas e aprovadas** |ConcorrenciaEstoqueTest |
| Privacidade |**Onze provas aprovadas**:4 filtro,2 integração anteriores,2 de conta e3 dos cenários NUL |PersistencePrivacyFilterTest, PersistencePrivacyIntegrationTest, ContaAcessoSecurityTest |
| Profiles dev/test/prod |**Três aprovados**, contexto documental sem DataSource |OpenApiProfilesTest |
| Node final |**19 aprovados,0 falhas**, incluindo os14 anteriores |target/aula12-nul-node.log |
| Newman real |**94 requisições,239 verificações,0 falhas** |target/aula12-newman.json, sanitizado; aula12-nul-newman.log |
| Contrato x respostas Newman |94 respostas, cobrindo as41 operações, sem falhas no subconjunto implementado |scripts/aula12-conferir-newman.cjs |

O acréscimo de56 execuções Maven conserva as198 anteriores. A correção NUL acrescentou seis execuções às248 da implementação inicial. Dez classes de testes de controller/documentação usam WithMockUser para manter o objetivo original, **com filtros ativos**. Os39 cenários de autenticação usam Basic e contas reais persistidas, sem identidade simulada. O suporte spring-boot-starter-security-test é necessário no Boot4 para integrar MockMvc/WithMockUser; não foi desabilitada a segurança globalmente.

Uma contagem antiga de persistência precisava distinguir o histórico001–004 dos incrementos005/006. Ela continua verificando18 changesets históricos e24 da005; a006 e o total49 possuem prova própria. Não se alteraram constraints para ajustar testes. Na preparação dos testes SQL novos foi necessário restaurar autocommit após Liquibase, para cada violação esperada não deixar a conexão abortada; essa correção é apenas da infraestrutura do teste.

Também foram corrigidos o registro de suporte a datas no ObjectMapper do teste e os exemplos400 novos para representar exatamente os campos ausentes observados no handler. A primeira suíte ampliada detectou essas diferenças; os resultados finais acima foram obtidos depois das correções, sem remover as verificações que falharam.

## Segurança e HTTP

Os testes confirmaram com filtros reais:

- Health, JSON/YAML, Swagger UI e bootstrap de Pessoa/conta acessíveis sem credencial.
- **Todas as38 operações protegidas** retornam401 para anônimos antes de processar dados de negócio.
- Basic correto retorna200 em `/api/contas-acesso/me`; senha errada, login inexistente, conta bloqueada e Pessoa inativa retornam401 genérico.
- POST conta retorna201 e Location `/api/contas-acesso/me`, somente ATIVA/OPERADOR. Envio de ADMINISTRADOR/BLOQUEADA não altera esses valores.
- Referência ausente retorna404; login equivalente e segunda conta da mesma Pessoa retornam409; entradas inválidas retornam400.
- Alteração de senha retorna204 com corpo vazio. Senha anterior falha, nova funciona e chamada posterior sem Basic retorna401; não se mantém sessão autenticada.
- Unicode válido funciona;36 caracteres de dois bytes são aceitos e37 rejeitados. Credencial acima de72 bytes também é rejeitada no Basic.
- Hash corrompido com custo inválido produz401, sem valor, causa interna ou erro500 no login.

O Newman executa o bootstrap, as81 requisições herdadas de CRUD/estoque/vendas e os cenários adicionais. Os IDs são capturados. Os dois casos negativos de estado no banco (conta BLOQUEADA e hash corrompido) são exercitados pela integração Java: não foram inventados endpoints administrativos para o Postman. A inativação de Pessoa e a troca de senha também são verificadas pelo Newman.

## Persistência e integridade

O PostgreSQL recebe somente hash prefixado `{bcrypt}`, diferente da entrada. matches confirma a credencial; dois cadastros com a mesma senha geram hashes diferentes. A prova lê `senha_hash` com JDBC, sem colocá-lo no relatório. Pessoa, Cliente e Colaborador não receberam campo de senha.

O teste de logs força duplicidade real de login com outro cadastro de Pessoa: observa SQLState23505 e `uk_conta_acesso_login_lower`, mas não a senha, nenhum dos dois hashes ou o login nos logs capturados. Outro teste impede emissão dos diagnósticos de autenticação mesmo em TRACE. Os testes de privacidade anteriores continuam aprovados. A verificação é para os caminhos/configurações exercitados, sem promessa universal sobre alterações futuras.

Sete changesets da006 foram registrados: **49 totais,42 anteriores**. Há duas tabelas novas, PKs, FKs, unicidades, CHECKs e índice funcional. Os testes SQL exigem SQLStates e nomes exatos:23505 para duplicidade,23514 para CHECK e23503 para referência. Rollback da006 foi executado somente em schemas temporários próprios de jogos_test e preservou as Pessoas e42 changesets anteriores. A limpeza desses schemas não atingiu public, desenvolvimento ou produção.

O código de JogoService/VendaService, as migrações001–005, os documentos e YAMLs históricos09/10/11 e a coleção Aula10 foram comparados por SHA-256 com a baseline: **29 arquivos preservados**. O master recebeu somente o include da006. Saldo, rollback, preço histórico, filtros e paginação continuam cobertos na suíte completa e no Newman autenticado.

## OpenAPI e Swagger UI

Contrato próprio exportado de uma aplicação real iniciada no profile test, porta18080: [aula-12-openapi.yaml](aula-12-openapi.yaml). São **41 operações,27 caminhos,34 schemas e nove tags**. basicAuth é HTTP Basic. As operações protegidas possuem segurança e401 documentados; Health e os dois POSTs públicos possuem security vazio. ErroAutenticacao representa o corpo real dos filtros; os demais erros mantêm ApiError.

JSON e YAML servidos foram interpretados e comparados estruturalmente ao arquivo de entrega em testes Maven. O arquivo foi reexportado após corrigir exemplos. Os exemplos400 são confrontados com respostas reais do handler. minLength/maxLength não exprimem limite de bytes UTF-8; a restrição é declarada na descrição e testada no backend. O validador Node é parcial e não certifica integralmente OpenAPI.

Foi realizada interação **real** com Swagger UI em Chrome headless isolado:41 operações renderizadas, autorização Basic por AuthActions e Try it out/Execute com **200** em `/me`. [Captura autenticada](evidencias/aula-12-swagger-autenticado.png): dados fictícios e trecho curl de Authorization ocultado no DOM antes da imagem. A captura não mostra senha nem hash. Não houve preenchimento humano do diálogo Authorize.

A autorização foi removida do estado da UI, e uma requisição HTTP independente retornou401. Entretanto, a automação não comprovou a resposta401 **renderizada** após logout; tentativas atingiram o prazo. Essa parte visual permanece **NÃO VERIFICADA**, sem atribuir causa não demonstrada nem fingir segunda captura. Metadados: target/aula12-swagger-ui.json; executor: scripts/aula12-swagger-ui.cjs.

Para repetir a parte automatizada, inicie Chrome headless isolado na porta9232 com perfil temporário em target, janela oculta, e execute o script com AULA12_BANCO_TESTE_CONFIRMADO=jogos_test. Para a parte pendente, use credenciais fictícias no Swagger, retire a autorização e observe a chamada401, sem compartilhar headers. Não é necessário alterar servidor ou banco para isso.

## Artefatos e limites

[aula-12-resultados.json](aula-12-resultados.json) contém somente contagens, nomes de suítes, códigos HTTP e metadados sem credenciais. O executor Newman normaliza o SDK e remove segredos antes de gravar; o teste Node inspeciona headers reais e corpos de conta, não uma lista ausente. Reports brutos, perfil Chrome, logs e `.env` ficam fora do inventário de publicação.

Não foram implementados JWT, autorização diferenciada, MFA, rate limiting ou provisionamento de produção. Não há garantia de segurança de HTTP Basic sem TLS; CSRF com clientes de navegador exige reavaliação. Participação/apresentação humana e a observação visual do401 permanecem pendentes. Nenhuma operação de escrita no Git foi realizada.

## Correção após a auditoria independente

A auditoria encontrou autenticação indevida com senhas de comprimentos diferentes contendo NUL. A correção e a comparação real antes/depois estão em [aula-12-correcao-nul.md](aula-12-correcao-nul.md). Agora cadastro com NUL retorna400; Basic com NUL retorna401; senha atual/nova inválidas retornam400 sem alterar o hash, comprovado por JDBC em REQUIRES_NEW. O script HTTP adicional executou16 requisições, e três provas novas verificam logs/respostas sem segredos. O relatório final atual é o da suíte254 e Node19, sem omitir a falha anterior.
