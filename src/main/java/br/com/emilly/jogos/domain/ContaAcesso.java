package br.com.emilly.jogos.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "conta_acesso")
public class ContaAcesso {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "pessoa_id", nullable = false, unique = true)
  private Pessoa pessoa;

  @Column(nullable = false, length = 80)
  private String login;

  @JsonIgnore
  @Column(name = "senha_hash", nullable = false, length = 255)
  private String senhaHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private StatusContaAcesso status;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "conta_acesso_perfil",
      joinColumns = @JoinColumn(name = "conta_acesso_id"))
  @Column(name = "perfil", nullable = false, length = 30)
  @Enumerated(EnumType.STRING)
  private Set<PerfilAcesso> perfis = new LinkedHashSet<>();

  protected ContaAcesso() {}

  public ContaAcesso(Pessoa pessoa, String login, String hash) {
    this.pessoa = Objects.requireNonNull(pessoa, "Pessoa obrigatória");
    if (pessoa.getStatus() != Status.ATIVO)
      throw new IllegalArgumentException("Pessoa deve estar ativa");
    this.login = normalizarLogin(login);
    alterarSenha(hash);
    status = StatusContaAcesso.ATIVA;
    perfis.add(PerfilAcesso.OPERADOR);
  }

  /** Equivale a LOWER(BTRIM(login)) no alfabeto ASCII permitido. Não usa trim Unicode. */
  public static String normalizarLogin(String valor) {
    if (valor == null) throw new IllegalArgumentException("Login inválido");
    int inicio = 0, fim = valor.length();
    while (inicio < fim && valor.charAt(inicio) == ' ') inicio++;
    while (fim > inicio && valor.charAt(fim - 1) == ' ') fim--;
    String login = valor.substring(inicio, fim);
    if (!login.matches("[A-Za-z0-9._-]{3,80}"))
      throw new IllegalArgumentException(
          "Login deve conter de 3 a 80 letras ASCII, números, ponto, hífen ou sublinhado");
    return login.toLowerCase(Locale.ROOT);
  }

  public void alterarSenha(String hash) {
    if (hash == null || !hash.matches("\\{bcrypt}\\$2[aby]\\$[0-9]{2}\\$[./A-Za-z0-9]{53}"))
      throw new IllegalArgumentException("Hash de senha inválido");
    senhaHash = hash;
  }

  public boolean podeAutenticar() {
    return status == StatusContaAcesso.ATIVA && pessoa.getStatus() == Status.ATIVO;
  }

  public void bloquear() {
    status = StatusContaAcesso.BLOQUEADA;
  }

  public void ativar() {
    status = StatusContaAcesso.ATIVA;
  }

  public Long getId() {
    return id;
  }

  public Pessoa getPessoa() {
    return pessoa;
  }

  public String getLogin() {
    return login;
  }

  @JsonIgnore
  public String getSenhaHash() {
    return senhaHash;
  }

  public StatusContaAcesso getStatus() {
    return status;
  }

  public Set<PerfilAcesso> getPerfis() {
    return Collections.unmodifiableSet(perfis);
  }

  @Override
  public String toString() {
    return "ContaAcesso[id=" + id + ", status=" + status + "]";
  }
}
