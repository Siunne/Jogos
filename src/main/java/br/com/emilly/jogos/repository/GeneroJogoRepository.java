package br.com.emilly.jogos.repository;

import br.com.emilly.jogos.domain.GeneroJogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GeneroJogoRepository
        extends JpaRepository<GeneroJogo, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    Optional<GeneroJogo> findByNomeIgnoreCase(String nome);
}