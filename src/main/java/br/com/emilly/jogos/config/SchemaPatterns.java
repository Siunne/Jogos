package br.com.emilly.jogos.config;

/** Padrões de @NotBlank e de texto obrigatório após normalização trim. */
public final class SchemaPatterns {
  public static final String NAO_BRANCO =
      "[\\s\\S]*[^\\u0009-\\u000D\\u001C-\\u0020\\u1680\\u2000-\\u2006\\u2008-\\u200A\\u2028\\u2029\\u205F\\u3000][\\s\\S]*";

  /**
   * Rejeita exatamente trim* + whitespaceJava* + trim*. Controles internos não são
   * removidos por trim. (?![\s\S]) exige fim absoluto também no ECMAScript, ao contrário de $.
   * Limites de tamanho e demais validações permanecem nos schemas/DTOs correspondentes.
   */
  public static final String NORMALIZADO_NAO_BRANCO =
      "^(?![\\u0000-\\u0020]*[\\u0009-\\u000D\\u001C-\\u0020\\u1680\\u2000-\\u2006\\u2008-\\u200A\\u2028\\u2029\\u205F\\u3000]*[\\u0000-\\u0020]*(?![\\s\\S]))[\\s\\S]+(?![\\s\\S])";

  private SchemaPatterns() {}
}
