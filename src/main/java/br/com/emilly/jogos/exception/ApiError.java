package br.com.emilly.jogos.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Erro HTTP padronizado; campos correspondem ao ApiExceptionHandler real.")
public record ApiError(
        @Schema(description = "Data e hora locais do servidor, sem offset; formato ISO LocalDateTime", example = "2026-11-04T18:30:00", type = "string", format = "local-date-time", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime timestamp,
        @Schema(description = "Código de status HTTP", example = "400", minimum = "100", maximum = "599", requiredMode = Schema.RequiredMode.REQUIRED)
        int status,
        @Schema(description = "Nome do status HTTP", example = "Bad Request", requiredMode = Schema.RequiredMode.REQUIRED)
        String error,
        @Schema(description = "Mensagem da validação, regra de negócio ou recurso inexistente", example = "Unidades disponíveis insuficientes", requiredMode = Schema.RequiredMode.REQUIRED)
        String message,
        @Schema(description = "Caminho HTTP da requisição que falhou", example = "/api/jogos/1/estoque/saidas", requiredMode = Schema.RequiredMode.REQUIRED)
        String path,
        @Schema(description = "Mensagens por campo em erros de validação; objeto vazio nos demais casos", example = "{}", requiredMode = Schema.RequiredMode.REQUIRED)
        Map<String, String> fields
) {
}