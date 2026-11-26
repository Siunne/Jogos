package br.com.emilly.jogos.dto;

import br.com.emilly.jogos.domain.StatusContaAcesso;
import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.JsonNode;

public record StatusContaRequest(
    @Schema(implementation = StatusContaAcesso.class, requiredMode = Schema.RequiredMode.REQUIRED)
    JsonNode status) {
  public StatusContaAcesso valor() {
    if (status == null || !status.isString()) throw new IllegalArgumentException("Status inválido");
    try {
      return StatusContaAcesso.valueOf(status.asString());
    } catch (IllegalArgumentException invalid) {
      throw new IllegalArgumentException("Status inválido");
    }
  }
}
