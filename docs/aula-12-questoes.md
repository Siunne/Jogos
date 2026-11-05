# Aula 12 — Dez questões para discussão

Respostas na ordem das [dez questões oficiais, seção14](https://github.com/jeffersonarpasserini/suporteos2026/blob/main/docs/12aula/12aula.md). A preparação escrita não comprova participação em discussão ou apresentação.

1. **Senha em Colaborador.** Isso obrigaria toda pessoa com acesso a ser funcionária. Na loja, um cliente também pode ter conta, e um colaborador pode não precisar de login. Por isso a credencial fica em ContaAcesso.
2. **Base64 e criptografia.** Base64 só muda a representação e qualquer pessoa pode decodificar. Criptografia protege usando uma chave. HTTP Basic precisa de HTTPS para proteger a transmissão.
3. **Hashes diferentes.** BCrypt usa um salt aleatório em cada geração. A mesma senha resulta em hashes diferentes, mas ambos podem ser verificados com matches.
4. **Comparação por equals.** Gerar um novo hash cria outro salt. Comparar esse texto com o hash antigo quase sempre falha. A verificação correta é passwordEncoder.matches.
5. **ADMINISTRADOR no request.** O próprio cliente poderia aumentar seus privilégios. O DTO não possui esse campo, e o cadastro cria somente OPERADOR.
6. **Quando CSRF faz falta.** Se o navegador envia credenciais automaticamente, por exemplo por cookie ou Basic guardado, outra página pode provocar uma ação. Ser stateless não resolve isso sozinho; a política precisa acompanhar o cliente usado.
7. **401 e403.** 401 é falta de autenticação válida. 403 indica uma proibição para uma identidade autenticada, no modelo explicado na aula. Aqui não há permissões diferentes por perfil.
8. **Conta bloqueada.** Não devemos confirmar que a senha estava correta ou que a conta existe. Uma resposta genérica diminui a informação disponível para tentativas de invasão.
9. **Bootstrap em produção.** Seriam necessários convite ou provisionamento administrativo, verificação de identidade, limitação de tentativas, TLS e auditoria. O cadastro público desta aula é uma simplificação.
10. **Controller e filtros.** O teste de controller verifica contrato e regra de negócio; o de segurança prova a autenticação. Neste projeto os filtros antigos continuam ativos com uma identidade simulada nos testes de controller. Os testes de autenticação usam Basic e contas reais de jogos_test, sem WithMockUser.
