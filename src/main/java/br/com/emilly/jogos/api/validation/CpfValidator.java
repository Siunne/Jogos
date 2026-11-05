package br.com.emilly.jogos.api.validation;

import br.com.emilly.jogos.domain.DocumentoFiscal;
import jakarta.validation.*;

public class CpfValidator implements ConstraintValidator<CpfValido, String> {
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value == null || DocumentoFiscal.cpfValido(value);
  }
}
