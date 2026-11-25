# Aula 13 — Transferência para Jogos

Os nove itens acompanham a seção17 do material do professor, adaptados à loja de jogos. Os testes citados constam da execução registrada em aula-13-evidencias.md.

| Item | Decisão no projeto | Evidência |
|---|---|---|
| 1. Chave própria | Gerada com RNG criptográfico, 32 bytes no .env ignorado; valor nunca apresentado | Configuração local; JwtConfigurationTest; git check-ignore |
| 2. Emissor e duração | https://jogos.local e PT30M | JwtProperties, application.properties, teste de claims |
| 3. Login | POST /api/autenticacao/login público, AuthenticationManager e BCrypt existentes | AutenticacaoService; JwtSecurityTest |
| 4. Claims | iss/sub/iat/exp/jti e perfis da conta; nenhum dado fiscal | TokenService; loginEmiteClaimsExatosSemDadosPessoais |
| 5. Substituição | Resource Server oficial; Basic desabilitado; consultas exigem Bearer | SecurityConfig; basicEAnonimoNaoLiberamRota |
| 6. Captura automática | Login No Auth grava accessToken em variável temporária da coleção | Jogos-Aula13.postman_collection.json; Newman |
| 7. Três situações | Bearer válido200, ausente401, assinatura alterada401 | JwtSecurityTest; Newman; relatório sanitizado |
| 8. Bloqueio | Novo login401; JWT anterior continua200 até expirar | bloqueioImpedeNovoLoginMasNaoRevogaJWT; pessoaInativaImpedeNovoLoginMasNaoRevogaJWT |
| 9. Evolução de revogação | Para incidente na loja, cache distribuído de jti revogados até exp, com política de retenção; avaliar versão de credenciais e provedor central | Proposta, não implementação; seção de riscos do guia |

O perfil comercial de vendedor não é perfil de acesso. A autenticação identifica a conta que chama a API, mas esta aula não muda as permissões entre OPERADOR e ADMINISTRADOR. As vendas mantêm preço histórico, locks ordenados e rollback integral. Uma chave compartilhada por vários serviços permitiria a todos emitir tokens; uma arquitetura distribuída futura pode usar assinatura assimétrica e provedor de identidade.

Não há JWT salvo no PostgreSQL, refresh, revogação, logout com invalidação nem sessão de autenticação. A apresentação e a compreensão oral pelo estudante não foram verificadas pela automação.
