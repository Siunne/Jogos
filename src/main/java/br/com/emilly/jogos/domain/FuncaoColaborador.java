package br.com.emilly.jogos.domain;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import java.util.*;

public enum FuncaoColaborador {
  VENDEDOR,
  ESTOQUISTA,
  GERENTE,
  ADMINISTRATIVO
}
