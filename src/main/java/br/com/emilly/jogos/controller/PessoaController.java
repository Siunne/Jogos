package br.com.emilly.jogos.controller;

import br.com.emilly.jogos.application.*;
import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.dto.*;
import br.com.emilly.jogos.exception.ApiError;
import br.com.emilly.jogos.mapper.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.*;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pessoas")
@Tag(name = "Pessoas", description = "Operações comerciais de pessoas; sem autenticação nesta aula")
public class PessoaController {
  private final PessoaService service;
  private final PessoaMapper mapper;

  public PessoaController(PessoaService service, PessoaMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @Operation(
      operationId = "cadastrarPessoa",
      summary = "Cadastrar pessoa",
      description =
          "Cria pessoa ATIVA. Data do servidor, CPF sem pontuação e email normalizado; valida DVs"
              + " sem comprovar titularidade.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Recurso criado; Location contém a URI relativa",
        useReturnTypeSchema = true,
        content = @Content(mediaType = "application/json"),
        headers =
            @Header(
                name = "Location",
                description = "URI do recurso criado",
                schema = @Schema(type = "string", format = "uri-reference"))),
    @ApiResponse(
        responseCode = "400",
        description = "Dados, parâmetros ou transição comercial inválidos",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Duplicidade ou conflito de integridade",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping
  public ResponseEntity<PessoaResponse> cadastrar(@Valid @RequestBody PessoaRequest request) {
    var p = service.cadastrar(request.nome(), request.email(), request.cpf());
    return ResponseEntity.created(URI.create("/api/pessoas/" + p.getId()))
        .body(mapper.toResponse(p));
  }

  @Operation(
      operationId = "consultarPessoa",
      summary = "Consultar pessoa",
      description = "Retorna a identidade civil.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Operação concluída",
        useReturnTypeSchema = true,
        content = @Content(mediaType = "application/json")),
    @ApiResponse(
        responseCode = "400",
        description = "Dados, parâmetros ou transição comercial inválidos",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Recurso ou referência inexistente",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{id}")
  public PessoaResponse buscar(
      @Parameter(description = "Identificador do recurso", example = "1") @PathVariable Long id) {
    return mapper.toResponse(service.buscar(id));
  }

  @Operation(
      operationId = "pesquisarPessoas",
      summary = "Pesquisar pessoas",
      description =
          "Filtros combináveis por nome e status. Ordenação: id,nome,email,cpf,dataCadastro,status."
              + " PaginaResponse com sete campos.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Operação concluída",
        useReturnTypeSchema = true,
        content = @Content(mediaType = "application/json")),
    @ApiResponse(
        responseCode = "400",
        description = "Dados, parâmetros ou transição comercial inválidos",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
  })
  @Parameters({
    @Parameter(
        name = "page",
        description = "Página base zero",
        schema = @Schema(type = "integer", minimum = "0", defaultValue = "0")),
    @Parameter(
        name = "size",
        description = "Padrão 20, mínimo 1 e máximo 100; inválido retorna 400",
        schema = @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "20")),
    @Parameter(
        name = "sort",
        description =
            "campo,asc ou campo,desc; repetível. Campos permitidos na descrição da operação",
        array = @ArraySchema(schema = @Schema(type = "string")),
        example = "[\"id,asc\"]")
  })
  @GetMapping
  public PaginaResponse<PessoaResponse> pesquisar(
      @Parameter(description = "Trecho do nome, ignora caixa") @RequestParam(required = false)
          String nome,
      @Parameter(description = "ATIVO ou INATIVO") @RequestParam(required = false) Status status,
      @Parameter(hidden = true) Pageable pageable) {
    return PaginaResponse.de(service.pesquisar(nome, status, pageable).map(mapper::toResponse));
  }

  @Operation(
      operationId = "alterarPessoa",
      summary = "Alterar pessoa",
      description =
          "Substitui nome, email e CPF com validação também no domínio. Preserva data/status e"
              + " vínculos históricos.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Operação concluída",
        useReturnTypeSchema = true,
        content = @Content(mediaType = "application/json")),
    @ApiResponse(
        responseCode = "400",
        description = "Dados, parâmetros ou transição comercial inválidos",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Recurso ou referência inexistente",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Duplicidade ou conflito de integridade",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
  })
  @PutMapping("/{id}")
  public PessoaResponse alterar(
      @Parameter(description = "Identificador do recurso", example = "1") @PathVariable Long id,
      @Valid @RequestBody PessoaRequest request) {
    return mapper.toResponse(service.alterar(id, request.nome(), request.email(), request.cpf()));
  }

  @Operation(
      operationId = "alterarStatusPessoa",
      summary = "Alterar status da pessoa",
      description =
          "Inativa ou ativa sem apagar papéis ou vendas. Inativa não participa de novas"
              + " vendas/finalizações.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Operação concluída",
        useReturnTypeSchema = true,
        content = @Content(mediaType = "application/json")),
    @ApiResponse(
        responseCode = "400",
        description = "Dados, parâmetros ou transição comercial inválidos",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Recurso ou referência inexistente",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class)))
  })
  @PutMapping("/{id}/status")
  public PessoaResponse status(
      @Parameter(description = "Identificador do recurso", example = "1") @PathVariable Long id,
      @Valid @RequestBody StatusRequest request) {
    return mapper.toResponse(service.status(id, request.status()));
  }
}
