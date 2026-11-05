package br.com.emilly.jogos.support;

import br.com.emilly.jogos.domain.*;
import jakarta.persistence.EntityManager;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.transaction.support.TransactionTemplate;

public final class ComercialFixture {
  private ComercialFixture() {}

  public record Dados(
      Long pessoa, Long cliente, Long vendedor, Long jogo1, Long jogo2, Long genero) {}

  public static Dados criar(EntityManager em, TransactionTemplate tx) {
    return tx.execute(
        s -> {
          String token = UUID.randomUUID().toString();
          var pessoa = new Pessoa("Pessoa " + token, token + "@example.com", DocumentosTeste.cpf());
          em.persist(pessoa);
          var cliente = new Cliente(pessoa, null);
          em.persist(cliente);
          var vendedor =
              new Colaborador(
                  pessoa, token.substring(0, 30), FuncaoColaborador.VENDEDOR, LocalDate.now());
          em.persist(vendedor);
          var genero = new GeneroJogo("Comercial " + token);
          em.persist(genero);
          var a =
              Jogo.novoCadastro(
                  "A" + token,
                  "Reino " + token,
                  new BigDecimal("10.000"),
                  new BigDecimal("100.00"),
                  BigDecimal.ONE);
          var b =
              Jogo.novoCadastro(
                  "B" + token,
                  "Aventura " + token,
                  new BigDecimal("1.000"),
                  new BigDecimal("50.00"),
                  BigDecimal.ONE);
          genero.adicionarJogo(a);
          genero.adicionarJogo(b);
          em.persist(a);
          em.persist(b);
          em.flush();
          return new Dados(
              pessoa.getId(),
              cliente.getId(),
              vendedor.getId(),
              a.getId(),
              b.getId(),
              genero.getId());
        });
  }
}
