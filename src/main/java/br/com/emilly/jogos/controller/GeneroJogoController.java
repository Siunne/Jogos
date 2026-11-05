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

import br.com.emilly.jogos.application.GeneroJogoService;
import br.com.emilly.jogos.mapper.GeneroJogoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import br.com.emilly.jogos.dto.*;
import br.com.emilly.jogos.domain.Status;
import org.springframework.data.domain.Pageable;

@Tag(name = "Gêneros de jogos", description = "Cadastro, consulta, pesquisa, status e exclusão de gêneros de jogos.")
@RestController
@RequestMapping("/api/generos")
public class GeneroJogoController {

    private final GeneroJogoService service;
    private final GeneroJogoMapper mapper;

    public GeneroJogoController(
            GeneroJogoService service,
            GeneroJogoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(operationId = "cadastrarGenero", summary = "Cadastrar gênero", description = "Cria gênero ATIVO. Nome único após ignorar caixa e espaços externos; cadastro permite até 100 caracteres.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Cadastro concluído", useReturnTypeSchema = true, content = @Content(mediaType = "application/json"), headers = @Header(name = "Location", description = "URI relativa do recurso criado", schema = @Schema(type = "string", format = "uri-reference"))),
        @ApiResponse(responseCode = "400", description = "Nome inválido ou JSON malformado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro400", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Dados inválidos\",\"path\":\"/api/generos\",\"fields\":{\"nome\":\"O nome é obrigatório\"}}"))),
        @ApiResponse(responseCode = "409", description = "Nome normalizado já cadastrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro409", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":409,\"error\":\"Conflict\",\"message\":\"Nome do gênero já cadastrado\",\"path\":\"/api/generos\",\"fields\":{}}")))
    })
    @PostMapping
    public ResponseEntity<GeneroJogoResponse> cadastrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do gênero; não recebe status.", required = true)
            @Valid @RequestBody GeneroJogoRequest request) {

        var genero = service.cadastrar(request.nome());
        var response = mapper.toResponse(genero);

        return ResponseEntity
                .created(URI.create("/api/generos/" + genero.getId()))
                .body(response);
    }

    @Operation(operationId = "consultarGenero", summary = "Consultar gênero por ID", description = "Retorna o gênero com nome e status para consulta e preenchimento do formulário de alteração.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação concluída", useReturnTypeSchema = true, content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Identificador não numérico", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro400", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Method parameter 'id': Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: \\\"abc\\\"\",\"path\":\"/api/generos/abc\",\"fields\":{}}"))),
        @ApiResponse(responseCode = "404", description = "Gênero não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro404", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Gênero de jogo não encontrado\",\"path\":\"/api/generos/1\",\"fields\":{}}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<GeneroJogoResponse> buscarPorId(
            @Parameter(description = "Identificador do recurso, representado por Long", example = "1") @PathVariable Long id) {

        var genero = service.buscarPorId(id);

        return ResponseEntity.ok(
                mapper.toResponse(genero)
        );
    }

    @Operation(operationId = "pesquisarGeneros", summary = "Pesquisar gêneros", description = "Combina nome e status no banco. Página base zero, padrão 20, máximo 100. Ordenação permitida: id, nome, status. Retorna PaginaResponse, não Page do Spring.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação concluída", useReturnTypeSchema = true, content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Filtro, paginação ou ordenação inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro400", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"page deve estar entre 0 e 2147483647\",\"path\":\"/api/generos\",\"fields\":{}}")))
    })
    @Parameters({
        @Parameter(name = "page", in = ParameterIn.QUERY, description = "Página iniciada em zero; valor negativo ou não inteiro retorna 400", schema = @Schema(type = "integer", minimum = "0", defaultValue = "0"), example = "0"),
        @Parameter(name = "size", in = ParameterIn.QUERY, description = "Tamanho da página: padrão 20, entre 1 e 100; fora do intervalo retorna 400", schema = @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "20"), example = "20"),
        @Parameter(name = "sort", in = ParameterIn.QUERY, description = "Ordenação campo,asc ou campo,desc; repetir sort para vários campos. Padrão id,asc. Campos permitidos descritos na operação; caminhos arbitrários retornam 400", array = @ArraySchema(schema = @Schema(type = "string")), example = "[\"nome,asc\"]")
    })
    @GetMapping
    public ResponseEntity<PaginaResponse<GeneroJogoResponse>> listar(
            @Parameter(description = "Trecho do nome, sem diferenciar caixa; espaços externos são ignorados", example = "aventura")
            @RequestParam(required = false) String nome,
            @Parameter(description = "Filtra por ATIVO ou INATIVO; outro valor retorna 400", example = "ATIVO")
            @RequestParam(required = false) Status status, @Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.ok(PaginaResponse.de(service.pesquisar(nome, status, pageable).map(mapper::toResponse)));
    }

    @Operation(operationId = "alterarGenero", summary = "Alterar gênero", description = "Substitui o nome do gênero, normalizando espaços externos e verificando duplicidade sem considerar o próprio ID. PUT idempotente; preserva status.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação concluída", useReturnTypeSchema = true, content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Nome ou identificador inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro400", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Dados inválidos\",\"path\":\"/api/generos/1\",\"fields\":{\"nome\":\"não deve estar em branco\"}}"))),
        @ApiResponse(responseCode = "404", description = "Gênero não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro404", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Gênero de jogo não encontrado\",\"path\":\"/api/generos/1\",\"fields\":{}}"))),
        @ApiResponse(responseCode = "409", description = "Nome normalizado já cadastrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro409", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":409,\"error\":\"Conflict\",\"message\":\"Nome do gênero já cadastrado\",\"path\":\"/api/generos/1\",\"fields\":{}}")))
    })
    @PutMapping("/{id}")
    public GeneroJogoResponse alterar(@Parameter(description = "Identificador do recurso, representado por Long", example = "1") @PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Representação editável: nome obrigatório, até 120 caracteres.", required = true)
            @Valid @RequestBody GeneroJogoAtualizacaoRequest request) {
        return mapper.toResponse(service.alterar(id, request.nome()));
    }

    @Operation(operationId = "alterarStatusGenero", summary = "Alterar status do gênero", description = "Ativa ou inativa sem excluir fisicamente ou remover jogos associados. Repetir o mesmo status mantém o estado final.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação concluída", useReturnTypeSchema = true, content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Status ausente/inválido ou identificador inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro400", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"JSON inválido ou malformado\",\"path\":\"/api/generos/1/status\",\"fields\":{}}"))),
        @ApiResponse(responseCode = "404", description = "Gênero não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro404", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Gênero de jogo não encontrado\",\"path\":\"/api/generos/1/status\",\"fields\":{}}")))
    })
    @PutMapping("/{id}/status")
    public GeneroJogoResponse alterarStatus(@Parameter(description = "Identificador do recurso, representado por Long", example = "1") @PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Status obrigatório: ATIVO ou INATIVO.", required = true)
            @Valid @RequestBody StatusRequest request) {
        return mapper.toResponse(service.alterarStatus(id, request.status()));
    }

    @Operation(operationId = "excluirGenero", summary = "Excluir gênero", description = "Exclui fisicamente somente gênero sem jogos. Gênero em uso gera 409; para mantê-lo e preservar vínculos use a operação de status. Sucesso não possui corpo.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Exclusão concluída, sem corpo", content = @Content),
        @ApiResponse(responseCode = "400", description = "Identificador não numérico", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro400", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Method parameter 'id': Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: \\\"abc\\\"\",\"path\":\"/api/generos/abc\",\"fields\":{}}"))),
        @ApiResponse(responseCode = "404", description = "Gênero não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro404", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Gênero de jogo não encontrado\",\"path\":\"/api/generos/1\",\"fields\":{}}"))),
        @ApiResponse(responseCode = "409", description = "Gênero possui jogos ou conflito de integridade", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro409", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":409,\"error\":\"Conflict\",\"message\":\"Gênero não pode ser excluído porque possui jogos\",\"path\":\"/api/generos/1\",\"fields\":{}}")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@Parameter(description = "Identificador do recurso, representado por Long", example = "1") @PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
