package br.com.emilly.jogos.domain;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "cliente")
public class Cliente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "pessoa_id", nullable = false, unique = true)
  private Pessoa pessoa;

  @Column(length = 30)
  private String telefone;

  protected Cliente() {}

  public Cliente(Pessoa pessoa, String telefone) {
    RegrasComerciais.ativa(pessoa);
    this.pessoa = pessoa;
    alterarDados(telefone);
  }

  public void alterarDados(String telefone) {
    if (telefone != null && telefone.length() > 30)
      throw new IllegalArgumentException("Telefone deve ter até 30 caracteres");
    String normalizado = telefone == null ? null : telefone.trim();
    this.telefone = normalizado == null || normalizado.isBlank() ? null : normalizado;
  }

  public Long getId() {
    return id;
  }

  public Pessoa getPessoa() {
    return pessoa;
  }

  public String getTelefone() {
    return telefone;
  }
}
