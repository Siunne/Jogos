package br.com.emilly.jogos.application;

import br.com.emilly.jogos.domain.Jogo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Política comum de todas as alterações de saldo. O lock dura até commit/rollback. */
@Component
public class BloqueioEstoque {
  private final EntityManager em;

  public BloqueioEstoque(EntityManager em) {
    this.em = em;
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void adquirir(Jogo jogo) {
    if (em.getLockMode(jogo) == LockModeType.PESSIMISTIC_WRITE) return;
    // lock não descarta alterações locais; flush já sob lock preserva mudanças pendentes.
    // DynamicUpdate impede que alterações de cadastro reenviem um saldo antigo.
    em.lock(jogo, LockModeType.PESSIMISTIC_WRITE);
    em.flush();
    em.refresh(jogo, LockModeType.PESSIMISTIC_WRITE);
  }
}
