package br.com.emilly.jogos.repository;

import br.com.emilly.jogos.domain.ContaAcesso;
import br.com.emilly.jogos.domain.PerfilAcesso;
import br.com.emilly.jogos.domain.Status;
import br.com.emilly.jogos.domain.StatusContaAcesso;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

public interface ContaAcessoRepository extends JpaRepository<ContaAcesso, Long> {
  Optional<ContaAcesso> findByPessoaId(Long pessoaId);

  @Query("""
      select count(c) from ContaAcesso c
      where c.status = :conta and c.pessoa.status = :pessoa and :perfil member of c.perfis
      """)
  long contarAdministradores(StatusContaAcesso conta, Status pessoa, PerfilAcesso perfil);

  boolean existsByPessoaId(Long id);

  @Query(
      value = "SELECT EXISTS(SELECT 1 FROM conta_acesso WHERE LOWER(BTRIM(login))=:login)",
      nativeQuery = true)
  boolean existeLogin(String login);

  @EntityGraph(attributePaths = {"pessoa", "perfis"})
  @Query("select c from ContaAcesso c where lower(trim(c.login)) = :login")
  Optional<ContaAcesso> buscarPorLoginNormalizado(String login);
}
