package br.com.emilly.jogos.api.validation;

import br.com.emilly.jogos.domain.PoliticaSenha;
import jakarta.validation.*;

public class SenhaValidator implements ConstraintValidator<SenhaValida, String> {
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value == null || PoliticaSenha.valida(value);
  }
}
