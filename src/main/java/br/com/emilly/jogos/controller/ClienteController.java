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
@RequestMapping("/api/clientes")
@Tag(
    name = "Clientes",
    description = "Operações comerciais de clientes; sem autenticação nesta aula")
public class ClienteController {
  private final ClienteService service;
  private final ClienteMapper mapper;

  public ClienteController(ClienteService service, ClienteMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @Operation(
      operationId = "cadastrarCliente",
      summary = "Cadastrar cliente",
      description =
          "Associa papel a pessoa ativa. Uma identidade pode ter ambos os papéis, mas não repetir o"
              + " mesmo.")
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
  @PostMapping
  public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody ClienteRequest request) {
    var p = service.cadastrar(request.pessoaId(), request.telefone());
    return ResponseEntity.created(URI.create("/api/clientes/" + p.getId()))
        .body(mapper.toResponse(p));
  }

  @Operation(
      operationId = "consultarCliente",
      summary = "Consultar cliente",
      description = "Retorna vínculo e dados necessários da pessoa, carregados sem OSIV.")
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
  public ClienteResponse buscar(
      @Parameter(description = "Identificador do recurso", example = "1") @PathVariable Long id) {
    return mapper.toResponse(service.buscar(id));
  }

  @Operation(
      operationId = "pesquisarClientes",
      summary = "Pesquisar clientes",
      description =
          "Filtros combináveis no banco. Página padrão 20, máximo 100. Ordenação: id,telefone.")
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
  public PaginaResponse<ClienteResponse> pesquisar(
      @Parameter(description = "Nome da pessoa") @RequestParam(required = false) String nome,
      @Parameter(description = "Estado da pessoa") @RequestParam(required = false) Status status,
      @Parameter(description = "Filtra a identidade central") @RequestParam(required = false)
          Long pessoaId,
      @Parameter(hidden = true) Pageable pageable) {
    return PaginaResponse.de(
        service.pesquisar(nome, status, pessoaId, pageable).map(mapper::toResponse));
  }

  @Operation(
      operationId = "alterarCliente",
      summary = "Alterar cliente",
      description = "Altera somente telefone; null/vazio remove o contato.")
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
  @PutMapping("/{id}")
  public ClienteResponse alterar(
      @Parameter(description = "Identificador do recurso", example = "1") @PathVariable Long id,
      @Valid @RequestBody ClienteAtualizacaoRequest request) {
    return mapper.toResponse(service.alterar(id, request.telefone()));
  }
}
