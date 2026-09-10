package br.com.emilly.jogos.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiError> tratarNaoEncontrado(
            RecursoNaoEncontradoException ex,
            HttpServletRequest request) {

        ApiError erro = criarErro(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ApiError> tratarDuplicidade(
            RecursoDuplicadoException ex,
            HttpServletRequest request) {

        ApiError erro = criarErro(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> tratarValidacao(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> fields = new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(erro ->
                        fields.put(
                                erro.getField(),
                                erro.getDefaultMessage()
                        )
                );

        ApiError erro = criarErro(
                HttpStatus.BAD_REQUEST,
                "Dados inválidos",
                request.getRequestURI(),
                fields
        );

        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> tratarJsonMalformado(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        ApiError erro = criarErro(
                HttpStatus.BAD_REQUEST,
                "JSON inválido ou malformado",
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity.badRequest().body(erro);
    }

    private ApiError criarErro(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> fields) {

        return new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                fields
        );
    }
}