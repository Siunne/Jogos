# Aula 15 — TDD em ContaAcesso

A validação anterior do domínio exigia a estrutura BCrypt e aceitava qualquer par de algarismos no campo de custo. A faixa 04–31 corresponde aos custos aceitos na geração BCrypt e à política do domínio Jogos para novas atribuições de hash. O Spring Security 7.0.7 admite excepcionalmente custo `00` em um caminho de verificação legada. Assim, estar fora da faixa de geração não implica, em todos os casos, falha de verificação. [Implementação BCrypt do Spring Security 7.0.7](https://github.com/spring-projects/spring-security/blob/7.0.7/crypto/src/main/java/org/springframework/security/crypto/bcrypt/BCrypt.java)

Os limites 04 e 31 devem ser aceitos estruturalmente. Os vizinhos 03 e 32 devem ser rejeitados, preservando o hash anterior. Os testes usam hashes sintéticos e verificam o metadado sem chamar `encode` ou `matches`. A validação estrutural não comprova correspondência criptográfica com uma senha. O trabalho computacional cresce exponencialmente com o custo, portanto aceitar 31 na estrutura não o torna uma escolha operacional recomendada. O oráculo usa a faixa de geração como referência e valores explícitos, sem repetir a expressão do método em teste.

Os endpoints recebem senhas em DTOs sem campo para fornecimento direto de hash. Cadastro, troca de senha e bootstrap continuam gerando hashes no servidor com custo 10 pelo encoder existente. O modelo persistente e os contratos HTTP permanecem iguais. A hidratação JPA de registros existentes segue pelo acesso aos campos. [BCryptPasswordEncoder do Spring Security 7.0.7](https://github.com/spring-projects/spring-security/blob/7.0.7/crypto/src/main/java/org/springframework/security/crypto/bcrypt/BCryptPasswordEncoder.java)

## RED

O teste registrado em `ContaAcessoCustoBcryptTest.java` foi escrito e copiado para `red-test.java` antes da alteração do domínio.

```powershell
.\mvnw.cmd "-Dtest=ContaAcessoCustoBcryptTest" test
```

`red.log` registra quatro execuções, duas falhas de asserção, zero erros e zero ignorados. Os dois limites válidos passaram. Para 03 e 32, o JUnit informou `Expected java.lang.IllegalArgumentException to be thrown, but nothing was thrown.` O processo retornou código 1. O teste compilou e chegou a `alterarSenha`.

A auditoria reproduziu as duas falhas de asserção e o código de saída 1 em uma cópia temporária da Aula 14 publicada. A reprodução comprova a diferença de comportamento entre as versões. A ordem histórica exata das edições continua sustentada pelos artefatos locais do RED, GREEN e REFACTOR.

## GREEN

O teste permaneceu igual. O domínio passou a ler o custo após validar a estrutura existente, rejeitando valores abaixo de 4 e acima de 31 antes de atribuir o novo hash. O diff local está em `green.patch`.

A repetição do comando isolado aprovou quatro casos, sem falhas, erros ou ignorados. `.\mvnw.cmd clean verify` aprovou 395 casos, todos sem falhas, erros ou ignorados, em 1 min 40 s. Neste passo o JaCoCo ainda estava em modo relatório. Os registros são `green-isolado.log` e `green.log`.

## REFACTOR

Uma expressão pré-compilada passou a validar o formato inteiro e a faixa em uma decisão. A mudança elimina posições fixas, conversão numérica, compilação repetida da expressão e duplicação de rejeição. As versões BCrypt aceitas, o conteúdo armazenado e a exceção continuam iguais ao GREEN. O diff local está em `refactor.patch`.

`.\mvnw.cmd clean verify` aprovou novamente 395 casos, sem falhas, erros ou ignorados, com o portão ativo. `refactor.log` registra 1 min 44 s e `BUILD SUCCESS`. Os artefatos locais das três etapas estão descritos nas [evidências](aula-15-evidencias.md).
