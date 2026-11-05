# Aula12 — Inventário local

Conferência após correção NUL: **18 modificados e 38 novos; 56 candidatos**. Branch main, HEAD56ce7361da8f8f384c1f51125254b7a424ec8859. Nenhum staging, commit, tag ou envio ao remoto.

## Modificados

- `README.md`
- `pom.xml`
- `scripts/aula11-exportar-contrato.cjs`
- `src/main/java/br/com/emilly/jogos/config/PersistencePrivacyFilter.java`
- `src/main/resources/db/changelog/db.changelog-master.yaml`
- `src/test/java/br/com/emilly/jogos/PersistenciaJpaTest.java`
- `src/test/java/br/com/emilly/jogos/api/OpenApiComercialTest.java`
- `src/test/java/br/com/emilly/jogos/api/OpenApiConcorrenciaTest.java`
- `src/test/java/br/com/emilly/jogos/api/OpenApiDocumentationTest.java`
- `src/test/java/br/com/emilly/jogos/api/OpenApiProfilesTest.java`
- `src/test/java/br/com/emilly/jogos/api/OpenApiUnicodeEquivalenceTest.java`
- `src/test/java/br/com/emilly/jogos/application/ConcorrenciaEstoqueTest.java`
- `src/test/java/br/com/emilly/jogos/config/PersistencePrivacyFilterTest.java`
- `src/test/java/br/com/emilly/jogos/controller/Aula08ControllerTest.java`
- `src/test/java/br/com/emilly/jogos/controller/Aula10ControllerTest.java`
- `src/test/java/br/com/emilly/jogos/controller/ConsultasAula07Test.java`
- `src/test/java/br/com/emilly/jogos/controller/GeneroJogoControllerTest.java`
- `src/test/java/br/com/emilly/jogos/controller/TextoNormalizadoHttpTest.java`

## Novos

- `docs/aula-12-arquivos.md`
- `docs/aula-12-auditoria.md`
- `docs/aula-12-autenticacao.md`
- `docs/aula-12-correcao-nul.md`
- `docs/aula-12-evidencias.md`
- `docs/aula-12-openapi.yaml`
- `docs/aula-12-questoes.md`
- `docs/aula-12-resultados.json`
- `docs/evidencias/aula-12-swagger-autenticado.png`
- `postman/Jogos-Aula12.postman_collection.json`
- `scripts/aula12-conferir-newman.cjs`
- `scripts/aula12-exportar-contrato.cjs`
- `scripts/aula12-newman.cjs`
- `scripts/aula12-nul-http.cjs`
- `scripts/aula12-seguranca.test.cjs`
- `scripts/aula12-swagger-ui.cjs`
- `src/main/java/br/com/emilly/jogos/api/validation/SenhaValida.java`
- `src/main/java/br/com/emilly/jogos/api/validation/SenhaValidator.java`
- `src/main/java/br/com/emilly/jogos/application/ContaAcessoService.java`
- `src/main/java/br/com/emilly/jogos/config/OpenApiSegurancaConfig.java`
- `src/main/java/br/com/emilly/jogos/controller/ContaAcessoController.java`
- `src/main/java/br/com/emilly/jogos/domain/ContaAcesso.java`
- `src/main/java/br/com/emilly/jogos/domain/PerfilAcesso.java`
- `src/main/java/br/com/emilly/jogos/domain/PoliticaSenha.java`
- `src/main/java/br/com/emilly/jogos/domain/StatusContaAcesso.java`
- `src/main/java/br/com/emilly/jogos/dto/AlteracaoSenhaRequest.java`
- `src/main/java/br/com/emilly/jogos/dto/ContaAcessoRequest.java`
- `src/main/java/br/com/emilly/jogos/dto/ContaAcessoResponse.java`
- `src/main/java/br/com/emilly/jogos/mapper/ContaAcessoMapper.java`
- `src/main/java/br/com/emilly/jogos/repository/ContaAcessoRepository.java`
- `src/main/java/br/com/emilly/jogos/security/ContaAcessoUserDetailsService.java`
- `src/main/java/br/com/emilly/jogos/security/SecurityConfig.java`
- `src/main/resources/db/changelog/changes/006-contas-acesso.yaml`
- `src/test/java/br/com/emilly/jogos/MigracaoContaAcessoTest.java`
- `src/test/java/br/com/emilly/jogos/api/OpenApiAutenticacaoTest.java`
- `src/test/java/br/com/emilly/jogos/security/ContaAcessoSecurityTest.java`
- `src/test/java/br/com/emilly/jogos/security/PoliticaSenhaTest.java`
- `src/test/resources/db/changelog/db.changelog-ate-005.yaml`

## Motivos e preservação

POM: segurança e integração de teste do Boot4. Master: apenas include006. Filtro de logs: proteger credenciais e manter metadados técnicos seguros. Testes antigos: WithMockUser com filtros ativos e contrato atual; teste de persistência distingue001–004,005 e006 sem remover asserções históricas. Exportador Aula11: proteção contra sobrescrita com contrato Aula12. README identifica a Aula11 publicada e o incremento local.

As migrações001–005, os documentos/YAMLs históricos09/10/11, a coleção Aula10 e os services de Jogo/Venda foram preservados por comparação SHA-256 (29 arquivos). A nova imagem é captura real com Authorization ocultado porCSS antes da captura, não uma imagem inventada. Só há uma captura:200 autenticado; o401 visual permanece não verificado.

## Fora da futura publicação

`.env`, credenciais reais, relatórios brutos, logs, `target/`, perfil temporário Chrome, caches npm, `.idea/` e resultados Surefire ficam fora. O relatório Newman novo é sanitizado antes de qualquer gravação e continua somente em target. A inspeção dos candidatos e das variáveis da coleção não encontrou tokens, chaves privadas ou credenciais reais. Varredura por padrões complementa a revisão, sem garantia universal.

## Reprodução

Todos os fontes, migração, master, DTOs, configuração, testes e scripts necessários constam acima. Dependências externas: Java21, Maven Wrapper, PostgreSQL jogos_test com variáveis externas; Node e Newman para scripts, Chrome apenas para a captura opcional. Consulte aula-12-autenticacao.md. Não copiar target nem .env para publicar.

## Correção NUL

Política compartilhada, mensagem de validação, schemas dos três campos de senha e testes receberam a correção pontual. SecurityConfig e ContaAcessoService já delegavam a essa política e não precisaram ser modificados nesta correção. Foram acrescentados scripts/aula12-nul-http.cjs e docs/aula-12-correcao-nul.md; contrato Aula12 foi reexportado. Migrações001–006 e master permaneceram idênticos ao estado da auditoria. Os contratos09/10/11 e a captura visual anterior foram preservados.
