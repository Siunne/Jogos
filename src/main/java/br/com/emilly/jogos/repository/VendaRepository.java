package br.com.emilly.jogos.repository;

import br.com.emilly.jogos.domain.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;

public interface VendaRepository
    extends JpaRepository<Venda, Long>, JpaSpecificationExecutor<Venda> {
  @EntityGraph(attributePaths = {"cliente.pessoa", "vendedor.pessoa", "itens.jogo"})
  Optional<Venda> findOneById(Long id);

  @Override
  @EntityGraph(attributePaths = {"cliente.pessoa", "vendedor.pessoa"})
  Page<Venda> findAll(Specification<Venda> filtro, Pageable pageable);
}
