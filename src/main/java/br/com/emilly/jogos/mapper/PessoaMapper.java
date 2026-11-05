package br.com.emilly.jogos.mapper;

import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.dto.*;
import org.springframework.stereotype.Component;

@Component
public class PessoaMapper {
  public PessoaResponse toResponse(Pessoa p) {
    return new PessoaResponse(
        p.getId(), p.getNome(), p.getEmail(), p.getCpf(), p.getDataCadastro(), p.getStatus());
  }
}
