package br.com.emilly.jogos.api.validation;

import jakarta.validation.*;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SenhaValidator.class)
public @interface SenhaValida {
  String message() default
      "Senha deve possuir no mínimo 12 caracteres, no máximo 72 bytes UTF-8 e não conter U+0000";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
