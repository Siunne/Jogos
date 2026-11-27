# Aula 15 — Inventário inicial

Fotografia da Aula 14 extraída dos XMLs Surefire. Tempo por classe inclui o custo de inicialização quando o contexto ainda não está no cache.

| Classe | Categoria | Casos | Tempo Surefire em s |
|---|---|---:|---:|
| OpenApiAutenticacaoTest | Contrato e componente | 4 | 30.762 |
| OpenApiComercialTest | Contrato e componente | 7 | 0.888 |
| OpenApiConcorrenciaTest | Contrato e componente | 1 | 0.053 |
| OpenApiDocumentationTest | Contrato e componente | 19 | 1.589 |
| OpenApiProfilesTest | Contrato e componente | 3 | 2.13 |
| OpenApiUnicodeEquivalenceTest | Contrato e componente | 2 | 1.453 |
| AutorizacaoTest | Segurança, componente, integração e concorrência | 20 | 12.35 |
| ConcorrenciaEstoqueTest | Concorrência e integração | 19 | 6.367 |
| JogoServiceTest | Integração de aplicação e persistência | 4 | 2.371 |
| VendaServiceTest | Integração de aplicação e transações | 8 | 0.595 |
| PersistencePrivacyFilterTest | Unidade de configuração e privacidade | 4 | 0.025 |
| PersistencePrivacyIntegrationTest | Integração de persistência e privacidade | 2 | 0.052 |
| Aula08ControllerTest | Componente e API | 24 | 1.424 |
| Aula10ControllerTest | Componente e API | 15 | 1.038 |
| ConsultasAula07Test | Componente e API | 5 | 0.079 |
| GeneroJogoControllerTest | Componente e API | 5 | 0.062 |
| TextoNormalizadoHttpTest | Componente e API | 8 | 2.382 |
| Aula08DomainTest | Unidade de domínio | 6 | 0.025 |
| DocumentoFiscalTest | Unidade de domínio | 22 | 0.28 |
| GeneroJogoTest | Unidade de domínio | 5 | 0.008 |
| JogoTest | Unidade de domínio | 7 | 0.01 |
| PessoaTest | Unidade de domínio | 6 | 0.015 |
| TextoNormalizadoTest | Unidade de domínio | 6 | 0.029 |
| VendaTest | Unidade de domínio | 9 | 0.031 |
| JogosApplicationTests | Componente, inicialização | 1 | 0.03 |
| MigracaoComercialTest | Integração de persistência e migração | 5 | 3.777 |
| MigracaoContaAcessoTest | Integração de persistência e migração | 2 | 1.638 |
| MigracaoGeneroTest | Integração de persistência e migração | 1 | 0.175 |
| PersistenciaJpaTest | Integração de persistência | 4 | 0.061 |
| ContaAcessoSecurityTest | Segurança, componente e persistência | 39 | 15.663 |
| JwtConfigurationTest | Segurança e configuração isolada | 6 | 0.057 |
| JwtSecurityTest | Segurança e componente | 86 | 13.744 |
| PoliticaSenhaTest | Unidade de domínio e segurança | 10 | 0.35 |
| UnicidadeGeneroTest | Integração de persistência | 1 | 0.016 |

## Casos representativos

| Categoria e método | Comportamento e oráculo | Dependências e infraestrutura | Custo | Detecta | Limite |
|---|---|---|---|---|---|
| Unidade de domínio · `VendaTest.preservaPrecoHistoricoETotal` | Preço capturado e total explícito em decimal | Entidades reais em memória | Baixo | Recalcular com preço atual | Persistência e commit |
| Unidade de aplicação · `JogoServiceUnitTest.saidaAcimaDoSaldoPreservaEstoque` | Exceção de saldo insuficiente e saldo 5 preservado | Repositório stub, domínio real, lock simulado, Mockito | Baixo | Aceitar retirada de 5,001 de um saldo 5 | Semântica do lock |
| Integração de aplicação · `JogoServiceTest.deveAtualizarUnidadesPorDirtyChecking` | Quantidade esperada após releitura | Service, proxy, JPA e PostgreSQL | Médio | Ausência de escrita ou associação incorreta | Efeito pós-commit em outro processo |
| Integração de persistência · `PersistenciaJpaTest.deveRejeitarQuantidadeNegativaNoBanco` | Operação SQL inválida gera exceção de integridade | JDBC, Liquibase e PostgreSQL | Médio | Constraint removida da migração | Validação de DTO |
| Componente · `Aula10ControllerTest.rejeitaCpfCnpjInvalidosComApiError` | Validação e corpo de erro HTTP | MockMvc, filtros, validação e contexto Spring | Médio | DTO aceita documento inválido ou resposta muda | Rede e servidor implantado |
| Contrato · `OpenApiDocumentationTest.yamlPodeSerInterpretadoECorrespondeAoJson` | Igualdade estrutural YAML e JSON | Parser e aplicação Spring em MockMvc | Médio | Publicações divergentes | Semântica de todos os payloads em produção |
| Segurança · `AutorizacaoTest.politicaHttpECicloDeTokens` | Política RBAC, status HTTP e perfis adquiridos no login | BCrypt, JWT, MockMvc e PostgreSQL | Médio | Token emitido com permissões incorretas | Configuração de proxy ou TLS |
| Concorrência · `ConcorrenciaEstoqueTest.duasVendasDisputamCincoUnidades` | Um vencedor, saldo final e estado das vendas | Threads, transações, spy de coordenação e PostgreSQL | Alto | Venda acima do saldo sob disputa | Todas as ordens de escalonamento possíveis |
| Externo · `aula14-newman.cjs`, fluxo de cadastro com operador e administrador | Status 403 e 201 e comparação das respostas com contrato | Newman, TCP loopback, aplicação empacotada e PostgreSQL | Alto | Falha na composição real com o servidor | Latência, TLS e disponibilidade de produção |

A base continha 117 arquivos Java de produção e 37 fontes de testes, das quais 34 classes tiveram relatórios Surefire e três eram auxiliares. Não havia `ContaAcessoTest` nem classe isolada de service com `MockitoExtension`. As categorias acima indicam perspectivas de evidência, com cada classe contabilizada uma vez na tabela inicial.
