# Aula 13 — Dez questões de discussão

Os enunciados abaixo reformulam, na mesma ordem, as dez questões da seção18 do [material oficial](https://github.com/jeffersonarpasserini/suporteos2026/blob/main/docs/13aula/13aula.md). As respostas são material de estudo; não comprovam participação ou apresentação humana.

1. **Por que é possível ler os dados do token?** O payload usa Base64URL, que pode ser decodificado sem chave. Não devemos colocar informações confidenciais nele.
2. **Qual é a função da assinatura?** Permite detectar alterações e verificar que o emissor conhecia a chave. Não esconde o conteúdo.
3. **O segredo HS256 permite emitir tokens?** Sim. A mesma chave serve para assinar e validar, então seu compartilhamento exige cuidado.
4. **Como a autenticação difere de uma sessão HTTP?** Na sessão, o servidor guarda o contexto. No JWT stateless, cada chamada leva o token verificável; o banco comercial continua existindo.
5. **De onde devem vir os perfis?** Da conta autenticada no servidor. Se o cliente pudesse escolher, poderia declarar privilégios próprios.
6. **Qual é o alcance da expiração?** Limita o tempo de uso de um token roubado. Não evita o roubo nem impede seu uso antes do vencimento.
7. **Qual o efeito de bloquear uma conta com JWT emitido?** Impede novo login. O token anterior ainda pode funcionar até exp, pois o filtro não consulta a conta a cada chamada.
8. **Nosso login equivale a um servidor OAuth2 completo?** Não. Ele emite JWT para nossas contas; não implementa os protocolos, clientes, grants e demais recursos de um Authorization Server.
9. **Quando separar chave privada e pública?** Quando vários serviços precisam validar sem poder emitir. Nesse cenário, assinatura assimétrica é mais adequada.
10. **Ler o payload comprova autenticidade?** Não. É preciso validar assinatura, emissor e tempo com o decoder antes de confiar nos claims.
