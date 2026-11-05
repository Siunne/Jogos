package br.com.emilly.jogos.api.validation;

import jakarta.validation.*;
import java.lang.annotation.*;

@Target({
  ElementType.FIELD,
  ElementType.PARAMETER,
  ElementType.RECORD_COMPONENT,
  ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CnpjValidator.class)
public @interface CnpjValido {
  String message() default "CNPJ inválido";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
