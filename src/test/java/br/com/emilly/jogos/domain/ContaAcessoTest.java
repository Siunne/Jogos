package br.com.emilly.jogos.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ContaAcessoTest {
  // Fixtures sintáticas. A verificação criptográfica pertence à suíte de segurança.
  private static final String HASH = "{bcrypt}$2a$10$" + "A".repeat(53);
  private static final String OUTRO_HASH = "{bcrypt}$2b$12$" + "B".repeat(53);

  @Test
  void criaContaAtivaComLoginNormalizadoEPerfilOperador() {
    var pessoa = pessoa();
    var conta = new ContaAcesso(pessoa, "  LOJA_15.Exemplo  ", HASH);

    assertAll(
        () -> assertEquals("loja_15.exemplo", conta.getLogin()),
        () -> assertSame(pessoa, conta.getPessoa()),
        () -> assertEquals(HASH, conta.getSenhaHash()),
        () -> assertEquals(StatusContaAcesso.ATIVA, conta.getStatus()),
        () -> assertEquals(Set.of(PerfilAcesso.OPERADOR), conta.getPerfis()),
        () -> assertTrue(conta.podeAutenticar()));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "   ", "loja 15", "\tloja15", "loja15\t", "ab", "lojà"})
  void rejeitaLoginForaDaParticaoAceita(String login) {
    assertThrows(IllegalArgumentException.class, () -> new ContaAcesso(pessoa(), login, HASH));
  }

  @Test
  void aceitaLimitesDeComprimentoAposNormalizacao() {
    var pessoa = pessoa();
    assertAll(
        () -> assertEquals("abc", new ContaAcesso(pessoa, " AbC ", HASH).getLogin()),
        () -> assertEquals("a".repeat(80), new ContaAcesso(pessoa, "A".repeat(80), HASH).getLogin()),
        () -> assertThrows(IllegalArgumentException.class,
            () -> new ContaAcesso(pessoa, "A".repeat(81), HASH)));
  }

  @Test
  void substituiHashValidoPreservandoOValorRecebido() {
    var conta = conta();

    conta.alterarSenha(OUTRO_HASH);

    assertEquals(OUTRO_HASH, conta.getSenhaHash());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "texto-sem-hash", "{bcrypt}$2a$10$curto"})
  void recusaHashInvalidoSemPerderOAnterior(String hash) {
    var conta = conta();

    assertThrows(IllegalArgumentException.class, () -> conta.alterarSenha(hash));
    assertEquals(HASH, conta.getSenhaHash());
  }

  @Test
  void concedeESubstituiPerfisSemAceitarAlteracaoExterna() {
    var conta = conta();
    conta.concederPerfil(PerfilAcesso.ADMINISTRADOR);
    assertEquals(Set.of(PerfilAcesso.OPERADOR, PerfilAcesso.ADMINISTRADOR), conta.getPerfis());

    var novos = new HashSet<>(Set.of(PerfilAcesso.ADMINISTRADOR));
    conta.substituirPerfis(novos);
    novos.clear();

    assertEquals(Set.of(PerfilAcesso.ADMINISTRADOR), conta.getPerfis());
    assertThrows(UnsupportedOperationException.class,
        () -> conta.getPerfis().add(PerfilAcesso.OPERADOR));
  }

  @Test
  void rejeitaPerfisInvalidosPreservandoOsAtuais() {
    var conta = conta();
    var comNulo = new HashSet<>(Arrays.asList(PerfilAcesso.ADMINISTRADOR, null));

    assertAll(
        () -> assertThrows(IllegalArgumentException.class, () -> conta.substituirPerfis(Set.of())),
        () -> assertThrows(IllegalArgumentException.class, () -> conta.substituirPerfis(null)),
        () -> assertThrows(IllegalArgumentException.class, () -> conta.substituirPerfis(comNulo)),
        () -> assertThrows(IllegalArgumentException.class, () -> conta.concederPerfil(null)));
    assertEquals(Set.of(PerfilAcesso.OPERADOR), conta.getPerfis());
  }

  @Test
  void contaBloqueadaSoVoltaAAutenticarQuandoAmbosEstaoAtivos() {
    var pessoa = pessoa();
    var conta = new ContaAcesso(pessoa, "loja15", HASH);

    conta.bloquear();
    assertEquals(StatusContaAcesso.BLOQUEADA, conta.getStatus());
    assertFalse(conta.podeAutenticar());

    pessoa.inativar();
    conta.ativar();
    assertEquals(StatusContaAcesso.ATIVA, conta.getStatus());
    assertFalse(conta.podeAutenticar());

    pessoa.ativar();
    assertTrue(conta.podeAutenticar());
  }

  @Test
  void recusaCriarContaParaPessoaInativa() {
    var pessoa = pessoa();
    pessoa.inativar();

    assertThrows(IllegalArgumentException.class, () -> new ContaAcesso(pessoa, "loja15", HASH));
  }

  private static ContaAcesso conta() {
    return new ContaAcesso(pessoa(), "loja15", HASH);
  }

  private static Pessoa pessoa() {
    return new Pessoa("Pessoa Exemplo", "pessoa@example.com", "52998224725");
  }
}
