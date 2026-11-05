package br.com.emilly.jogos.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import br.com.emilly.jogos.exception.ApiError;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Infraestrutura", description = "Verificação simples da disponibilidade HTTP da aplicação.")
@RestController
public class HealthController {

    @Operation(operationId = "verificarHealth", summary = "Verificar aplicação", description = "Retorna o texto OK quando a aplicação atende HTTP. Não é um diagnóstico completo dos serviços externos.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Aplicação respondendo", content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "OK")))
    })
    @GetMapping("/api/health")
    public String health() {
        return "OK";
    }
}