package br.com.emilly.jogos.domain;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import java.util.*;

public final class DocumentoFiscal {
  private DocumentoFiscal() {}

  public static boolean cpfValido(String s) {
    if (!formato(s, 11)) return false;
    String b = s.substring(0, 9);
    for (int n = 0; n < 2; n++) {
      int soma = 0;
      for (int i = 0; i < b.length(); i++) soma += (b.charAt(i) - '0') * (b.length() + 1 - i);
      b += digito(soma);
    }
    return s.equals(b);
  }

  public static boolean cnpjValido(String s) {
    if (!formato(s, 14)) return false;
    String b = s.substring(0, 12);
    for (int n = 0; n < 2; n++) {
      int soma = 0, peso = b.length() - 7;
      for (char c : b.toCharArray()) {
        soma += (c - '0') * peso--;
        if (peso == 1) peso = 9;
      }
      b += digito(soma);
    }
    return s.equals(b);
  }

  private static boolean formato(String s, int tamanho) {
    return s != null && s.matches("[0-9]{" + tamanho + "}") && s.chars().distinct().count() > 1;
  }

  private static int digito(int soma) {
    int r = soma % 11;
    return r < 2 ? 0 : 11 - r;
  }
}
