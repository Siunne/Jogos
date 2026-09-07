package br.com.emilly.jogos.exception;

public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}