package br.com.emilly.jogos.api.validation;

import br.com.emilly.jogos.domain.DocumentoFiscal;
import jakarta.validation.*;

public class CnpjValidator implements ConstraintValidator<CnpjValido, String> {
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value == null || DocumentoFiscal.cnpjValido(value);
  }
}
