package br.com.emilly.jogos.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GeneroJogo {

    private final String nome;
    private Status status;
    private final List<Jogo> jogos = new ArrayList<>();

    public GeneroJogo(String nome) {
        this.nome = validarTextoObrigatorio(
                nome,
                "Nome do gênero é obrigatório");
        this.status = Status.ATIVO;
    }

    public void adicionarJogo(Jogo jogo) {
        Objects.requireNonNull(jogo, "Jogo é obrigatório");

        boolean codigoJaUtilizado = jogos.stream()
                .anyMatch(item -> item != jogo
                        && item.getCodigo().equals(jogo.getCodigo()));

        if (codigoJaUtilizado) {
            throw new IllegalArgumentException(
                    "Código do jogo já utilizado no gênero");
        }

        jogo.associarAo(this);

        if (!jogos.contains(jogo)) {
            jogos.add(jogo);
        }
    }

    public void ativar() {
        this.status = Status.ATIVO;
    }

    public void inativar() {
        this.status = Status.INATIVO;
    }

    public String getNome() {
        return nome;
    }

    public Status getStatus() {
        return status;
    }

    public List<Jogo> getJogos() {
        return List.copyOf(jogos);
    }

    private static String validarTextoObrigatorio(
            String texto,
            String mensagem) {

        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }

        return texto.trim();
    }
}