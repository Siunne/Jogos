# Aula 13 — Inventário local de arquivos

41 candidatos: 14 modificados e 27 novos. Nenhum preparado no staging. Não inclui segredos/configuração local ignorada.

## Modificados

- .env.example
- pom.xml
- scripts/aula12-seguranca.test.cjs
- src/main/java/br/com/emilly/jogos/config/OpenApiSegurancaConfig.java
- src/main/java/br/com/emilly/jogos/controller/ContaAcessoController.java
- src/main/java/br/com/emilly/jogos/exception/ApiExceptionHandler.java
- src/main/java/br/com/emilly/jogos/security/SecurityConfig.java
- src/main/resources/application.properties
- src/test/java/br/com/emilly/jogos/api/OpenApiAutenticacaoTest.java
- src/test/java/br/com/emilly/jogos/api/OpenApiComercialTest.java
- src/test/java/br/com/emilly/jogos/api/OpenApiConcorrenciaTest.java
- src/test/java/br/com/emilly/jogos/api/OpenApiDocumentationTest.java
- src/test/java/br/com/emilly/jogos/security/ContaAcessoSecurityTest.java
- src/test/resources/application-test.properties

## Novos

- docs/aula-13-arquivos.md
- docs/aula-13-auditoria.md
- docs/aula-13-evidencias.md
- docs/aula-13-jwt.md
- docs/aula-13-openapi.yaml
- docs/aula-13-questoes.md
- docs/aula-13-resultados.json
- docs/aula-13-transferencia.md
- docs/evidencias/aula-13-swagger-bearer.png
- postman/Jogos-Aula13.postman_collection.json
- scripts/aula13-conferir-newman.cjs
- scripts/aula13-exportar-contrato.cjs
- scripts/aula13-jwt.test.cjs
- scripts/aula13-newman.cjs
- scripts/aula13-sanitizar.cjs
- scripts/aula13-swagger-ui.cjs
- src/main/java/br/com/emilly/jogos/application/AutenticacaoService.java
- src/main/java/br/com/emilly/jogos/application/TokenService.java
- src/main/java/br/com/emilly/jogos/controller/AutenticacaoController.java
- src/main/java/br/com/emilly/jogos/dto/LoginRequest.java
- src/main/java/br/com/emilly/jogos/dto/TokenResponse.java
- src/main/java/br/com/emilly/jogos/exception/CredenciaisInvalidasException.java
- src/main/java/br/com/emilly/jogos/security/ClaimsOriginaisJwtDecoder.java
- src/main/java/br/com/emilly/jogos/security/JwtConfig.java
- src/main/java/br/com/emilly/jogos/security/JwtProperties.java
- src/test/java/br/com/emilly/jogos/security/JwtConfigurationTest.java
- src/test/java/br/com/emilly/jogos/security/JwtSecurityTest.java

## Correção dos claims

Nesta etapa foram alterados JwtConfig.java, JwtSecurityTest.java, os quatro documentos solicitados e aula-13-resultados.json. Foi criado somente ClaimsOriginaisJwtDecoder.java. Não houve alteração de BCrypt, política de senha, estoque, transações, dependências ou contratos. A enumeração acima inclui também todo o incremento original da Aula13, ainda não publicado.

## Exclusões e preservação

.env continua ignorado e não foi alterado nesta correção. target/, relatórios completos, logs e configurações pessoais do IntelliJ ficam fora da entrega. A chave conhecida em src/test é fictícia/exclusiva desse profile. Nenhum JWT ativo deve ser exportado em variáveis ou relatórios.

21 arquivos críticos foram comparados com a baseline por SHA-256 e estão preservados: migrações/master, YAMLs09–12, política de senha e núcleo de estoque/vendas. O contrato Aula13 também permanece sem mudança e foi comparado com o JSON/YAML servido. HEAD e branch preservados; nenhuma escrita no Git.
