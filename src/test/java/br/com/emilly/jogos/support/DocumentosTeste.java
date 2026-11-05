package br.com.emilly.jogos.support;

import br.com.emilly.jogos.domain.DocumentoFiscal;
import java.util.concurrent.ThreadLocalRandom;

public final class DocumentosTeste {
  private DocumentosTeste() {}

  public static String cpf() {
    for (; ; ) {
      String b = String.format("%09d", ThreadLocalRandom.current().nextLong(1000000000L));
      String s = b;
      for (int n = 0; n < 2; n++) {
        int sum = 0;
        for (int i = 0; i < s.length(); i++) sum += (s.charAt(i) - '0') * (s.length() + 1 - i);
        int r = sum % 11;
        s += (r < 2 ? 0 : 11 - r);
      }
      if (DocumentoFiscal.cpfValido(s)) return s;
    }
  }

  public static String cnpj() {
    for (; ; ) {
      String s = String.format("%012d", ThreadLocalRandom.current().nextLong(1000000000000L));
      for (int n = 0; n < 2; n++) {
        int sum = 0, w = s.length() - 7;
        for (char c : s.toCharArray()) {
          sum += (c - '0') * w--;
          if (w == 1) w = 9;
        }
        int r = sum % 11;
        s += (r < 2 ? 0 : 11 - r);
      }
      if (DocumentoFiscal.cnpjValido(s)) return s;
    }
  }
}
