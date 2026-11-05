# Aula 10 — Auditoria requisito por requisito

Fonte relida integralmente: https://github.com/jeffersonarpasserini/suporteos2026/blob/main/docs/10aula/10aula.md. Requisitos pedagógicos repetidos são relacionados às mesmas evidências. Sugestões de Git do professor não se sobrepõem à proibição da autora. Extensões opcionais não foram convertidas em obrigações.

| Nº | Seção/requisito | Arquivo responsável | Teste ou evidência | Situação |
|---|---|---|---|---|
| 1 | Pré-requisitos: Baseline antes de editar, Git limpo, 85 testes | docs/aula-10-pessoas-vendas.md | target/aula10-baseline.log: BUILD SUCCESS | ATENDIDO |
| 2 | §§1–7: Adaptar Produto/GrupoProduto sem renomear Jogo/GeneroJogo | domain/Jogo.java; ItemVenda.java | OpenApiDocumentationTest: 38 mappings exatos | ATENDIDO |
| 3 | §§2–4: Identidade central Pessoa, composição e ausência de herança JPA | domain/Pessoa.java, Cliente.java, Colaborador.java | PessoaTest.papeisSimultaneosComMesmaIdentidade | ATENDIDO |
| 4 | §§3–4: Explicar substituição, limites da herança e diferença de função/permissão | docs/aula-10-pessoas-vendas.md | Diagrama e questões 1–3; ContaAcesso somente conceitual | ATENDIDO |
| 5 | §10: Pessoa: nome/email/CPF/data/status e alterações validadas | Pessoa; RegrasComerciais.texto | TextoNormalizadoTest (6); TextoNormalizadoHttpTest (8): criação/PUT, controles e expansão do e-mail; leituras PostgreSQL independentes | ATENDIDO |
| 6 | §§10,14: CPF único e conflito409 | PessoaService; PessoaRepository; migração005 | Aula10ControllerTest.rejeitaCpfEEmailDuplicadosSemBloquearProprioId | ATENDIDO |
| 7 | §§10,14: Email válido, minúsculo, único normalizado | Pessoa.alterarDados; índice uk_pessoa_email_normalizado | PessoaTest; Aula10ControllerTest; MigracaoComercialTest | ATENDIDO |
| 8 | §10: Data gerada no servidor e status inicial ATIVO | Pessoa construtor; PessoaRequest | cadastroPessoaNormalizaRetornaLocationEDateDoServidor | ATENDIDO |
| 9 | §10: PUT preserva data/status e valida alterações no domínio | Pessoa.alterarDados | PessoaTest.alteracaoPreservaDataEStatus | ATENDIDO |
| 10 | §§10,21: Ativação/inativação não apaga histórico | PessoaService.status; VendaService.buscar | jogoVendidoNaoPodeSerExcluidoEHistoricoSobreviveInativacao | ATENDIDO |
| 11 | §10: Cliente: pessoa obrigatória, FK e unicidade | domain/Cliente.java; ClienteRepository;005 | criaPapeisSimultaneosComLocationEImpedeDuplicidade | ATENDIDO |
| 12 | §14: Papel criado somente com pessoa ativa | Cliente construtor; Colaborador construtor | PessoaTest.inativaNaoRecebePapel; papelExigePessoaAtiva | ATENDIDO |
| 13 | §10: Telefone opcional e editável, máximo30 | Cliente.alterarDados; ClienteAtualizacaoRequest | PessoaTest; criaPapeisSimultaneosComLocationEImpedeDuplicidade | ATENDIDO |
| 14 | §10: Colaborador: pessoa obrigatória e única | Colaborador; ColaboradorRepository;005 | criaPapeisSimultaneosComLocationEImpedeDuplicidade | ATENDIDO |
| 15 | §10: Matrícula única e alteração com exclusão do próprio ID | ColaboradorService.matricula | matriculaDuplicadaEFuncaoInvalida | ATENDIDO |
| 16 | §10: Enum das quatro funções e data de admissão obrigatória | FuncaoColaborador; Colaborador | PessoaTest.colaboradorValidaFuncaoEAdmissao | ATENDIDO |
| 17 | §15.4: PUT não substitui pessoa nem admissão | ColaboradorAtualizacaoRequest | OpenApiComercialTest.novosDtosDocumentadosComExemplosValidos; teste HTTP de alteração | ATENDIDO |
| 18 | §§10,14: Somente vendedor/gerente aptos a vender | Colaborador.validarVendedor | PessoaTest; VendaServiceTest.funcaoAlteradaImpedeFinalizacao | ATENDIDO |
| 19 | §§8–9: CPF: DVs reais e exemplo52998224725/24 | DocumentoFiscal.cpfValido | DocumentoFiscalTest.exemplosOficiais | ATENDIDO |
| 20 | §§8–9: CNPJ: DVs reais e exemplo11222333000181/82 | DocumentoFiscal.cnpjValido | DocumentoFiscalTest.exemplosOficiais | ATENDIDO |
| 21 | §§8–9: Repetidos, null, vazio, espaços, tamanho e caracteres indevidos | DocumentoFiscal | cpfRejeitaFronteiras; cnpjRejeitaFronteiras, parametrizados | ATENDIDO |
| 22 | §9: Obrigatoriedade separada de validade fiscal | CpfValidator, CnpjValidator, anotações @NotBlank | validadoresSeparamObrigatoriedade | ATENDIDO |
| 23 | §9: Validar nos DTOs e novamente no domínio | PessoaRequest; FornecedorRequest; Pessoa; Fornecedor | DocumentoFiscalTest: fronteiras nas duas camadas; rejeitaCpfCnpjInvalidosComApiError | ATENDIDO |
| 24 | §§6,21: Não confundir DV com existência ou titularidade | docs/aula-10-pessoas-vendas.md; descrições OpenAPI | Explicação fiscal e respostas às questões6/7 | ATENDIDO |
| 25 | §9: Fornecedor passa a validar CNPJ sem perder associação opcional | Fornecedor; FornecedorRequest | 85 testes anteriores preservados; Newman 30 pedidos antigos | ATENDIDO |
| 26 | Pedido §3: Inspecionar legado e não corrigir documentos arbitrariamente | docs/aula-10-pessoas-vendas.md | SELECT dev2/2 inválidos e test12/12; proposta de saneamento, sem UPDATE | ATENDIDO |
| 27 | §11: Venda começa ABERTA com data do servidor | Venda construtor | VendaTest; Aula10ControllerTest.abrir; Newman | ATENDIDO |
| 28 | §11: Somente aberta aceita inclusão e remoção | Venda.validarAberta, adicionarItem, removerItem | VendaTest.finalizadaNaoAlteraNemEstorna; contrato HTTP | ATENDIDO |
| 29 | §11: Venda vazia não finaliza | Venda.validarFinalizacao | VendaTest.vaziaNaoFinaliza; Newman | ATENDIDO |
| 30 | §11: Cancelamento somente da aberta, sem reabertura ou estorno | Venda.cancelar | VendaTest.cancelaSemBaixaENaoReabre | ATENDIDO |
| 31 | §§5,11: Entidade associativa ItemVenda, não ManyToMany direto | ItemVenda; Venda.itens | JPA e diagrama; vendaAdicionaRemoveReincluiEValidaSnapshot | ATENDIDO |
| 32 | §11: Quantidade positiva, BigDecimal e precisão18/3 | ItemVenda; ItemVendaRequest;005 | VendaTest.rejeitaQuantidadesInvalidas; dtoQuantidadeRejeitaMaisDeTresCasas | ATENDIDO |
| 33 | §11: Preço não negativo, BigDecimal, precisão18/2 | ItemVenda construtor;005 | Validação de domínio e CHECK inspecionado em MigracaoComercialTest | ATENDIDO |
| 34 | §§5,11: Snapshot de preço100×2=200 preservado após catálogo120 | ItemVenda.valorUnitario | snapshotPersistidoEFinalizacaoBaixamUmaUnicaVez; preservaPrecoHistoricoETotal | ATENDIDO |
| 35 | §11: Subtotal e total com escala2 e HALF_UP | calcularSubtotal; calcularTotal | subtotalFracionarioArredondaComHalfUp | ATENDIDO |
| 36 | §11: Total derivado, sem coluna total | Venda.calcularTotal;005 | VendaResumo sem total; inspeção da migração e OpenApiComercialTest | ATENDIDO |
| 37 | §11: Não repetir mesmo jogo na venda, par único no banco | Venda.adicionarItem; uk_item_venda_venda_id_jogo_id | rejeitaJogoRepetido; rejeitaDuplicadoEQuantidadeZeroSemBaixar | ATENDIDO |
| 38 | §5: Jogo em várias vendas; venda com vários jogos | ItemVenda ManyToOne dos dois lados | VendaServiceTest.paginaResumoExecutaNoBancoSemBuscarColecoes; Newman | ATENDIDO |
| 39 | §14: Inclusão de item não baixa estoque | VendaService.adicionarItem | preservaPrecoHistoricoETotal; snapshotPersistidoEFinalizacaoBaixamUmaUnicaVez; Newman saldo10 | ATENDIDO |
| 40 | §14: Cancelar venda aberta não baixa estoque | VendaService.cancelar | cancelamentoMantemSaldo; vaziaNaoFinalizaECancelamentoNaoBaixa | ATENDIDO |
| 41 | §14: Finalização valida participantes ativos e função adequada | Venda.validarFinalizacao/validarParticipantes | Testes separados de cliente inativo, vendedor inativo e função alterada | ATENDIDO |
| 42 | §14: Jogos ativos na inclusão/finalização | Venda.adicionarItem; VendaService.finalizar | VendaTest.rejeitaJogoInativo; VendaServiceTest.jogoInativoImpedeFinalizacao | ATENDIDO |
| 43 | §14: Baixa por métodos existentes e estoque nunca negativo | VendaService.finalizar; Jogo.retirarUnidades | snapshotPersistidoEFinalizacaoBaixamUmaUnicaVez; rollback; testes anteriores de estoque | ATENDIDO |
| 44 | §14: Finalização em transação única | VendaService.finalizar @Transactional | Sucesso persistido e rollbackOnly provocado por falha do service | ATENDIDO |
| 45 | §17: Rollback real: flush/JDBC e leitura posterior independente | VendaServiceTest | rollbackRealDoSegundoItemPreservaAmbosSaldosEVendaAberta | ATENDIDO |
| 46 | §14: Impedir segunda finalização e baixa duplicada | Venda.validarAberta | finalizadaNaoAlteraNemEstorna; Newman | ATENDIDO |
| 47 | §§14,21: Documentar concorrência, não implementar locks | docs/aula-10-pessoas-vendas.md | Limitação explícita: atualização perdida; sem @Version/Lock | ATENDIDO |
| 48 | §12: Migração incremental005 registrada no master | 005-pessoas-clientes-vendas.yaml; master | MigracaoComercialTest e PersistenciaJpaTest18+24 | ATENDIDO |
| 49 | §12: Não modificar changesets001–004 | src/main/resources/db/changelog/changes | git diff somente de leitura: nenhum arquivo anterior alterado | ATENDIDO |
| 50 | §12: Cinco tabelas e tipos reais do domínio Jogos | 005-pessoas-clientes-vendas.yaml | MigracaoComercialTest verifica cinco tabelas | ATENDIDO |
| 51 | §12: PKs e seis FKs | 005-pessoas-clientes-vendas.yaml | MigracaoComercialTest consulta pg_constraint | ATENDIDO |
| 52 | §12: Unicidade CPF/email/matrícula/pessoa por papel/venda-jogo | 005-pessoas-clientes-vendas.yaml; ItemVenda | MigracaoComercialTest: testes separados 23505/23514 e nomes esperados; duplicação real do par venda/jogo rejeitada | ATENDIDO |
| 53 | §12: CHECK de status, função, quantidade e preço | 005-pessoas-clientes-vendas.yaml | MigracaoComercialTest verifica sete CHECKs | ATENDIDO |
| 54 | §12: RESTRICT protege histórico e CASCADE só venda→itens | 005-pessoas-clientes-vendas.yaml | jogoVendidoNaoPodeSerExcluidoEHistoricoSobreviveInativacao; Newman409 | ATENDIDO |
| 55 | §12: Rollback definido para todos os changesets | 005-pessoas-clientes-vendas.yaml | Inspeção do YAML; não executado sobre schema de dev/prod | ATENDIDO |
| 56 | Pedido §6: Aplicar master em banco/schema limpo isolado | MigracaoComercialTest | aplicaTodasMigracoesEmSchemaLimpo | ATENDIDO |
| 57 | Pedido §6: Aplicar005 após001–004 preservando legado | MigracaoComercialTest | aplica005Sobre001A004SemAlterarFornecedorLegado | ATENDIDO |
| 58 | §13: Quatro repositories das raízes | Pessoa/Cliente/Colaborador/VendaRepository | Testes integrados no PostgreSQL e consultas MockMvc | ATENDIDO |
| 59 | §14: Quatro services e regras fora dos controllers | application/*Service.java | VendaServiceTest; PessoaTest; Aula10ControllerTest | ATENDIDO |
| 60 | §15: Mappers e DTOs, sem entidades JPA expostas | mapper/*Mapper.java; dto/* | Serialização HTTP/Newman; schemas OpenAPI | ATENDIDO |
| 61 | §13: EntityGraph e OSIV desabilitado | Repositories; application.properties | HTTP fora da transação de teste e Newman real; sem LazyInitializationException | ATENDIDO |
| 62 | §13: Página de resumo sem coleção e sem N+1 desnecessário | VendaRepository.findAll; VendaResumoResponse | paginaResumoExecutaNoBancoSemBuscarColecoes: <=2 SQL, zero collection fetch | ATENDIDO |
| 63 | §15.9: Specifications combináveis para cadastros e vendas | FiltrosComerciais; Filtros | pesquisaOsTresCadastrosComFiltrosEPagina; filtrosVendasCombinadosSemColecaoEPaginacaoReal | ATENDIDO |
| 64 | §15.9: Período inclusivo e rejeição de intervalo invertido | FiltrosComerciais.vendas | rejeitaPeriodoInvertidoOrdenacaoETamanhoInvalidos | ATENDIDO |
| 65 | §15: Página base0, padrão20, máximo100, sort permitido | PaginacaoConfig; services | Testes antigos; testes HTTP novos e OpenApiComercialTest | ATENDIDO |
| 66 | §15: Cinco operações de pessoas | PessoaController | Aula10ControllerTest; Newman; mapping exato no OpenApiDocumentationTest | ATENDIDO |
| 67 | §15: Quatro operações de clientes | ClienteController | Aula10ControllerTest; Newman; mapping exato | ATENDIDO |
| 68 | §15: Quatro operações de colaboradores | ColaboradorController | Aula10ControllerTest; Newman; mapping exato | ATENDIDO |
| 69 | §15: Sete operações de vendas | VendaController | Aula10ControllerTest; Newman; mapping exato | ATENDIDO |
| 70 | §15: Criações201 com Location | Quatro controllers novos | Newman testa Location; cadastroPessoaNormalizaRetornaLocationEDateDoServidor | ATENDIDO |
| 71 | Pedido §8: Remoção de item204 sem corpo | VendaController.remover | MockMvc e Newman204 sem corpo | ATENDIDO |
| 72 | §15.13: IllegalStateException comercial400, erros404/409 corretos | ApiExceptionHandler | Transições HTTP400; referências404; duplicidades409 | ATENDIDO |
| 73 | §18: Quatro novos grupos Swagger | Controllers @Tag | OpenApiDocumentationTest e captura Chrome real | ATENDIDO |
| 74 | §17: Comparar todas as rotas e métodos reais com contrato | OpenApiDocumentationTest | 38 operações e24 caminhos; nenhuma rota anterior removida | ATENDIDO |
| 75 | §15: Documentar DTOs, exemplos e validações | SchemaPatterns; OpenApiUnicodeEquivalenceTest; aula10-unicode-equivalencia.test.cjs | 5.539 entradas contra entidades e padrões MockMvc/servidos, em Java e ECMAScript; POST/PUT com JDBC independente; zero divergências no corpus | ATENDIDO |
| 76 | §18: Exportar versão própria e preservar Aula09 | docs/aula-10-openapi.yaml; script de exportação | OpenApiComercialTest compara YAML/JSON; YAML09 permanece histórico | ATENDIDO |
| 77 | Pedido §10: Swagger disponível dev/test e bloqueado prod sem banco real | OpenApiProfilesTest | Três testes web sem DataSource; nenhum banco de produção conectado | ATENDIDO |
| 78 | §18: Executar coleção no Newman real | postman/Jogos-Aula10.postman_collection.json | target/aula10-newman.json:81 pedidos/135 assertions/0 falhas | ATENDIDO |
| 79 | §18: Gerar documentos fiscais matematicamente válidos | Prerequest Postman; aula10-contrato-gerador.test.cjs | Quatro testes Node novos, incluindo bases repetidas e horário 1793000000000; Newman 81/135 aprovado | ATENDIDO |
| 80 | Pedido §11: Preservar pedidos e testes das aulas anteriores | Coleção raiz e pasta Aulas07–09 | 30 pedidos e43 verificações anteriores executados no conjunto | ATENDIDO |
| 81 | §18: IDs capturados automaticamente | Scripts Postman | Nenhum pressuposto de ID=1; execuções sucessivas aprovadas | ATENDIDO |
| 82 | §18: Inclusão/remoção/reinclusão, finalizar/cancelar e jogo vendido | Coleção Aula10 | Newman todos os cenários, 204/200/400/409 | ATENDIDO |
| 83 | §23: Executar cenário dois jogos, snapshot, finalização e erro | docs/aula-10-evidencias-http.json | Extração direta do Newman; 10→8 e10→7; total350; remoção400 | ATENDIDO |
| 84 | §23: Síntese de até dez linhas: estado, snapshot, transação | docs/aula-10-pessoas-vendas.md | Oito linhas numeradas conectadas aos resultados reais | ATENDIDO |
| 85 | §§2,24: Diagrama e justificativa de composição no tema Jogos | docs/aula-10-pessoas-vendas.md | Mermaid e comparação com alternativa de herança | ATENDIDO |
| 86 | §25: Responder às12 questões de revisão | docs/aula-10-pessoas-vendas.md | Seção Doze questões de revisão | ATENDIDO |
| 87 | §19: Explicar futura ContaAcesso associada a Pessoa | docs/aula-10-pessoas-vendas.md | FK única futura e separação identidade/papel/autorização | ATENDIDO |
| 88 | §§19,24: Não implementar credenciais, JWT, autorização ou estorno | domain e configuração existentes | Nenhuma dessas classes/dependências adicionada; limites documentados | ATENDIDO |
| 89 | §26 Modelagem: Cardinalidades, composição e alternativas de evolução | Entidades e diagrama | PessoaTest; papéis simultâneos e futuro acesso explicados | ATENDIDO |
| 90 | §26 Documentos: DVs nas duas camadas, fronteiras e limites | DocumentoFiscalTest; docs | Null/vazio/repetidos/formato e titularidade discutidos | ATENDIDO |
| 91 | §26 Venda: Invariantes, snapshot e contrato testados | VendaTest; VendaServiceTest; Aula10ControllerTest | Unitários, persistência e HTTP reais | ATENDIDO |
| 92 | §26 Transação: Rollback demonstrado e concorrência analisada | VendaServiceTest; docs | Flush+JDBC+leitura independente; limitação explícita | ATENDIDO |
| 93 | §26 API: Experiência externa completa e reproduzível | OpenAPI; Postman; docs/aula-10-openapi-unicode.md | Maven: 178 testes e Node: 14 testes aprovados; Newman: 81 requisições/135 verificações e HTTP253 sem falhas; caso original aceito pelo padrão e HTTP201/GET200 | ATENDIDO |
| 94 | §26 Comunicação: Conectar teoria, decisão e evidências sem resultados inventados | Guia e evidências | Composição/alternativas/estado/snapshot/atomicidade e12 questões | ATENDIDO |
| 95 | §26 Transferência: Adaptar regras ao tema sem cópia mecânica de nomes | ItemVenda.jogo; Jogo.retirarUnidades | Preços e estoque de jogos reais, fornecedor opcional preservado | ATENDIDO |
| 96 | §21: Minimização de dados e documentos não usados como credenciais | PersistencePrivacyFilter; logback-spring.xml; application-dev.properties | Três testes unitários e dois integrados PostgreSQL: dados pessoais ausentes, SQLState/restrições preservados; alcance em aula-10-correcoes-auditoria.md | ATENDIDO |
| 97 | §27: Suíte completa anterior e nova no PostgreSQL | src/test/java | target/aula10-unicode-suite-final.log: 178 testes, zero falhas, erros e ignorados; anteriores preservados | ATENDIDO |
| 98 | Pedido §16: Sete testes Node existentes continuam passando | scripts de testes Node | target/aula10-unicode-node.log: 14 aprovados, incluindo os 12 anteriores | ATENDIDO |
| 99 | §27: git diff --check e inspeção de novos arquivos/segredos | Inventário e verificação final | Consultas somente de leitura; target/.env ignorados | ATENDIDO |
| 100 | Pedido §16: Reprodução sem classes/credenciais versionadas | Guia e Maven Wrapper | Cópia limpa com DB_TEST externo: Maven: 178 testes e Node: 14 testes aprovados; target/aula10-unicode-reproducao*.log | ATENDIDO |
| 101 | §23: Participação em dupla e apresentação humana | Atividade orientada documentada | Execução automatizada não substitui participação de outra pessoa | NÃO VERIFICADO |
| 102 | Regra da autora: Manter implementação local sem Git de escrita/publicação | git status/log/diff | HEAD permanece96fd68f; nenhum add/commit/push/tag/branch/merge/reset/rebase | ATENDIDO |

Contagem: 102 unidades; {"ATENDIDO":101,"NÃO VERIFICADO":1}. A rubrica foi relacionada a evidências técnicas, sem atribuir nota ou presumir aprovação do professor. O nível avançado depende também da avaliação humana.

Os resultados completos e os JSONs essenciais estão em aula-10-evidencias.md e aula-10-evidencias-http.json. Saneamento do legado em desenvolvimento exige validação externa e autorização; nenhum dado foi alterado arbitrariamente. A implementação técnica e a entrega acadêmica completa são afirmações diferentes: a participação em dupla permanece NÃO VERIFICADA.

## Reavaliação após a auditoria independente — 05/11/2026

A auditoria independente havia reclassificado 52, 75, 79, 93 e 96 como PARCIAL. Os seis achados foram corrigidos e reexecutados: [correções e alcance das provas](aula-10-correcoes-auditoria.md). Esses cinco itens estão ATENDIDOS pelas evidências atualizadas nas respectivas linhas. Contagem atual: **101 ATENDIDOS, 0 PARCIAIS, 0 NÃO ATENDIDOS e 1 NÃO VERIFICADO**. O item 101 permanece NÃO VERIFICADO: não houve comprovação de participação humana.

Maven atual: 161 testes sem falhas, erros ou ignorados. Node: 11 aprovados. Newman: 81 requisições/135 verificações/zero falhas. A proteção de logs foi comprovada nos caminhos JDBC/Hibernate testados e não significa garantia universal contra logging futuro ou configuração externa que substitua o filtro. Os diagnósticos de privacidade não reproduzem documentos pessoais completos nas novas evidências. A ausência de concorrência, autenticação e estorno continua sendo limitação intencional do escopo.

Reprodução atual em cópia de fontes sem .git/.env/classes compiladas, com configuração externa apenas DB_TEST: 161 testes sem falhas, erros ou ignorados; target/aula10-correcao-reproducao.log.

## Correção final de campos obrigatórios — 05/11/2026

A conferência independente havia reclassificado os itens 5 e 93 como PARCIAL por permitir vazio após trim. A correção e suas provas estão em [validação de textos](aula-10-validacao-textos.md). Ambos retornam a ATENDIDO: seis testes de domínio, oito HTTP/persistência e um teste OpenAPI adicional. Todos os 176 testes Maven passaram; Node: 12 testes e Newman: 81 requisições/135 verificações passaram. O HTTP real adicional executou 253 chamadas, incluindo 162 respostas400 esperadas, preservando valores anteriores e NBSP.

Contagem atual: **101 ATENDIDOS, 0 PARCIAIS, 0 NÃO ATENDIDOS, 1 NÃO VERIFICADO**. O item101 continua NÃO VERIFICADO, sem comprovação de atividade humana. Não houve saneamento de registros antigos de testes nem alteração de dados em desenvolvimento/produção. Nenhuma migração aplicada foi editada e nenhum comando de escrita no Git foi executado.

## Equivalência OpenAPI/Unicode após a última auditoria

A auditoria havia reclassificado 75 e 93 como PARCIAL por divergência no padrão. A correção preserva o domínio e foi comprovada contra 5.539 entradas, nove campos de oito DTOs, padrões efetivamente exportados, Java, ECMAScript e HTTP/JDBC. [Provas e alcance](aula-10-openapi-unicode.md). O padrão representa somente a obrigatoriedade após trim; não pretende substituir outras validações.

Contagem atual: **101 ATENDIDOS, 0 PARCIAIS, 0 NÃO ATENDIDOS e 1 NÃO VERIFICADO**. Item101 permanece sem comprovação humana. Maven:178 testes sem falhas/erros/ignorados; Node:14 aprovados; Newman:81 requisições/135 verificações/zero falhas; HTTP anterior:253 chamadas esperadas. Nenhuma regra de entidade, migração ou YAML histórico09 foi alterado.
