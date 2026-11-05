package br.com.emilly.jogos.domain;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "colaborador")
public class Colaborador {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "pessoa_id", nullable = false, unique = true)
  private Pessoa pessoa;

  @Column(nullable = false, length = 30, unique = true)
  private String matricula;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private FuncaoColaborador funcao;

  @Column(name = "data_admissao", nullable = false)
  private LocalDate dataAdmissao;

  protected Colaborador() {}

  public Colaborador(
      Pessoa pessoa, String matricula, FuncaoColaborador funcao, LocalDate dataAdmissao) {
    RegrasComerciais.ativa(pessoa);
    if (dataAdmissao == null) throw new IllegalArgumentException("Data de admissão é obrigatória");
    this.pessoa = pessoa;
    this.dataAdmissao = dataAdmissao;
    alterarDados(matricula, funcao);
  }

  public void alterarDados(String matricula, FuncaoColaborador funcao) {
    String nova = normalizarMatricula(matricula);
    if (funcao == null) throw new IllegalArgumentException("Função é obrigatória");
    this.matricula = nova;
    this.funcao = funcao;
  }

  public void validarVendedor() {
    RegrasComerciais.ativa(pessoa);
    if (funcao != FuncaoColaborador.VENDEDOR && funcao != FuncaoColaborador.GERENTE)
      throw new IllegalArgumentException("Colaborador deve ser VENDEDOR ou GERENTE");
  }

  public static String normalizarMatricula(String matricula) {
    return RegrasComerciais.texto(matricula, 30, "Matrícula");
  }

  public Long getId() {
    return id;
  }

  public Pessoa getPessoa() {
    return pessoa;
  }

  public String getMatricula() {
    return matricula;
  }

  public FuncaoColaborador getFuncao() {
    return funcao;
  }

  public LocalDate getDataAdmissao() {
    return dataAdmissao;
  }
}
