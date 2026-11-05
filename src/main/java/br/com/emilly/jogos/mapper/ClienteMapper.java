package br.com.emilly.jogos.mapper;

import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.dto.*;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {
  public ClienteResponse toResponse(Cliente c) {
    return new ClienteResponse(
        c.getId(),
        c.getPessoa().getId(),
        c.getPessoa().getNome(),
        c.getPessoa().getStatus(),
        c.getTelefone());
  }
}
