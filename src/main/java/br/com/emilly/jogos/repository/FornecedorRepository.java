package br.com.emilly.jogos.repository;

import br.com.emilly.jogos.domain.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FornecedorRepository
        extends JpaRepository<Fornecedor, Long> {

    boolean existsByCnpj(String cnpj);
}