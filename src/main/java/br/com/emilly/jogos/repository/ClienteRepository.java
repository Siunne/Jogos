package br.com.emilly.jogos.repository;

import br.com.emilly.jogos.domain.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;

public interface ClienteRepository
    extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {
  boolean existsByPessoaId(Long pessoaId);

  @EntityGraph(attributePaths = "pessoa")
  Optional<Cliente> findOneById(Long id);

  @Override
  @EntityGraph(attributePaths = "pessoa")
  Page<Cliente> findAll(Specification<Cliente> filtro, Pageable pageable);
}
