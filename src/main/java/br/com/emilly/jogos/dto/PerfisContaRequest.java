package br.com.emilly.jogos.dto;

import br.com.emilly.jogos.domain.PerfilAcesso;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.EnumSet;
import java.util.Set;
import tools.jackson.databind.JsonNode;

public record PerfisContaRequest(
    @ArraySchema(minItems = 1, schema = @Schema(implementation = PerfilAcesso.class))
    JsonNode perfis) {
  public Set<PerfilAcesso> valores() {
    if (perfis == null || !perfis.isArray() || perfis.isEmpty())
      throw new IllegalArgumentException("Informe ao menos um perfil");
    var valores = EnumSet.noneOf(PerfilAcesso.class);
    for (var perfil : perfis) {
      if (!perfil.isString()) throw new IllegalArgumentException("Perfil inválido");
      try {
        valores.add(PerfilAcesso.valueOf(perfil.asString()));
      } catch (IllegalArgumentException invalid) {
        throw new IllegalArgumentException("Perfil inválido");
      }
    }
    return valores;
  }
}
