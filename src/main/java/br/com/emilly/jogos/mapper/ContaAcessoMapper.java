package br.com.emilly.jogos.mapper;

import br.com.emilly.jogos.domain.ContaAcesso;
import br.com.emilly.jogos.dto.ContaAcessoResponse;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ContaAcessoMapper {
  public ContaAcessoResponse toResponse(ContaAcesso c) {
    return new ContaAcessoResponse(
        c.getId(),
        c.getPessoa().getId(),
        c.getPessoa().getNome(),
        c.getLogin(),
        c.getStatus(),
        Set.copyOf(c.getPerfis()));
  }
}
