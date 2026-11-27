# Aula 15 — Estratégia de testes

A verificação confronta a implementação com uma especificação, como exigir 403 quando um operador tenta administrar contas. A validação pergunta se os perfis modelam corretamente o trabalho da loja e requer avaliação das pessoas envolvidas. Um erro humano, como esquecer o estado da Pessoa, pode introduzir um defeito em `ContaAcesso.podeAutenticar` e produzir a falha observável de aceitar um login indevido.

O oráculo define o resultado esperado por uma fonte independente. A matriz de acesso determina 401 ou 403, a constraint determina rejeição de estoque negativo e um cálculo manual fornece o total esperado de uma venda. Partições de equivalência agrupam nulo, vazio ou branco como login ausente. Os limites 2/3 e 80/81 distinguem comprimentos proibidos e aceitos. A decisão de autenticação cruza estado da pessoa e da conta, incluindo o curto-circuito quando a conta está bloqueada.

O JUnit Platform descobre e executa a suíte. O Jupiter fornece o modelo de testes, com instância por caso por padrão. `@ParameterizedTest` executa um caso por argumento. Arrange prepara a conta, Act troca o hash e Assert compara o valor ou verifica a exceção e o estado anterior. `assertAll` agrupa observações do mesmo cenário. Testes monetários usam `compareTo` quando apenas o valor importa, pois `equals` compara também a escala. Para verificar arredondamento e escala contratual, `equals` é apropriado.

## Dublês e fronteiras

| Dublê | Papel | Exemplo em Jogos |
|---|---|---|
| Dummy | Preencher dependência alheia ao cenário | Repositório de fornecedor na retirada unitária de estoque |
| Stub | Responder de forma prescrita | `jogos.findById` devolve um Jogo de cinco unidades |
| Fake | Implementação simplificada funcional | Um catálogo em memória serviria a testes de busca sem SQL |
| Mock | Verificar interação que integra o contrato | Em um publicador hipotético, verificar o envio do evento de venda |
| Spy | Observar ou coordenar um colaborador real | `@MockitoSpyBean BloqueioEstoque` coordena o teste concorrente com SQL real |

`JogoServiceUnitTest.entradaDevolveJogoComSaldoAtualizado` observa o retorno do serviço com uma entidade em memória. `JogoServiceTest.deveAtualizarUnidadesPorDirtyChecking` verifica a alteração do saldo pelo serviço e a releitura pelo Hibernate após flush e limpeza. Repositório, transação e banco são reais na integração. Uma resposta simulada não valida SQL, constraint, lock, rollback ou mapeamento.

O Spring TestContext reutiliza contextos com configurações equivalentes. Profiles, propriedades, customizadores e substituições de beans podem mudar a chave. O spy de concorrência exige configuração própria. Nenhum teste desta aula descarta contexto. A suíte usa PostgreSQL e Liquibase real, com schema validado pelo Hibernate. Migrações históricas criam schemas exclusivos, com remoção em `finally`.

Flush força a emissão de SQL. `clear` evita observar apenas a entidade gerenciada. Rollback automático isola testes, mas deixa sem evidência efeitos que dependem de commit ou de observação por outra conexão. `VendaServiceTest.rollbackRealDoSegundoItemPreservaAmbosSaldosEVendaAberta` e `AutorizacaoTest.falhaNoInsertDeContaDesfazPessoaDoBootstrap` verificam estado em nova fronteira transacional. Os casos que fazem commit usam dados próprios e limpeza. Concorrência mede efeitos de transações distintas com latches, espera limitada, encerramento de executores e, quando necessário, observação dos locks no PostgreSQL.

## HTTP e segurança

MockMvc percorre filtros reais, MVC, validação, serviços, serialização e banco conforme cada cenário. A requisição é simulada na API Servlet. `@WithMockUser` e processadores que injetam identidade exercitam autorização com identidade conhecida. `JwtSecurityTest` e os fluxos de login de `AutorizacaoTest` passam pelo login real, decodificação e validação criptográfica. Casos negativos assinados em testes usam chaves fictícias. `ContaAcessoSecurityTest.somenteHashBcryptPersisteEVerifica` verifica uma senha com encoder real e banco. `ContaAcessoTest` comprova armazenamento e invariantes do hash em memória.

A matriz HTTP cobre 44 operações com três identidades. 401 indica identidade ausente ou inválida, 403 indica identidade válida sem autoridade suficiente. A seleção do handler comprova a passagem pela barreira quando um cenário usa corpo incompleto. Os testes funcionais verificam JSON, validação, códigos e efeitos de negócio com payloads válidos. `OpenApiDocumentationTest` compara o contrato servido. Newman usa TCP em loopback e valida fluxos externos em uma instância real com banco de testes.

## Qualidade e diagnóstico

Na unidade de conta, as rejeições de perfis compartilham uma conta e conferem seu estado ao final. Uma conta independente por rejeição, com conferência dos perfis imediatamente após cada exceção, ajudaria a localizar qual cenário alterou o estado. A auditoria classificou esse ajuste como opcional, sem defeito funcional comprovado.

A pirâmide orienta a distribuição de custo. Unidades dão diagnóstico rápido, integração cobre fronteiras reais e poucos fluxos amplos verificam a composição. FIRST pede testes rápidos, independentes, repetíveis, com verificação automática e escritos no momento adequado. Um teste flaky alterna resultados sem alteração relevante. Para investigar, confira a ordem, o isolamento, os recursos compartilhados, o relógio e as esperas. Em concorrência, registre a disputa observada e confira timeouts antes de repetir.

Use `clean verify` para eliminar dados JaCoCo antigos. Procure `prepare-agent` no log e verifique `jacoco.exec` se o relatório faltar. Confirme profile, porta e nome do banco se a conexão falhar. `test` chega ao Surefire, enquanto `verify` também gera o relatório e executa o portão. O aviso de attach dinâmico do Mockito no Java 21 indica uma dependência de instrumentação que merece revisão antes de atualizar a JVM. A coexistência foi exercitada no build com JaCoCo e os spies existentes.
