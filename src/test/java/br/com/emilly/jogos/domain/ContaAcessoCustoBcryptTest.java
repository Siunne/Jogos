package br.com.emilly.jogos.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ContaAcessoCustoBcryptTest {
  private static final String HASH_ANTERIOR = "{bcrypt}$2a$10$" + "A".repeat(53);

  @ParameterizedTest
  @ValueSource(strings = {"03", "32"})
  void recusaCustoForaDaFaixaSemTrocarHash(String custo) {
    var conta = conta();
    var novoHash = "{bcrypt}$2b$" + custo + "$" + "B".repeat(53);

    assertThrows(IllegalArgumentException.class, () -> conta.alterarSenha(novoHash));
    assertEquals(HASH_ANTERIOR, conta.getSenhaHash());
  }

  @ParameterizedTest
  @ValueSource(strings = {"04", "31"})
  void aceitaOsLimitesDeCustoDoBcrypt(String custo) {
    var conta = conta();
    var novoHash = "{bcrypt}$2b$" + custo + "$" + "B".repeat(53);

    conta.alterarSenha(novoHash);

    assertEquals(novoHash, conta.getSenhaHash());
  }

  private static ContaAcesso conta() {
    return new ContaAcesso(
        new Pessoa("Pessoa Exemplo", "pessoa@example.com", "52998224725"),
        "loja15", HASH_ANTERIOR);
  }
}
