package br.com.emilly.jogos.repository;

import br.com.emilly.jogos.domain.Jogo;
import br.com.emilly.jogos.domain.Status;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface JogoRepository extends JpaRepository<Jogo, Long>, JpaSpecificationExecutor<Jogo> {

    boolean existsByGeneroId(Long generoId);

    @Override
    @EntityGraph(attributePaths = {"genero", "fornecedor"})
    Page<Jogo> findAll(Specification<Jogo> specification, Pageable pageable);

    Optional<Jogo> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Jogo> findByGeneroId(Long generoId);

    List<Jogo> findByStatus(Status status);

    @Override
    @EntityGraph(attributePaths = {"genero", "fornecedor"})
    Optional<Jogo> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"genero", "fornecedor"})
    List<Jogo> findAll();
}
