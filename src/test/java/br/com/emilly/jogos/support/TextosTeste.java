package br.com.emilly.jogos.support;

import java.util.*;

public final class TextosTeste {
  private TextosTeste() {}

  public static List<String> invalidos() {
    Set<String> values = new LinkedHashSet<>();
    values.add(null);
    values.add("");
    for (int i = 0; i <= 32; i++) values.add(String.valueOf((char) i));
    values.addAll(List.of("\t\n", "\u2003", "\u1680", "\u3000", "\u0000\u2003\u001b"));
    return Collections.unmodifiableList(new ArrayList<>(values));
  }
}
