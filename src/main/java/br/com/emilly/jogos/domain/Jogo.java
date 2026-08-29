package br.com.emilly.jogos.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Jogo {

    private Long id;
    private String codigo;
    private String nome;
    private Integer unidadesDisponiveis;
    private BigDecimal preco;
    private LocalDate dataCadastro;
    private Status status;
    private GeneroJogo genero;

    public Jogo(
            Long id,
            String codigo,
            String nome,
            Integer unidadesDisponiveis,
            BigDecimal preco,
            LocalDate dataCadastro,
            Status status,
            GeneroJogo genero
    ) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Código do jogo é obrigatório");
        }

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do jogo é obrigatório");
        }

        if (unidadesDisponiveis == null || unidadesDisponiveis < 0) {
            throw new IllegalArgumentException("Unidades disponíveis devem ser zero ou maior");
        }

        if (preco == null || preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço deve ser zero ou maior");
        }

        if (dataCadastro == null) {
            throw new IllegalArgumentException("Data de cadastro é obrigatória");
        }

        if (status == null) {
            throw new IllegalArgumentException("Status é obrigatório");
        }

        if (genero == null) {
            throw new IllegalArgumentException("Gênero do jogo é obrigatório");
        }


        this.id = id;
        this.codigo = codigo;
        this.nome = nome;
        this.unidadesDisponiveis = unidadesDisponiveis;
        this.preco = preco;
        this.dataCadastro = dataCadastro;
        this.status = status;
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

    public Integer getUnidadesDisponiveis() {
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

    public void alterarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do jogo é obrigatório");
        }

        this.nome = nome;
    }

    public void alterarPreco(BigDecimal preco) {
        if (preco == null || preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço deve ser zero ou maior");
        }

        this.preco = preco;
    }

    public void alterarUnidadesDisponiveis(Integer unidadesDisponiveis) {
        if (unidadesDisponiveis == null || unidadesDisponiveis < 0) {
            throw new IllegalArgumentException("Unidades disponíveis devem ser zero ou maior");
        }

        this.unidadesDisponiveis = unidadesDisponiveis;
    }

    public void alterarGenero(GeneroJogo genero) {
        if (genero == null) {
            throw new IllegalArgumentException("Gênero do jogo é obrigatório");
        }

        this.genero = genero;
    }

    public void ativar() {
        this.status = Status.ATIVO;
    }

    public void inativar() {
        this.status = Status.INATIVO;
    }
}
