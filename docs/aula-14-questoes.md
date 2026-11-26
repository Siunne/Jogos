# Aula 14 — Questões de discussão

As dez perguntas abaixo seguem a numeração e o sentido da seção 15. A consulta aos metadados e à árvore do repositório oficial não confirmou uma licença que autorize a transcrição integral. Por isso, foram mantidas as reformulações e as respostas existentes. [Enunciados oficiais](https://github.com/jeffersonarpasserini/suporteos2026/blob/main/docs/14aula/14aula.md#15-quest%C3%B5es-para-discuss%C3%A3o)

## 1. Como um token autenticado ainda pode levar a uma resposta HTTP 403?

O JWT prova a identidade. A política ainda confere se essa identidade tem o perfil exigido. Um operador autenticado recebe 403 ao tentar cadastrar uma conta.

## 2. Como as verificações por papel e por authority interpretam seus argumentos?

hasRole aplica a convenção ROLE_. hasAuthority compara a autoridade exata. Assim, hasRole('ADMINISTRADOR') corresponde a hasAuthority('ROLE_ADMINISTRADOR').

## 3. Por que o enum guarda o nome do perfil sem o prefixo usado pelo Spring?

O enum representa perfis do domínio. O prefixo é uma convenção do Spring Security aplicada pelo conversor.

## 4. Qual risco surgiria se o login aceitasse perfis escolhidos pelo cliente?

Um cliente poderia se atribuir direitos administrativos. Os perfis emitidos em Jogos vêm da conta carregada pelo servidor.

## 5. Por que tirar um botão da interface ainda deixa a operação acessível a um cliente HTTP?

Um cliente pode chamar a API diretamente. O servidor precisa aplicar a regra para qualquer origem da requisição.

## 6. O que a proteção no serviço acrescenta à barreira HTTP?

A política HTTP bloqueia a requisição cedo. A proteção do serviço também cobre chamadas de outros pontos de entrada ao caso de uso.

## 7. Como uma chamada entre métodos do mesmo objeto afeta a aplicação de PreAuthorize?

Uma chamada interna no próprio objeto pode contornar o proxy, deixando a anotação sem efeito nessa chamada.

## 8. Por que o JWT já emitido conserva seus perfis e quando a mudança aparece?

O JWT já foi assinado e contém uma cópia dos perfis no momento da emissão. As mudanças aparecem em tokens emitidos em novo login.

## 9. Qual capacidade o sistema perderia se bloqueasse seu último administrador?

Sem administrador efetivamente ativo, a loja perde a capacidade normal de gerenciar contas e restabelecer acessos.

## 10. Como a execução paralela em várias instâncias afeta o provisionamento inicial?

Uma verificação seguida de inserção pode correr em mais de uma instância. Em Jogos, instâncias no mesmo PostgreSQL compartilham o lock transacional. Escritores que ignorem o lock ficam fora dessa garantia.
