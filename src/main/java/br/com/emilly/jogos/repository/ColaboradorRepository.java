package br.com.emilly.jogos.repository;

import br.com.emilly.jogos.domain.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;

public interface ColaboradorRepository
    extends JpaRepository<Colaborador, Long>, JpaSpecificationExecutor<Colaborador> {
  boolean existsByPessoaId(Long pessoaId);

  boolean existsByMatricula(String matricula);

  boolean existsByMatriculaAndIdNot(String matricula, Long id);

  @EntityGraph(attributePaths = "pessoa")
  Optional<Colaborador> findOneById(Long id);

  @Override
  @EntityGraph(attributePaths = "pessoa")
  Page<Colaborador> findAll(Specification<Colaborador> filtro, Pageable pageable);
}
