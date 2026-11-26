# Aula 14 — Matriz de conformidade

Fonte dos critérios e da numeração de seções. [Material oficial](https://github.com/jeffersonarpasserini/suporteos2026/blob/main/docs/14aula/14aula.md)

As evidências das aulas anteriores servem de baseline. As verificações da implementação desta aula estão descritas em [evidências](aula-14-evidencias.md). Os critérios de avaliação recebem classificação técnica, sem atribuição de nota.

| Seção | Requisito | Evidência | Classificação |
|---|---|---|---|
| Resultados 1 | Distinguir identificação, autenticação e autorização | Guia e diagramas | ATENDIDO |
| Resultados 2 | Distinguir perfil, authority e permissão | Guia e questões | ATENDIDO |
| Resultados 3 | Interpretar 401 e 403 | Newman e AutorizacaoTest | ATENDIDO |
| Resultados 4 | Justificar menor privilégio | Matriz e justificativa | ATENDIDO |
| Resultados 5 | Converter o claim do JWT | SecurityConfig | ATENDIDO |
| Resultados 6 | Política por verbo e rota | SecurityConfig e matriz | ATENDIDO |
| Resultados 7 | Segurança de métodos | Teste direto no proxy | ATENDIDO |
| Resultados 8 | Gerir acessos preservando administração | AutorizacaoTest | ATENDIDO |
| Resultados 9 | Provisionar sem senha no repositório | Runner e variáveis externas | ATENDIDO |
| Resultados 10 | Testar permissões e proibições | Newman e Maven | ATENDIDO |
| Resultados 11 | Ler fluxos de autorização | Diagramas entregues, leitura humana sem evidência | NÃO VERIFICADO |
| Resultados 12 | Transferir a política | Implementação Jogos e justificativa | ATENDIDO |
| Pré-requisitos | Marco, banco isolado e baseline | HEAD Aula 13 e logs | ATENDIDO |
| Ponto 1 | Claim perfis produz authorities ROLE_* | Conversor e fluxos Bearer | ATENDIDO |
| Ponto 1 | Converter após validação do token | Resource Server e decoder estrito | ATENDIDO |
| Ponto 2 | Habilitar segurança de métodos | EnableMethodSecurity | ATENDIDO |
| Ponto 2 | Rotas públicas conforme ambiente | Contrato, OpenApiProfilesTest e política | ATENDIDO |
| Ponto 2 | Reservar administração e exclusões | Política, matriz e testes | ATENDIDO |
| Ponto 2 | Ordenar regras específicas antes das genéricas | SecurityFilterChain | ATENDIDO |
| Ponto 2 | Negar por padrão | denyAll | ATENDIDO |
| Ponto 3 | Defender os serviços | PreAuthorize e teste direto | ATENDIDO |
| Ponto 3 | Aplicar proxy sem autoinvocação | Controllers chamam o serviço gerenciado | ATENDIDO |
| Seção 5 | Conta sempre possui perfil válido | Domínio e DTO | ATENDIDO |
| Seção 5 | Preservar próprio perfil e acesso | AutorizacaoTest | ATENDIDO |
| Seção 5 | Impedir perda do último administrador | Contagem efetiva e advisory lock | ATENDIDO |
| Seção 6 | Criar conta inicialmente OPERADOR | HTTP 201 e teste de campos extras | ATENDIDO |
| Seção 6 | Substituir perfis | PUT perfis, HTTP 200/400/404 | ATENDIDO |
| Seção 6 | Ativar e bloquear | PUT status, HTTP 200/400/404 | ATENDIDO |
| Seção 6 | Preservar conta e senha próprias | Testes de identidade e troca de senha | ATENDIDO |
| Ponto 4 | Propriedades externas e flag desativada | .env.example e teste do contexto | ATENDIDO |
| Ponto 4 | Validar dados e duplicidades | Testes do serviço transacional | ATENDIDO |
| Ponto 4 | Criar Pessoa e ContaAcesso em conjunto | Teste de rollback | ATENDIDO |
| Ponto 4 | Conceder ambos os perfis e gravar BCrypt | Teste de bootstrap e login | ATENDIDO |
| Ponto 4 | Operação idempotente | Duas chamadas ao serviço | ATENDIDO |
| Seção 7 | Reusar estrutura Liquibase | Migrações 001–006 preservadas | ATENDIDO |
| Seção 8 | Rota protegida sem token | HTTP 401 | ATENDIDO |
| Seção 8 | Token inválido ou expirado | JwtSecurityTest | ATENDIDO |
| Seção 8 | Operador em operação cotidiana | Newman, GET jogos | ATENDIDO |
| Seção 8 | Operador criando conta | HTTP 403 | ATENDIDO |
| Seção 8 | Administrador criando conta | HTTP 201 | ATENDIDO |
| Seção 8 | Administrador alterando perfis | HTTP 200 | ATENDIDO |
| Seção 8 | Administrador bloqueando outra conta | HTTP 200 | ATENDIDO |
| Seção 8 | Autobloqueio rejeitado | HTTP 400 | ATENDIDO |
| Seção 8 | Primeiro provisionamento | Runner e teste de integração | ATENDIDO |
| Seção 8 | Segundo provisionamento sem duplicação | Teste de integração | ATENDIDO |
| Seção 9 | Bearer, requisitos administrativos e novas operações | OpenAPI Aula 14 | ATENDIDO |
| Seção 10 | Login admin, Pessoa e conta operador | Newman | ATENDIDO |
| Seção 10 | Login operador, /me e negativa na gestão | Newman | ATENDIDO |
| Seção 10 | Promoção e novo login | Newman e AutorizacaoTest | ATENDIDO |
| Seção 12 | Perfis confiáveis vêm do servidor | Teste de login com campo extra | ATENDIDO |
| Seção 12 | Documentar validade residual do JWT | Guia e testes de ciclo | ATENDIDO |
| Orientada 1 | Identificar claims da Aula 13 | Guia e teste de emissão | ATENDIDO |
| Orientada 2 | Implementar conversor | SecurityConfig | ATENDIDO |
| Orientada 3 | Habilitar segurança de métodos | EnableMethodSecurity | ATENDIDO |
| Orientada 4 | Aplicar a política HTTP | Matriz, código e exercitação HTTP | ATENDIDO |
| Orientada 5 | Fechar os cadastros públicos | Teste de 401 em rotas protegidas | ATENDIDO |
| Orientada 6 | Provisionar administrador | Bootstrap | ATENDIDO |
| Orientada 7 | Criar DTOs de administração | PerfisContaRequest e StatusContaRequest | ATENDIDO |
| Orientada 8 | Implementar operações administrativas | Controller e serviços | ATENDIDO |
| Orientada 9 | Rodar testes direcionados | AutorizacaoTest e JwtSecurityTest | ATENDIDO |
| Orientada 10 | Exercitar 401, 403 e 201 no Postman | Newman | ATENDIDO |
| Orientada 11 | Rodar a suíte completa | Log Maven | ATENDIDO |
| Autônoma 1 | Definir dois perfis | Matriz | ATENDIDO |
| Autônoma 2 | Elaborar matriz antes da configuração | Matriz produzida antes do código | ATENDIDO |
| Autônoma 3 | Justificar administração | Matriz e justificativa | ATENDIDO |
| Autônoma 4 | Converter sem confiar no cliente | Conversor, login e suíte | ATENDIDO |
| Autônoma 5 | Proteger HTTP e serviço | Política e proxy | ATENDIDO |
| Autônoma 6 | Provisionar primeira identidade | Runner condicionado | ATENDIDO |
| Autônoma 7 | Diagramar login | Diagramas | ATENDIDO |
| Autônoma 8 | Diagramar permitido e proibido | Diagramas | ATENDIDO |
| Autônoma 9 | 401, 403 e dois sucessos | Newman e Maven | ATENDIDO |
| Autônoma 10 | Documentar limites | Guia e justificativa | ATENDIDO |
| Entregáveis | Matriz, código, diagramas, testes e coleção | Inventário | ATENDIDO |
| Entregáveis | Justificativa de 300 a 500 palavras | Transferência, 359 palavras | ATENDIDO |
| Seção 15 | Responder às dez questões na ordem original | Questões e link para enunciados | ATENDIDO |
| Avaliação 1 | Fundamentação, 15% | Guia e respostas | ATENDIDO |
| Avaliação 2 | Matriz de acesso, 15% | Matriz do projeto | ATENDIDO |
| Avaliação 3 | Conversão JWT, 15% | Conversor e fluxos de perfil | ATENDIDO |
| Avaliação 4 | HTTP e métodos, 20% | Testes HTTP e serviço | ATENDIDO |
| Avaliação 5 | Gestão segura de contas, 10% | Invariantes e bootstrap | ATENDIDO |
| Avaliação 6 | Casos negativos e positivos, 15% | Maven e Newman | ATENDIDO |
| Avaliação 7 | Documentação e diagramas, 10% | Entregáveis escritos | ATENDIDO |
| Checklist | Claims, método, política pública e administrativa | Código e testes | ATENDIDO |
| Checklist | Flag padrão, segredos e último administrador | Configuração, inspeção e testes | ATENDIDO |
| Checklist | OpenAPI, Postman e diagramas | Contratos e laboratório | ATENDIDO |
| Checklist | Testes, diff e revisão | Evidências e inventário | ATENDIDO |
| Demonstração 1 | JWT sem perfis não tem acesso de negócio | Decoder estrito rejeita com 401 | ATENDIDO |
| Demonstração 2 | Operador consulta e recebe 403 ao administrar | Newman | ATENDIDO |
| Demonstração 3 | Administrador cadastra conta | Newman e Maven | ATENDIDO |
| Demonstração 4 | Alteração aparece em novo login | Ciclo de tokens | ATENDIDO |
| Demonstração 5 | Autobloqueio retorna 400 | AutorizacaoTest | ATENDIDO |
| Demonstração 6 | Bootstrap não duplica | AutorizacaoTest | ATENDIDO |
| Marco Git | Commit, tag e publicação | Pendente por instrução do usuário | NÃO ATENDIDO |
| Aprendizagem | Apresentação e comprovação pessoal | Sem sessão humana observada | NÃO VERIFICADO |

Total de 94 verificações agrupadas por requisito ou atividade efetiva. Classificação técnica com 91 atendidas, zero parciais, uma não atendida e duas não verificadas. As duas não verificadas tratam de compreensão e apresentação humanas.

## Revalidação pós-auditoria

| Requisito anteriormente parcial | Classificação | Evidência |
|---|---|---|
| Resultados 8 — gerir acessos preservando administração | ATENDIDO | Edição e troca de senha aguardam o lock. As duas alterações preservaram os status e perfis confirmados. |
| Seção 5 — impedir a perda do último administrador | ATENDIDO | AutorizacaoTest preservou a invariante e verificou o estado em transação independente. As alterações comuns não escrevem status fora desse fluxo. |
| Avaliação 5 — gestão segura de contas | ATENDIDO | Matriz HTTP, testes diretos de serviços e falha no INSERT do bootstrap com rollback integral. |
| Checklist — flag padrão, segredos e último administrador | ATENDIDO | Bootstrap desativado por padrão, inspeção dos candidatos e regressões administrativas aprovadas. |

As 94 verificações oficiais ficam em 91 atendidas, zero parciais, uma não atendida por publicação pendente e duas não verificadas por dependerem de evidência humana. A exigência documental está atendida pela alternativa autorizada na finalização. As dez perguntas foram conferidas com a seção 15 e aparecem reformuladas, com numeração e referência ao original. A transcrição integral depende de licença permissiva, que não foi confirmada.

## Parecer definitivo

| Categoria | Resultado |
|---|---|
| Defeito funcional existente | Nenhum defeito remanescente foi identificado nos chamadores atuais revisados. |
| Documentação obrigatória | As dez perguntas reformuladas e as respectivas respostas foram confrontadas com a seção 15. A referência aponta para a seção original. |
| Evidência histórica | Os metadados do Hibernate registram que os UPDATEs anteriores incluíam status. A versão intermediária da Aula 14 não tem revisão própria no Git. O HEAD publicado é da Aula 13. A auditoria preserva a prova estrutural anterior, sem afirmar reprodução dinâmica histórica. |
| Evidência da correção | Os testes PostgreSQL confirmaram a espera pelo lock e a preservação de status e perfis por leitura em nova transação. O rollback real do bootstrap foi comprovado. |
| Limite arquitetural | Futuros chamadores que pré-carreguem entidades em transações próprias devem preservar a ordem de aquisição e a leitura após o lock. Escritas SQL externas que ignorem a coordenação ficam fora da garantia. |
| Melhoria fora do escopo | Uma versão histórica arquivada permitiria cotejar as duas implementações. A indisponibilidade dessa evidência não caracteriza falha funcional atual. |
| Publicação | Tecnicamente pronta. A execução do marco Git aguarda autorização expressa. |