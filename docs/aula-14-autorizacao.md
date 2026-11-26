# Aula 14 — Autorização por perfis

A identificação começa com o login informado. A autenticação verifica a senha BCrypt e emite um JWT HS256 com validade de 1800 segundos. A autorização decide se os perfis assinados permitem a operação solicitada. Um perfil agrupa responsabilidades do negócio, uma authority é sua representação no Spring Security e a permissão é a operação que a política libera.

O conversor transforma OPERADOR e ADMINISTRADOR em ROLE_OPERADOR e ROLE_ADMINISTRADOR. Claims inválidos continuam rejeitados pelo decoder da Aula 13. Perfis duplicados geram uma única authority. O login consulta os perfis da conta persistida. A assinatura, o emissor, os tempos e o tipo original do subject preservam a validação anterior.

A [matriz de acesso](aula-14-matriz-acesso.md) orienta a ordem dos matchers. Token ausente ou inválido gera 401 com desafio Bearer. Token válido sem perfil suficiente gera 403. Entrada ou transição inválida após autorização gera 400. As anotações PreAuthorize protegem os serviços mesmo quando chamados por outra entrada. Os controllers chamam beans gerenciados pelo Spring e os métodos administrativos não são chamados por autoinvocação.

## Primeiro administrador

Preencha localmente o .env com INITIAL_ADMIN_NAME, INITIAL_ADMIN_EMAIL, INITIAL_ADMIN_CPF, INITIAL_ADMIN_LOGIN e INITIAL_ADMIN_PASSWORD. Use um CPF válido, e-mail, login no alfabeto ASCII do projeto e senha que respeite a política de 12 caracteres, máximo de 72 bytes UTF-8 e ausência de NUL. Preserve JWT_SECRET e as credenciais já configuradas. Ative INITIAL_ADMIN_ENABLED=true somente para a inicialização local autorizada. Inicie com o profile escolhido, confira a mensagem de provisionamento e desative a flag. O profile dev importa .env. Em produção as configurações são externas à aplicação.

O runner condicional usa serviço transacional próprio. Verifica a existência de administrador disponível, valida os dados conforme as regras de Pessoa e ContaAcesso e grava Pessoa e conta na mesma transação. A senha fica armazenada em BCrypt com salt e prefixo {bcrypt}. A primeira conta recebe os dois perfis. Se já existir administrador disponível, a execução termina antes de criar registros.

## Concorrência e tokens

O advisory lock transacional do PostgreSQL serializa cadastro de conta, alteração de perfis, alteração de status de conta, alteração de status de Pessoa e bootstrap. Cada operação que pode remover disponibilidade obtém o lock antes de buscar a conta e contar administradores. Com o isolamento padrão READ COMMITTED, a consulta após aquisição observa a transação anterior concluída. A mesma chave é usada por instâncias que compartilham o banco. Escritas SQL diretas e aplicações que ignorem o lock ficam fora da garantia. A API não expõe exclusão de Pessoa e o vínculo existente é protegido por FK.

A contagem exige conta ATIVA, Pessoa ATIVO e perfil ADMINISTRADOR. A identidade do autor vem do contexto de segurança. Uma conta administrativa não pode retirar o próprio perfil nem impedir o próprio login por bloqueio ou inativação da Pessoa.

Promoção e remoção só aparecem em JWTs emitidos após novo login. Bloqueio impede emissão e a reativação a restabelece se a Pessoa estiver ativa. JWT administrativo anterior conserva privilégios até expirar. Refresh e revogação são temas da Aula 16.

## Verificação local

Execute Maven com o profile test e DB_TEST_URL apontando exclusivamente para jogos_test. Para o laboratório HTTP, rode a aplicação test em loopback, provisione um administrador fictício com as variáveis externas e execute a coleção Aula 14. A variável AULA14_BANCO_TESTE_CONFIRMADO deve ser jogos_test. O script aula14-newman.cjs roda por npm exec --offline --package=newman -- node scripts/aula14-newman.cjs e grava um relatório sanitizado em target.

No PowerShell, para a suíte local, confirme primeiro a URL do banco de testes e mantenha as credenciais no .env local.

```powershell
$env:SPRING_PROFILES_ACTIVE = 'test'
$env:DB_TEST_URL = 'jdbc:postgresql://localhost:5433/jogos_test'
.\mvnw.cmd test
```

No laboratório, rode a aplicação com o profile test, o endereço 127.0.0.1 e a configuração adicional de src/test/resources/application-test.properties. Exporte JSON e YAML de /v3/api-docs e /v3/api-docs.yaml. O primeiro fica em target/aula14-openapi.json e também alimenta o alias target/aula10-openapi.json para os testes de Unicode. O YAML da entrega fica em docs/aula-14-openapi.yaml.

```powershell
$env:AULA14_BASE_URL = 'http://127.0.0.1:18081'
$env:AULA14_BANCO_TESTE_CONFIRMADO = 'jogos_test'
npm.cmd exec --offline --package=newman -- node scripts/aula14-newman.cjs
node scripts/aula14-conferir-newman.cjs
$env:JOGOS_OPENAPI_JSON = 'target/aula14-openapi.json'
$env:JOGOS_NEWMAN_JSON = 'target/aula14-newman.json'
node --test scripts/aula14-autorizacao.test.cjs
```

Para repetir o Swagger automatizado, inicie o Chrome headless com depuração em 127.0.0.1:9234 e perfil isolado em target. Execute scripts/aula14-swagger-ui.cjs com as mesmas variáveis do Newman e as credenciais fictícias do administrador de teste.
## Correção da atualização perdida

A auditoria observou que as instruções estáticas do Hibernate incluíam status nas atualizações das duas entidades sem versionamento. Uma edição baseada em estado anterior podia escrever esse status junto com e-mail ou senha. A evidência anterior consiste em metadados do Hibernate. O encadeamento explorável do comportamento anterior permanece sem reprodução dinâmica nesta etapa.

A edição de Pessoa e a troca de senha agora usam o advisory lock administrativo antes da leitura. O lock serializa os casos de uso concorrentes e permanece retido até o commit ou rollback. A troca de senha confere a senha atual depois de adquirir o lock. Isso evita que duas trocas para a mesma conta sejam aceitas com base na mesma senha anterior. Pessoa e ContaAcesso também usam DynamicUpdate, que limita as escritas nas entidades aos atributos alterados. Assim, uma edição comum não inclui status nem alterações na coleção de perfis. O lock fornece a coordenação transacional. O conjunto de colunas atualizado reduz o acoplamento entre os tipos de escrita.

O escopo é o dos casos de uso da aplicação executados em transações READ COMMITTED com leitura após aquisição do lock. Chamadores que componham novas transações devem preservar a ordem de aquisição e evitar pré-carregar as entidades a serem alteradas. Escritores SQL externos que ignorem o lock ficam fora dessa garantia. A coordenação global reduz paralelismo para essas modificações. Nenhuma coluna ou migração persistente foi adicionada.

O teste de integração do bootstrap instala no banco de testes um trigger temporário que afeta somente o login fictício escolhido para a execução. O trigger verifica que Pessoa já está presente na transação e rejeita o INSERT da conta com SQLSTATE 23514. Depois que a transação do serviço falha, uma transação independente verifica ausência de Pessoa e de conta. O teste remove o trigger e sua função.
