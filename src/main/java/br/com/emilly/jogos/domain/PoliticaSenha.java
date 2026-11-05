package br.com.emilly.jogos.domain;

import java.nio.charset.StandardCharsets;

public final class PoliticaSenha {
  private PoliticaSenha() {}

  public static boolean valida(String senha) {
    return senha != null
        && senha.indexOf('\0') < 0
        && !senha.isBlank()
        && senha.codePointCount(0, senha.length()) >= 12
        && senha.getBytes(StandardCharsets.UTF_8).length <= 72
        && StandardCharsets.UTF_8.newEncoder().canEncode(senha);
  }

  public static void validar(String senha) {
    if (!valida(senha))
      throw new IllegalArgumentException(
          "Senha deve possuir no mínimo 12 caracteres, no máximo 72 bytes UTF-8 e não conter"
              + " U+0000");
  }
}
