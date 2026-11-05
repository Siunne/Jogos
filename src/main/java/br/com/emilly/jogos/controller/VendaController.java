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
@RequestMapping("/api/vendas")
@Tag(name = "Vendas", description = "Operações comerciais de vendas; sem autenticação nesta aula")
public class VendaController {
  private final VendaService service;
  private final VendaMapper mapper;

  public VendaController(VendaService service, VendaMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @Operation(
      operationId = "abrirVenda",
      summary = "Abrir venda",
      description = "Começa ABERTA, sem baixar estoque; cliente ativo e vendedor/gerente ativo.")
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
                schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping
  public ResponseEntity<VendaResponse> cadastrar(@Valid @RequestBody VendaRequest request) {
    var v = service.cadastrar(request.clienteId(), request.vendedorId());
    return ResponseEntity.created(URI.create("/api/vendas/" + v.getId()))
        .body(mapper.toResponse(v));
  }

  @Operation(
      operationId = "consultarVenda",
      summary = "Consultar venda",
      description =
          "Detalhe com itens e preços históricos; total derivado dos subtotais, sem coluna total.")
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
  public VendaResponse buscar(
      @Parameter(description = "Identificador do recurso", example = "1") @PathVariable Long id) {
    return mapper.toResponse(service.buscar(id));
  }

  @Operation(
      operationId = "pesquisarVendas",
      summary = "Pesquisar vendas",
      description =
          "Filtros combináveis por status, cliente, vendedor e datas inclusivas. Rejeita período"
              + " invertido. Ordenação: id,dataVenda,status. Resumo sem itens ou total evita"
              + " carregar coleções na página.")
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
  public PaginaResponse<VendaResumoResponse> pesquisar(
      @Parameter(description = "Estado da venda") @RequestParam(required = false)
          StatusVenda status,
      @Parameter(description = "Cliente comprador") @RequestParam(required = false) Long clienteId,
      @Parameter(description = "Colaborador vendedor") @RequestParam(required = false)
          Long vendedorId,
      @Parameter(description = "Início inclusivo, yyyy-MM-dd") @RequestParam(required = false)
          LocalDate dataInicial,
      @Parameter(description = "Fim inclusivo, yyyy-MM-dd") @RequestParam(required = false)
          LocalDate dataFinal,
      @Parameter(hidden = true) Pageable pageable) {
    return PaginaResponse.de(
        service
            .pesquisar(status, clienteId, vendedorId, dataInicial, dataFinal, pageable)
            .map(mapper::toResumo));
  }

  @Operation(
      operationId = "adicionarItemVenda",
      summary = "Adicionar item",
      description =
          "Somente ABERTA. Jogo ativo, quantidade positiva. Copia preço atual sem baixar estoque."
              + " Jogo não pode repetir na venda.")
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
  @PostMapping("/{id}/itens")
  public VendaResponse adicionar(
      @Parameter(description = "Identificador do recurso", example = "1") @PathVariable Long id,
      @Valid @RequestBody ItemVendaRequest request) {
    return mapper.toResponse(service.adicionarItem(id, request.jogoId(), request.quantidade()));
  }

  @Operation(
      operationId = "removerItemVenda",
      summary = "Remover item",
      description =
          "Somente ABERTA; item deve pertencer à venda. Exclusão do item sem efeito no estoque.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "204",
        description = "Item excluído, sem corpo",
        content = @Content),
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
  @DeleteMapping("/{vendaId}/itens/{itemId}")
  public ResponseEntity<Void> remover(
      @Parameter(description = "Venda aberta") @PathVariable Long vendaId,
      @Parameter(description = "Item pertencente à venda") @PathVariable Long itemId) {
    service.removerItem(vendaId, itemId);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      operationId = "finalizarVenda",
      summary = "Finalizar venda",
      description =
          "Uma única transação valida participantes, jogos ativos e saldos; baixa todos os jogos e"
              + " marca FINALIZADA. Venda vazia ou repetição falham. Rollback impede baixa parcial."
              + " Sem locks; concorrência permanece limitação.")
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
  @PostMapping("/{id}/finalizacao")
  public VendaResponse finalizar(
      @Parameter(description = "Identificador do recurso", example = "1") @PathVariable Long id) {
    return mapper.toResponse(service.finalizar(id));
  }

  @Operation(
      operationId = "cancelarVenda",
      summary = "Cancelar venda aberta",
      description =
          "ABERTA passa a CANCELADA sem baixa. Não permite reabrir, alterar ou estornar FINALIZADA"
              + " nesta aula.")
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
  @PostMapping("/{id}/cancelamento")
  public VendaResponse cancelar(
      @Parameter(description = "Identificador do recurso", example = "1") @PathVariable Long id) {
    return mapper.toResponse(service.cancelar(id));
  }
}
