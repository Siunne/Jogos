# Aula 12 — Correção de senhas com U+0000

## Causa e correção restrita

A auditoria independente criou uma conta fictícia com12 caracteres nulos e conseguiu autenticar com13 e20 nulos. A política anterior verificava tamanho, branco e UTF-8 válido, mas U+0000 passava por todas essas verificações.

Na implementação [BCrypt do Spring Security7.0.7](https://github.com/spring-projects/spring-security/blob/7.0.7/crypto/src/main/java/org/springframework/security/crypto/bcrypt/BCrypt.java), a senha recebe um terminador zero e o material de chave é percorrido ciclicamente (`streamtowords`, deslocamento módulo comprimento). Sequências compostas só de zeros geram o mesmo fluxo de chave, independentemente desses comprimentos. Não se trata de truncar a senha no primeiro NUL: também foi comprovada equivalência entre padrões periódicos de texto com NUL interno. O teste usa quatro e seis repetições de `loja` seguidas de NUL, retirando o último terminador de cada entrada; o encoder bruto aceita ambas para um mesmo hash.

`PoliticaSenha.valida` agora exige `senha.indexOf('\0') < 0`. A mesma política já é usada por SenhaValidator, ContaAcessoService e pelo adaptador de PasswordEncoder no encode e no matches. Portanto, uma única regra protege cadastro, senha atual/nova e HTTP Basic, sem duplicação ou alteração do algoritmo. O Basic mantém401 genérico; erros de corpo usam400 e ApiError, sem valores fornecidos pelo cliente.

Mínimo12 pontos de código, máximo72 bytes UTF-8, Unicode bem formado, salt e prefixo BCrypt permanecem. Não foi proibida toda a categoria de controles ou espaços Unicode. As migrações001–006 e contratos históricos não foram modificados.

## Antes e depois por HTTP real

| Cenário | Auditoria anterior | Depois da correção |
|---|---|---|
| Cadastro com12 nulos |201 |400 |
| Cadastro com13/20 nulos |Não foi necessário à prova anterior |400 em ambos |
| Basic com12/13/20 nulos |200 em todos, sobre a conta insegura |401 em todos, inclusive fixture legada insegura no teste Java |
| Troca usando Basic20 nulos e atual13 nulos |204, indevido |401 |
| Nova senha com NUL e Basic válido |Sem proteção específica anterior |400; hash anterior preservado |
| Senha atual com NUL e Basic válido |Podia coincidir com a classe insegura |400; hash anterior preservado |
| Troca válida Unicode |Funcionava |204; antiga401, nova200, Unicode diferente401 |

Antes: `target/aula12-auditoria-reproducao.json`, execução real da auditoria. Depois: `target/aula12-nul-http.json` (**16 requisições**) e integração Java com filtros ativos. O HTTP adicional não altera registros de terceiros; cria somente fixtures próprias em jogos_test. Nenhuma senha, hash ou Authorization utilizável é salvo nesse relatório.

## Provas e resultados

Novos testes:

- `PoliticaSenhaTest.nulEPadrõesPeriodicosComNulSaoRejeitadosAntesDoEncoder`: confirma a equivalência no encoder bruto e a rejeição pela política.
- `ContaAcessoSecurityTest.cadastroRejeitaNulEmQualquerPosicaoSemPersistirNemVazar`: NUL isolado, prefixo, meio e sufixo; HTTP400; zero contas por consulta JDBC em transação REQUIRES_NEW; service direto também rejeita.
- `novaSenhaOuSenhaAtualComNulNaoAlteramHashPersistido`: HTTP400, erros por campo e hash anterior idêntico em leitura independente; senha válida anterior ainda autentica.
- `basicBloqueiaEquivalentesNulDeContaLegadaSemAlterarHash`: injeta hash inseguro somente na própria fixture de teste; Basic12/13/20 e tentativa de troca retornam401; hash permanece inalterado.
- `unicodeSemNulContinuaValidoEDiferentesNaoAutenticam`: pares ASCII, acentos compostos/decompostos, emojis, NBSP e frase com ESC; correta200/diferente401, sem normalização de senha.
- `OpenApiAutenticacaoTest.padroesServidosProibemNulSemProibirUnicodeValido`: lê o padrão real da aplicação para os três campos de senha.
- Teste Node correspondente lê o padrão exportado e o executa em ECMAScript; não repete uma regex independente hardcoded.

Resultado final: **254 Maven,0 falhas/erros/ignorados**, incluindo39 execuções de segurança,10 de política de senha,4 de contrato de autenticação,19 de concorrência e os testes anteriores de rollback/preço histórico. **19 Node aprovados**; Newman real **94 requisições/239 verificações/0 falhas**. Os248 testes anteriores e18 Node anteriores continuam aprovados. As capturas de logs dos cenários NUL somam-se às provas anteriores, totalizando11 provas de privacidade.

Relatórios: `target/aula12-nul-direcionados.log` (49 direcionados antes da adição da prova OpenAPI), `aula12-nul-maven.log`, `aula12-nul-node.log`, `aula12-nul-newman.log`, `aula12-nul-http.json`. O contrato atual foi reexportado da aplicação real e permanece com41 operações,27 caminhos,34 schemas e nove tags.

## Limites e legado

Não foram encontradas equivalências inesperadas nos pares sem NUL testados; isso não é uma prova formal sobre toda entrada possível. A correção proíbe NUL em qualquer posição, cobrindo os casos periódicos descobertos, sem prometer ausência universal de colisões criptográficas.

Nenhum hash legado foi alterado automaticamente. Contas eventualmente cadastradas anteriormente com senha contendo NUL deixam de autenticar por essa entrada e precisam de redefinição controlada; recuperação administrativa de senha continua fora desta aula. A fixture insegura do teste é própria e removida pela limpeza do teste, sem tocar fornecedores, contas reais, desenvolvimento ou produção.

Mantêm-se as pendências: participação/discussão humana e comprovação visual do401 no Swagger. A captura200 anterior é preservada; não foi produzida uma captura401 fictícia. Não houve staging, commit, push ou tag.
