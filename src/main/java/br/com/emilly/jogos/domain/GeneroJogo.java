package br.com.emilly.jogos.domain;

public class GeneroJogo {


    private Long id;
    private String nome;
    private Status status;

    public GeneroJogo(Long id, String nome, Status status){

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do gênero é obrigatório");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status é obrigatório");
        }

        this.id = id;
        this.nome = nome;
        this.status = status;
    }
    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Status getStatus() {
        return status;
    }

    public void alterarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do gênero é obrigatório");
        }

        this.nome = nome;
    }

    public void ativar() {
        this.status = Status.ATIVO;
    }

    public void inativar() {
        this.status = Status.INATIVO;
    }
}