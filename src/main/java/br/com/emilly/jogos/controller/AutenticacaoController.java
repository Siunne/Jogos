package br.com.emilly.jogos.controller;

import br.com.emilly.jogos.application.AutenticacaoService;
import br.com.emilly.jogos.dto.*;
import br.com.emilly.jogos.exception.ApiError;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/autenticacao")
@Tag(
    name = "Autenticação",
    description = "Emissão didática de JWT; não é um Authorization Server OAuth2 completo")
public class AutenticacaoController {
  private final AutenticacaoService service;

  public AutenticacaoController(AutenticacaoService service) {
    this.service = service;
  }

  @PostMapping("/login")
  @Operation(
      operationId = "autenticar",
      summary = "Autenticar e emitir Bearer JWT",
      description =
          "Público. Reutiliza BCrypt e status da conta/Pessoa. HS256, iss, sub, iat, exp, jti e"
              + " perfis do servidor; 1800 segundos absolutos. Bloqueio ou troca de senha impedem"
              + " novos logins com credenciais anteriores, mas não revogam tokens emitidos. Exige"
              + " TLS fora do laboratório.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Token emitido", useReturnTypeSchema = true),
    @ApiResponse(
        responseCode = "400",
        description = "JSON inválido ou campos ausentes",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Login ou senha inválidos, conta bloqueada ou Pessoa inativa",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  public TokenResponse login(@Valid @RequestBody LoginRequest request) {
    return service.autenticar(request.login(), request.senha());
  }
}
