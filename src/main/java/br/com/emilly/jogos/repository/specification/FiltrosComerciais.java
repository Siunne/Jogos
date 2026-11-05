package br.com.emilly.jogos.repository.specification;

import br.com.emilly.jogos.domain.*;
import java.time.*;
import java.util.*;
import org.springframework.data.jpa.domain.Specification;

public final class FiltrosComerciais {
  private FiltrosComerciais() {}

  public static <T> Specification<T> papeis(String nome, Status status, Long pessoaId) {
    Specification<T> f = (r, q, c) -> c.conjunction();
    if (nome != null && !nome.isBlank()) {
      String busca =
          "%"
              + nome.trim()
                  .toLowerCase(Locale.ROOT)
                  .replace("\\", "\\\\")
                  .replace("%", "\\%")
                  .replace("_", "\\_")
              + "%";
      f = f.and((r, q, c) -> c.like(c.lower(r.get("pessoa").get("nome")), busca, '\\'));
    }
    if (status != null) f = f.and((r, q, c) -> c.equal(r.get("pessoa").get("status"), status));
    if (pessoaId != null) f = f.and((r, q, c) -> c.equal(r.get("pessoa").get("id"), pessoaId));
    return f;
  }

  public static Specification<Venda> vendas(
      StatusVenda status, Long clienteId, Long vendedorId, LocalDate inicio, LocalDate fim) {
    if (inicio != null && fim != null && inicio.isAfter(fim))
      throw new IllegalArgumentException("Data inicial não pode ser posterior à final");
    if (LocalDate.MAX.equals(fim))
      throw new IllegalArgumentException("Data final fora do intervalo suportado");
    Specification<Venda> f = (r, q, c) -> c.conjunction();
    if (status != null) f = f.and((r, q, c) -> c.equal(r.get("status"), status));
    if (clienteId != null) f = f.and((r, q, c) -> c.equal(r.get("cliente").get("id"), clienteId));
    if (vendedorId != null)
      f = f.and((r, q, c) -> c.equal(r.get("vendedor").get("id"), vendedorId));
    if (inicio != null)
      f = f.and((r, q, c) -> c.greaterThanOrEqualTo(r.get("dataVenda"), inicio.atStartOfDay()));
    if (fim != null)
      f = f.and((r, q, c) -> c.lessThan(r.get("dataVenda"), fim.plusDays(1).atStartOfDay()));
    return f;
  }
}
