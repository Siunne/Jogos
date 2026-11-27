package br.com.emilly.jogos.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import br.com.emilly.jogos.domain.Jogo;
import br.com.emilly.jogos.exception.RecursoNaoEncontradoException;
import br.com.emilly.jogos.repository.FornecedorRepository;
import br.com.emilly.jogos.repository.GeneroJogoRepository;
import br.com.emilly.jogos.repository.JogoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JogoServiceUnitTest {
  @Mock JogoRepository jogos;
  @Mock GeneroJogoRepository generos;
  @Mock FornecedorRepository fornecedores;
  @Mock BloqueioEstoque bloqueio;
  @InjectMocks JogoService service;

  @Test
  void entradaDevolveJogoComSaldoAtualizado() {
    var jogo = jogoComCincoUnidades();
    when(jogos.findById(1L)).thenReturn(Optional.of(jogo));

    var atualizado = service.entrada(1L, new BigDecimal("2.125"));

    assertEquals(0, new BigDecimal("7.125").compareTo(atualizado.getUnidadesDisponiveis()));
  }

  @Test
  void saidaAdmiteQuantidadeIgualAoSaldo() {
    var jogo = jogoComCincoUnidades();
    when(jogos.findById(1L)).thenReturn(Optional.of(jogo));

    var atualizado = service.saida(1L, new BigDecimal("5"));

    assertEquals(0, atualizado.getUnidadesDisponiveis().signum());
  }

  @Test
  void saidaAcimaDoSaldoPreservaEstoque() {
    var jogo = jogoComCincoUnidades();
    when(jogos.findById(1L)).thenReturn(Optional.of(jogo));

    var erro = assertThrows(IllegalArgumentException.class,
        () -> service.saida(1L, new BigDecimal("5.001")));

    assertAll(
        () -> assertEquals("Unidades disponíveis insuficientes", erro.getMessage()),
        () -> assertEquals(0, new BigDecimal("5").compareTo(jogo.getUnidadesDisponiveis())));
  }

  @Test
  void cadastroComGeneroInexistenteNaoVinculaJogo() {
    var jogo = jogoComCincoUnidades();
    when(generos.findById(42L)).thenReturn(Optional.empty());

    assertThrows(RecursoNaoEncontradoException.class, () -> service.cadastrar(jogo, 42L));
    assertNull(jogo.getGenero());
  }

  private static Jogo jogoComCincoUnidades() {
    return new Jogo("JOGO-15", "Jogo de exemplo", new BigDecimal("5.000"),
        new BigDecimal("25.00"), LocalDate.of(2026, 1, 1));
  }
}
