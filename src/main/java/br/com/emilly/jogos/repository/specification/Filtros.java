package br.com.emilly.jogos.repository.specification;

import br.com.emilly.jogos.domain.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.Locale;

public final class Filtros {
    private Filtros() {}
    public static <T> Specification<T> nomeEStatus(String nome, Status status) {
        Specification<T> filtros = (root, query, cb) -> cb.conjunction();
        if (nome != null && !nome.isBlank()) {
            String trecho = "%" + nome.trim().toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
            filtros = filtros.and((root, query, cb) -> cb.like(cb.lower(root.get("nome")), trecho, '\\'));
        }
        if (status != null) filtros = filtros.and((root, query, cb) -> cb.equal(root.get("status"), status));
        return filtros;
    }
    public static Specification<Jogo> jogos(String nome, Status status, Long generoId,
                                           Long fornecedorId, Boolean abaixoEstoqueMinimo) {
        Specification<Jogo> filtros = nomeEStatus(nome, status);
        if (generoId != null) filtros = filtros.and((root, query, cb) -> cb.equal(root.get("genero").get("id"), generoId));
        if (fornecedorId != null) filtros = filtros.and((root, query, cb) -> cb.equal(root.get("fornecedor").get("id"), fornecedorId));
        if (Boolean.TRUE.equals(abaixoEstoqueMinimo)) {
            filtros = filtros.and((root, query, cb) -> cb.lessThan(root.get("unidadesDisponiveis"), root.get("estoqueMinimo")));
        }
        return filtros;
    }
}
