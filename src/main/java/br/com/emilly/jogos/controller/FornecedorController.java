package br.com.emilly.jogos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import br.com.emilly.jogos.exception.ApiError;

import br.com.emilly.jogos.application.FornecedorService;
import br.com.emilly.jogos.dto.FornecedorRequest;
import br.com.emilly.jogos.dto.FornecedorResponse;
import br.com.emilly.jogos.mapper.FornecedorMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Fornecedores", description = "Cadastro e consultas dos fornecedores opcionais dos jogos.")
@RestController
@RequestMapping("/api/fornecedores")
public class FornecedorController {

    private final FornecedorService service;
    private final FornecedorMapper mapper;

    public FornecedorController(
            FornecedorService service,
            FornecedorMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(operationId = "cadastrarFornecedor", summary = "Cadastrar fornecedor", description = "Cria fornecedor ATIVO com razão social e identificador único de 14 dígitos. Exemplos são fictícios.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Cadastro concluído", useReturnTypeSchema = true, content = @Content(mediaType = "application/json"), headers = @Header(name = "Location", description = "URI relativa do recurso criado", schema = @Schema(type = "string", format = "uri-reference"))),
        @ApiResponse(responseCode = "400", description = "Dados ou JSON inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro400", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Dados inválidos\",\"path\":\"/api/fornecedores\",\"fields\":{\"razaoSocial\":\"A razão social é obrigatória\"}}"))),
        @ApiResponse(responseCode = "409", description = "Identificador já cadastrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro409", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":409,\"error\":\"Conflict\",\"message\":\"CNPJ já cadastrado\",\"path\":\"/api/fornecedores\",\"fields\":{}}")))
    })
    @PostMapping
    public ResponseEntity<FornecedorResponse> cadastrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Razão social obrigatória e CNPJ de 14 dígitos.", required = true)
            @Valid @RequestBody FornecedorRequest request) {

        var fornecedor = service.cadastrar(
                request.razaoSocial(),
                request.cnpj()
        );

        var response = mapper.toResponse(fornecedor);

        return ResponseEntity
                .created(URI.create(
                        "/api/fornecedores/" + fornecedor.getId()))
                .body(response);
    }

    @Operation(operationId = "consultarFornecedor", summary = "Consultar fornecedor por ID", description = "Retorna os dados do fornecedor existente. Não há operações públicas de alteração ou exclusão de fornecedor neste projeto.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação concluída", useReturnTypeSchema = true, content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Identificador não numérico", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro400", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Method parameter 'id': Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: \\\"abc\\\"\",\"path\":\"/api/fornecedores/abc\",\"fields\":{}}"))),
        @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro404", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Fornecedor não encontrado\",\"path\":\"/api/fornecedores/1\",\"fields\":{}}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<FornecedorResponse> buscarPorId(
            @Parameter(description = "Identificador do recurso, representado por Long", example = "1") @PathVariable Long id) {

        var fornecedor = service.buscarPorId(id);

        return ResponseEntity.ok(
                mapper.toResponse(fornecedor)
        );
    }

    @Operation(operationId = "listarFornecedores", summary = "Listar fornecedores", description = "Retorna array de fornecedores. Esta listagem não é paginada e não possui filtros ou ordenação configuráveis.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação concluída", useReturnTypeSchema = true, content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public ResponseEntity<List<FornecedorResponse>> listar() {

        var response = service.listar()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }
}