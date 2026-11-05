package br.com.emilly.jogos.controller;

import br.com.emilly.jogos.application.ContaAcessoService;
import br.com.emilly.jogos.dto.*;
import br.com.emilly.jogos.exception.ApiError;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contas-acesso")
@Tag(
    name = "Contas de acesso",
    description =
        "Identidade de acesso independente dos papéis comerciais. HTTP Basic exige TLS fora do"
            + " laboratório local.")
public class ContaAcessoController {
  private final ContaAcessoService service;

  public ContaAcessoController(ContaAcessoService service) {
    this.service = service;
  }

  @Operation(
      operationId = "criarContaAcesso",
      summary = "Criar conta OPERADOR",
      description =
          "Bootstrap público provisório. Pessoa ativa, uma conta por pessoa, login único"
              + " normalizado. Senha recebe BCrypt; nunca é devolvida. Perfis e status não são"
              + " aceitos como campos do contrato.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Conta criada; Location aponta para /me, que exige Basic",
        useReturnTypeSchema = true),
    @ApiResponse(
        responseCode = "400",
        description = "Dados inválidos",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Pessoa inexistente",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Login ou pessoa já vinculados",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping
  public ResponseEntity<ContaAcessoResponse> cadastrar(
      @Valid @RequestBody ContaAcessoRequest request) {
    return ResponseEntity.created(URI.create("/api/contas-acesso/me"))
        .body(service.cadastrar(request.pessoaId(), request.login(), request.senha()));
  }

  @Operation(
      operationId = "consultarMinhaConta",
      summary = "Consultar própria conta",
      description = "Exige Basic a cada chamada. Conta bloqueada ou Pessoa inativa não autentica.")
  @ApiResponse(responseCode = "200", description = "Conta autenticada", useReturnTypeSchema = true)
  @GetMapping("/me")
  public ContaAcessoResponse propria(Principal principal) {
    return service.propria(principal.getName());
  }

  @Operation(
      operationId = "alterarMinhaSenha",
      summary = "Alterar própria senha",
      description =
          "Exige senha atual correta e nova diferente, mínimo12 caracteres e máximo72 bytes UTF-8."
              + " Commit invalida a senha antiga nas próximas requisições; não há sessão ou retry"
              + " automático.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "204",
        description = "Senha alterada, sem corpo",
        content = @Content),
    @ApiResponse(
        responseCode = "400",
        description = "Senha atual incorreta, repetida ou nova inválida",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PutMapping("/me/senha")
  public ResponseEntity<Void> alterar(
      Principal principal, @Valid @RequestBody AlteracaoSenhaRequest request) {
    service.alterarSenha(principal.getName(), request.senhaAtual(), request.novaSenha());
    return ResponseEntity.noContent().build();
  }
}
