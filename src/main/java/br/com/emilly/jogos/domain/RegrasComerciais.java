package br.com.emilly.jogos.domain;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import java.util.*;

final class RegrasComerciais {
  private RegrasComerciais() {}

  static String texto(String valor, int max, String campo) {
    if (valor == null || valor.isBlank() || valor.length() > max || valor.trim().isBlank())
      throw new IllegalArgumentException(campo + " obrigatório, até " + max + " caracteres");
    return valor.trim();
  }

  static void ativa(Pessoa pessoa) {
    if (pessoa == null) throw new IllegalArgumentException("Pessoa é obrigatória");
    if (pessoa.getStatus() != Status.ATIVO)
      throw new IllegalArgumentException("Pessoa deve estar ativa");
  }

  static BigDecimal decimal(
      BigDecimal valor, int inteiros, int casas, boolean positivo, String campo) {
    if (valor == null
        || valor.signum() < 0
        || (positivo && valor.signum() == 0)
        || Math.max(0, valor.scale()) > casas
        || valor.precision() - valor.scale() > inteiros)
      throw new IllegalArgumentException(campo + " inválido");
    return valor;
  }
}
