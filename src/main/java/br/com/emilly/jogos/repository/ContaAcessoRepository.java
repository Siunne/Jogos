package br.com.emilly.jogos.repository;

import br.com.emilly.jogos.domain.ContaAcesso;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

public interface ContaAcessoRepository extends JpaRepository<ContaAcesso, Long> {
  boolean existsByPessoaId(Long id);

  @Query(
      value = "SELECT EXISTS(SELECT 1 FROM conta_acesso WHERE LOWER(BTRIM(login))=:login)",
      nativeQuery = true)
  boolean existeLogin(String login);

  @EntityGraph(attributePaths = {"pessoa", "perfis"})
  @Query("select c from ContaAcesso c where lower(trim(c.login)) = :login")
  Optional<ContaAcesso> buscarPorLoginNormalizado(String login);
}
