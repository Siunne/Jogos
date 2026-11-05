package br.com.emilly.jogos.mapper;

import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.dto.*;
import org.springframework.stereotype.Component;

@Component
public class ColaboradorMapper {
  public ColaboradorResponse toResponse(Colaborador c) {
    return new ColaboradorResponse(
        c.getId(),
        c.getPessoa().getId(),
        c.getPessoa().getNome(),
        c.getPessoa().getStatus(),
        c.getMatricula(),
        c.getFuncao(),
        c.getDataAdmissao());
  }
}
