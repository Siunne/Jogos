# Aula 14 — Evidências

Todas as execuções com persistência usaram o profile test e o banco PostgreSQL jogos_test. As credenciais do laboratório foram geradas em memória. O administrador inicial usado em cada execução foi removido após o laboratório. O .env local e a chave JWT continuaram em uso conforme a configuração anterior.

| Verificação | Baseline Aula 13 | Aula 14 |
|---|---:|---:|
| Maven completo | 346 aprovados | 362 aprovados |
| Casos de autenticação JWT no Maven | 86 aprovados | 86 aprovados |
| Concorrência de estoque no Maven | 19 aprovados | 19 aprovados |
| Autorização, bootstrap e invariantes no Maven | — | 16 aprovados |
| Newman | 98 requisições e 250 verificações | 112 requisições e 278 verificações |
| Node | 27 aprovados | 30 aprovados |

As rodadas finais acima tiveram zero falhas. O Node verificou os artefatos sanitizados atualizados da Aula 14 e manteve as asserções que se referem explicitamente a artefatos históricos. O comparador validou o subconjunto de JSON Schema declarado para as 112 respostas e cobriu as 44 operações do contrato.

## Resultados HTTP

| Operação | Resultado observado |
|---|---|
| Rota protegida sem credenciais, JWT malformado/expirado ou Basic | 401 |
| JWT com tempos inválidos ou subject de tipo errado | 401 |
| OPERADOR consulta jogos e própria conta | 200 |
| OPERADOR administra contas ou exclui registros | 403 |
| ADMINISTRADOR cria conta inicialmente OPERADOR | 201 |
| ADMINISTRADOR altera perfis, bloqueia e reativa outra conta | 200 |
| Administrador tenta bloquear a si mesmo ou perder seu próprio perfil administrativo | 400 |
| Um JWT administrativo ainda válido tenta indisponibilizar o último administrador efetivo | 400 |
| Conta bloqueada tenta novo login | 401 |
| Após reativação da conta com Pessoa ativa | 200 no login |

O AutorizacaoTest chama serviços reais pelo proxy Spring para a evidência de defesa em profundidade. Os testes HTTP usam filtros ativos, registros persistidos e JWT obtido no endpoint de login. A disputa PostgreSQL usa dois threads e transações reais, com mudança simultânea no status da conta e da Pessoa. Uma operação é rejeitada e a contagem posterior confirma um administrador efetivo.

Os 346 casos anteriores foram preservados. Os casos de exclusão e de mudança de status de Pessoa que exercitam regras de negócio passaram a usar acesso administrativo apenas quando necessário. O cadastro público provisório foi substituído pela expectativa autenticada. Identidade simulada nesses testes de regra exercita autorização ou lógica de negócio. A evidência de autenticação vem das suítes JWT e Newman.

## Contrato e histórico

JSON servido, YAML servido e contrato exportado foram comparados estruturalmente pelos testes. O resultado contém 44 operações, 30 caminhos, 39 schemas e dez grupos. As migrações 001–006, os YAMLs e as coleções anteriores permanecem preservados. Liquibase continuou com 49 changesets. Permanecem incluídos CRUD, política de senha, privacidade, CPF/CNPJ, estoque, concorrência, rollback e preço histórico.

Logs ignorados pelo Git em target incluem aula14-baseline-maven.log, aula14-baseline-newman.log, aula14-maven-final-2.log, aula14-newman.log, aula14-node-final.log e aula14-swagger-final.log. Resultados intermediários foram usados no diagnóstico e não integram a contagem final.

## Swagger UI

O Chrome headless executou Try it out e Execute no Swagger. O login do operador retornou 200, a consulta da própria conta retornou 200 e a tentativa de cadastro de conta retornou 403. Um novo login no formulário com a conta administrativa retornou 200. O mesmo cadastro retornou 201. As respostas foram observadas pela sessão do navegador e a autorização foi aplicada por AuthActions. O laboratório registrou apenas caminhos e códigos HTTP. A comprovação humana permanece não verificada.
## Revalidação da correção

A rodada Maven após a correção aprovou 366 testes, incluindo 86 casos JWT, 20 de AutorizacaoTest e 19 de concorrência de estoque. O teste concorrente observou as duas escritas comuns aguardarem o advisory lock em pg_locks. A transação administrativa aplicou status e perfis, confirmou as mudanças e liberou as escritas. A consulta em nova transação confirmou status, perfis, senha e edição cadastral após bloqueio/inativação e após reativação.

O teste de bootstrap confirmou SQLSTATE 23514 gerado no INSERT da conta após o trigger encontrar a Pessoa vinculada. A leitura posterior em transação independente encontrou zero contas e zero Pessoas correspondentes. O trigger e a função foram removidos no encerramento do teste.

A matriz HTTP exercitou 44 operações com três identidades. Credenciais vieram de login real. Para as recusas, verificou 401/403 e ausência de handler MVC. Para as permissões, verificou que o handler foi selecionado. Quando o caso usou corpo ausente ou id inexistente, esse resultado valida a barreira de autorização. Newman e as suítes funcionais exercitam os efeitos reais com dados válidos. Os critérios de segurança de métodos permanecem cobertos separadamente.

A suíte de JWT ampliou as entradas de perfis com tokens assinados por uma chave fictícia de teste. Ausência, nulo, tipos impróprios, maiúsculas/minúsculas incorretas, espaços, prefixo indevido e mistura inválida resultaram em 401. Duplicatas de OPERADOR mantiveram o acesso operacional. O teste não presume que um cliente externo consiga assinar tokens.

A evidência anterior do problema está no metamodelo da auditoria. Esta etapa não reproduziu um bypass antes da correção. Os logs da rodada atual são correcao14-verificado-maven.log, correcao14-verificado-newman.log e correcao14-verificado-node.log em target.

## Conferência final direcionada

Os controllers de Pessoa e ContaAcesso encaminham identificadores, principal e DTOs aos serviços, sem carregar previamente entidades. Os comandos começam a transação no serviço e adquirem o advisory lock antes das leituras relevantes. Open EntityManager in View está desativado. ClienteService e ColaboradorService consultam Pessoa para seus próprios vínculos. ContaAcessoUserDetailsService consulta a conta para autenticação. Esses chamadores não alteram status ou perfis. O bootstrap é a outra entrada de escrita de perfis e obtém o lock antes de consultar os administradores ativos. A FK da conta usa ON DELETE RESTRICT para Pessoa vinculada.

A revisão compara evidência estrutural anterior com evidência dinâmica posterior. A árvore publicada é da Aula 13 e não contém a implementação intermediária da Aula 14 como uma revisão recuperável. Uma reconstrução por alterações manuais não seria uma cópia comprovada daquele estado. O relatório de metadados anterior e os testes PostgreSQL da implementação atual permanecem as referências.

A finalização foi documental. Foram conferidos os registros anteriores de 366 testes Maven, incluindo 86 JWT, 20 de autorização e bootstrap e 19 de concorrência de estoque, 30 testes Node e 112 requisições Newman com 278 verificações, todos aprovados. A suíte não foi reexecutada nesta etapa.