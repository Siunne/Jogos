package br.com.emilly.jogos.repository;

import br.com.emilly.jogos.domain.Jogo;
import br.com.emilly.jogos.domain.Status;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JogoRepository extends JpaRepository<Jogo, Long> {

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