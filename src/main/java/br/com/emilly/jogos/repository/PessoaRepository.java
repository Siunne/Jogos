package br.com.emilly.jogos.repository;

import br.com.emilly.jogos.domain.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface PessoaRepository
    extends JpaRepository<Pessoa, Long>, JpaSpecificationExecutor<Pessoa> {
  boolean existsByCpf(String cpf);

  boolean existsByCpfAndIdNot(String cpf, Long id);

  boolean existsByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
