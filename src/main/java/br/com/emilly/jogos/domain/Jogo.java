package br.com.emilly.jogos.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(
        name = "jogo",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_jogo_codigo",
                        columnNames = "codigo")
        }
)
public class Jogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(
            name = "unidades_disponiveis",
            nullable = false,
            precision = 18,
            scale = 3)
    private BigDecimal unidadesDisponiveis;

    @Column(
            nullable = false,
            precision = 18,
            scale = 2)
    private BigDecimal preco;

    @Column(
            name = "estoque_minimo",
            nullable = false,
            precision = 18,
            scale = 3)
    private BigDecimal estoqueMinimo;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDate dataCadastro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "genero_jogo_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_jogo_genero_jogo"))
    private GeneroJogo genero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "fornecedor_id",
            foreignKey = @ForeignKey(
                    name = "fk_jogo_fornecedor"))
    private Fornecedor fornecedor;

    protected Jogo() {
    }

    public Jogo(
            String codigo,
            String nome,
            BigDecimal unidadesDisponiveis,
            BigDecimal preco,
            LocalDate dataCadastro) {

        this(
                codigo,
                nome,
                unidadesDisponiveis,
                preco,
                BigDecimal.ZERO,
                dataCadastro);
    }

    public Jogo(
            String codigo,
            String nome,
            BigDecimal unidadesDisponiveis,
            BigDecimal preco,
            BigDecimal estoqueMinimo,
            LocalDate dataCadastro) {

        this.codigo = validarTextoObrigatorio(
                codigo,
                "Código do jogo é obrigatório");

        this.nome = validarTextoObrigatorio(
                nome,
                "Nome do jogo é obrigatório");

        this.unidadesDisponiveis = validarNaoNegativo(
                unidadesDisponiveis,
                "Unidades disponíveis não podem ser negativas");

        this.preco = validarNaoNegativo(
                preco,
                "Preço não pode ser negativo");

        this.estoqueMinimo = validarNaoNegativo(
                estoqueMinimo,
                "Estoque mínimo não pode ser negativo");

        this.dataCadastro = Objects.requireNonNull(
                dataCadastro,
                "Data de cadastro é obrigatória");

        this.status = Status.ATIVO;
    }

    public BigDecimal calcularValorTotal() {
        return unidadesDisponiveis
                .multiply(preco)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void receberUnidades(BigDecimal quantidade) {
        validarPositivo(
                quantidade,
                "Quantidade recebida deve ser maior que zero");

        this.unidadesDisponiveis =
                unidadesDisponiveis.add(quantidade);
    }

    public void retirarUnidades(BigDecimal quantidade) {
        validarPositivo(
                quantidade,
                "Quantidade retirada deve ser maior que zero");

        if (unidadesDisponiveis.compareTo(quantidade) < 0) {
            throw new IllegalArgumentException(
                    "Unidades disponíveis insuficientes");
        }

        this.unidadesDisponiveis =
                unidadesDisponiveis.subtract(quantidade);
    }

    public void alterarNome(String novoNome) {
        this.nome = validarTextoObrigatorio(
                novoNome,
                "Nome do jogo é obrigatório");
    }

    public void alterarPreco(BigDecimal novoPreco) {
        this.preco = validarNaoNegativo(
                novoPreco,
                "Preço não pode ser negativo");
    }

    public void ativar() {
        this.status = Status.ATIVO;
    }

    public void inativar() {
        this.status = Status.INATIVO;
    }

    void associarAo(GeneroJogo genero) {
        Objects.requireNonNull(
                genero,
                "Gênero do jogo é obrigatório");

        if (this.genero != null && this.genero != genero) {
            throw new IllegalStateException(
                    "Jogo já pertence a outro gênero");
        }

        this.genero = genero;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getUnidadesDisponiveis() {
        return unidadesDisponiveis;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public BigDecimal getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public Status getStatus() {
        return status;
    }

    public GeneroJogo getGenero() {
        return genero;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    private static String validarTextoObrigatorio(
            String texto,
            String mensagem) {

        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }

        return texto.trim();
    }

    private static BigDecimal validarNaoNegativo(
            BigDecimal valor,
            String mensagem) {

        Objects.requireNonNull(valor, mensagem);

        if (valor.signum() < 0) {
            throw new IllegalArgumentException(mensagem);
        }

        return valor;
    }

    private static void validarPositivo(
            BigDecimal valor,
            String mensagem) {

        Objects.requireNonNull(valor, mensagem);

        if (valor.signum() <= 0) {
            throw new IllegalArgumentException(mensagem);
        }
    }
}