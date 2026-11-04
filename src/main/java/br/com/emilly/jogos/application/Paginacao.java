package br.com.emilly.jogos.application;

import org.springframework.data.domain.Pageable;
import java.util.Set;

public final class Paginacao {
    private Paginacao() {}
    public static void validar(Pageable pageable, Set<String> permitidos) {
        if (pageable.isUnpaged() || pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Tamanho da página deve estar entre 1 e 100");
        }
        pageable.getSort().forEach(order -> {
            if (!permitidos.contains(order.getProperty())) {
                throw new IllegalArgumentException("Campo de ordenação inválido: " + order.getProperty());
            }
        });
    }
}
