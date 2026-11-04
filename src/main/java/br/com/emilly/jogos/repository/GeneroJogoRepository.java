package br.com.emilly.jogos.repository;

import br.com.emilly.jogos.domain.GeneroJogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GeneroJogoRepository
        extends JpaRepository<GeneroJogo, Long>, JpaSpecificationExecutor<GeneroJogo> {

    @Query("""
            select count(g) > 0 from GeneroJogo g
            where lower(trim(g.nome)) = lower(trim(:nome))
              and (:id is null or g.id <> :id)
            """)
    boolean existeNomeNormalizado(@Param("nome") String nome, @Param("id") Long id);

    boolean existsByNomeIgnoreCase(String nome);

    Optional<GeneroJogo> findByNomeIgnoreCase(String nome);
}
