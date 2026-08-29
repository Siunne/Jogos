package br.com.emilly.jogos.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public class Jogo {

    private final String codigo;
    private String nome;
    private BigDecimal unidadesDisponiveis;
    private BigDecimal preco;
    private final LocalDate dataCadastro;
    private Status status;
    private GeneroJogo genero;

    public Jogo(
            String codigo,
            String nome,
            BigDecimal unidadesDisponiveis,
            BigDecimal preco,
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

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public Status getStatus() {
        return status;
    }

    public GeneroJogo getGenero() {
        return genero;
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