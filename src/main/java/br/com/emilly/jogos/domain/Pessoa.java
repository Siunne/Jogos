package br.com.emilly.jogos.domain;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "pessoa")
public class Pessoa {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 150)
  private String nome;

  @Column(nullable = false, length = 180)
  private String email;

  @Column(nullable = false, length = 11, unique = true)
  private String cpf;

  @Column(name = "data_cadastro", nullable = false)
  private LocalDate dataCadastro;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status;

  protected Pessoa() {}

  public Pessoa(String nome, String email, String cpf) {
    alterarDados(nome, email, cpf);
    dataCadastro = LocalDate.now();
    status = Status.ATIVO;
  }

  public void alterarDados(String nome, String email, String cpf) {
    String novoNome = RegrasComerciais.texto(nome, 150, "Nome");
    String novoEmail = RegrasComerciais.texto(email, 180, "E-mail").toLowerCase(Locale.ROOT);
    novoEmail = RegrasComerciais.texto(novoEmail, 180, "E-mail");
    if (!novoEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
      throw new IllegalArgumentException("E-mail inválido");
    if (!DocumentoFiscal.cpfValido(cpf)) throw new IllegalArgumentException("CPF inválido");
    this.nome = novoNome;
    this.email = novoEmail;
    this.cpf = cpf;
  }

  public void ativar() {
    status = Status.ATIVO;
  }

  public void inativar() {
    status = Status.INATIVO;
  }

  public Long getId() {
    return id;
  }

  public String getNome() {
    return nome;
  }

  public String getEmail() {
    return email;
  }

  public String getCpf() {
    return cpf;
  }

  public LocalDate getDataCadastro() {
    return dataCadastro;
  }

  public Status getStatus() {
    return status;
  }
}
