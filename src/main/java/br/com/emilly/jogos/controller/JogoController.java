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

import br.com.emilly.jogos.application.JogoService;
import br.com.emilly.jogos.mapper.JogoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import br.com.emilly.jogos.dto.*;
import br.com.emilly.jogos.domain.Status;
import org.springframework.data.domain.Pageable;

@Tag(name = "Jogos", description = "Cadastro, manutenção, pesquisa paginada e movimentação de estoque de jogos.")
@RestController
@RequestMapping("/api/jogos")
public class JogoController {

    private final JogoService service;
    private final JogoMapper mapper;

    public JogoController(
            JogoService service,
            JogoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(operationId = "cadastrarJogo", summary = "Cadastrar jogo", description = "Cria jogo ATIVO com gênero existente e fornecedor opcional. A data de cadastro é definida pelo servidor; não é enviada pelo cliente. Código é único; saldo inicial e mínimo não podem ser negativos.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Cadastro concluído", useReturnTypeSchema = true, content = @Content(mediaType = "application/json"), headers = @Header(name = "Location", description = "URI relativa do recurso criado", schema = @Schema(type = "string", format = "uri-reference"))),
        @ApiResponse(responseCode = "400", description = "Dados ou JSON inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro400", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Dados inválidos\",\"path\":\"/api/jogos\",\"fields\":{\"nome\":\"O nome é obrigatório\"}}"))),
        @ApiResponse(responseCode = "404", description = "Gênero ou fornecedor não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro404", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Gênero de jogo não encontrado\",\"path\":\"/api/jogos\",\"fields\":{}}"))),
        @ApiResponse(responseCode = "409", description = "Código já cadastrado ou conflito de integridade", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro409", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":409,\"error\":\"Conflict\",\"message\":\"Código do jogo já cadastrado\",\"path\":\"/api/jogos\",\"fields\":{}}")))
    })
    @PostMapping
    public ResponseEntity<JogoResponse> cadastrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados de criação sem dataCadastro e status; fornecedorId pode ser null ou omitido.", required = true)
            @Valid @RequestBody JogoRequest request) {

        var jogo = mapper.toEntity(request);

        jogo = service.cadastrar(
                jogo,
                request.generoId(),
                request.fornecedorId()
        );

        var response = mapper.toResponse(jogo);

        return ResponseEntity
                .created(URI.create("/api/jogos/" + jogo.getId()))
                .body(response);
    }

    @Operation(operationId = "consultarJogo", summary = "Consultar jogo por ID", description = "Retorna os dados completos e associações para consulta ou preenchimento do formulário. Código, saldo, data e status são apresentados, mas não pertencem ao PUT geral.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação concluída", useReturnTypeSchema = true, content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Identificador não numérico", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro400", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Method parameter 'id': Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: \\\"abc\\\"\",\"path\":\"/api/jogos/abc\",\"fields\":{}}"))),
        @ApiResponse(responseCode = "404", description = "Jogo não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro404", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Jogo não encontrado\",\"path\":\"/api/jogos/1\",\"fields\":{}}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<JogoResponse> buscarPorId(
            @Parameter(description = "Identificador do recurso, representado por Long", example = "1") @PathVariable Long id) {

        var jogo = service.buscarPorId(id);

        return ResponseEntity.ok(
                mapper.toResponse(jogo)
        );
    }

    @Operation(operationId = "pesquisarJogos", summary = "Pesquisar jogos", description = "Combina nome, status, generoId, fornecedorId e abaixoEstoqueMinimo no banco. Não existe filtro por preço. Página base zero, padrão 20, máximo 100. Ordenação permitida: id, nome, codigo, preco, estoqueMinimo, unidadesDisponiveis, dataCadastro, status. Retorna os sete campos de PaginaResponse.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação concluída", useReturnTypeSchema = true, content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Filtro, paginação ou ordenação inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro400", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"page deve estar entre 0 e 2147483647\",\"path\":\"/api/jogos\",\"fields\":{}}")))
    })
    @Parameters({
        @Parameter(name = "page", in = ParameterIn.QUERY, description = "Página iniciada em zero; valor negativo ou não inteiro retorna 400", schema = @Schema(type = "integer", minimum = "0", defaultValue = "0"), example = "0"),
        @Parameter(name = "size", in = ParameterIn.QUERY, description = "Tamanho da página: padrão 20, entre 1 e 100; fora do intervalo retorna 400", schema = @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "20"), example = "20"),
        @Parameter(name = "sort", in = ParameterIn.QUERY, description = "Ordenação campo,asc ou campo,desc; repetir sort para vários campos. Padrão id,asc. Campos permitidos descritos na operação; caminhos arbitrários retornam 400", array = @ArraySchema(schema = @Schema(type = "string")), example = "[\"nome,asc\"]")
    })
    @GetMapping
    public ResponseEntity<PaginaResponse<JogoResponse>> listar(
            @Parameter(description = "Trecho do nome, sem diferenciar caixa; espaços externos são ignorados", example = "estrelas")
            @RequestParam(required = false) String nome,
            @Parameter(description = "Filtra por ATIVO ou INATIVO; outro valor retorna 400", example = "ATIVO")
            @RequestParam(required = false) Status status,
            @Parameter(description = "Identificador exato do gênero; ausência não restringe a consulta", example = "1")
            @RequestParam(required = false) Long generoId,
            @Parameter(description = "Identificador exato do fornecedor; ausência não restringe a consulta", example = "1")
            @RequestParam(required = false) Long fornecedorId,
            @Parameter(description = "true exige saldo estritamente menor que o mínimo; false ou ausência não restringe o saldo", example = "true")
            @RequestParam(required = false) Boolean abaixoEstoqueMinimo, @Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.ok(PaginaResponse.de(service.pesquisar(nome, status, generoId, fornecedorId,
                abaixoEstoqueMinimo, pageable).map(mapper::toResponse)));
    }

    @Operation(operationId = "alterarJogo", summary = "Alterar jogo", description = "Substitui nome, preço, estoque mínimo, gênero e fornecedor. Preserva código, saldo, data de cadastro e status. Fornecedor null ou ausente remove a associação. A troca de gênero mantém os dois lados da associação. PUT idempotente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação concluída", useReturnTypeSchema = true, content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Dados ou identificador inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro400", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Dados inválidos\",\"path\":\"/api/jogos/1\",\"fields\":{\"nome\":\"não deve estar em branco\"}}"))),
        @ApiResponse(responseCode = "404", description = "Jogo, gênero ou fornecedor não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro404", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Jogo não encontrado\",\"path\":\"/api/jogos/1\",\"fields\":{}}"))),
        @ApiResponse(responseCode = "409", description = "Conflito de integridade detectado pelo banco", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro409", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":409,\"error\":\"Conflict\",\"message\":\"Operação em conflito com a integridade dos dados\",\"path\":\"/api/jogos/1\",\"fields\":{}}")))
    })
    @PutMapping("/{id}")
    public JogoResponse alterar(@Parameter(description = "Identificador do recurso, representado por Long", example = "1") @PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Representação editável completa; gênero obrigatório e fornecedor opcional.", required = true)
            @Valid @RequestBody JogoAtualizacaoRequest request) {
        return mapper.toResponse(service.alterar(id, request.nome(), request.preco(), request.estoqueMinimo(),
                request.generoId(), request.fornecedorId()));
    }

    @Operation(operationId = "alterarStatusJogo", summary = "Alterar status do jogo", description = "Ativa ou inativa o jogo sem excluir o cadastro nem alterar saldo ou data. Alterar status não equivale a exclusão física.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação concluída", useReturnTypeSchema = true, content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Status ausente/inválido ou identificador inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro400", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"JSON inválido ou malformado\",\"path\":\"/api/jogos/1/status\",\"fields\":{}}"))),
        @ApiResponse(responseCode = "404", description = "Jogo não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro404", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Jogo não encontrado\",\"path\":\"/api/jogos/1/status\",\"fields\":{}}")))
    })
    @PutMapping("/{id}/status")
    public JogoResponse alterarStatus(@Parameter(description = "Identificador do recurso, representado por Long", example = "1") @PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Status obrigatório: ATIVO ou INATIVO.", required = true)
            @Valid @RequestBody StatusRequest request) {
        return mapper.toResponse(service.alterarStatus(id, request.status()));
    }

    @Operation(operationId = "entradaEstoque", summary = "Registrar entrada de estoque", description = "Soma quantidade positiva ao saldo atual. Operação não idempotente e acumulativa: repetir a chamada repete o efeito. Quantidade zero, negativa, ausente ou com mais de três casas decimais é rejeitada.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação concluída", useReturnTypeSchema = true, content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Quantidade ou identificador inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro400", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Dados inválidos\",\"path\":\"/api/jogos/1/estoque/entradas\",\"fields\":{\"quantidade\":\"deve ser maior que 0\"}}"))),
        @ApiResponse(responseCode = "404", description = "Jogo não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro404", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Jogo não encontrado\",\"path\":\"/api/jogos/1/estoque/entradas\",\"fields\":{}}")))
    })
    @PostMapping("/{id}/estoque/entradas")
    public JogoResponse entrada(@Parameter(description = "Identificador do recurso, representado por Long", example = "1") @PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Quantidade positiva, com no máximo três casas decimais.", required = true)
            @Valid @RequestBody MovimentacaoEstoqueRequest request) {
        return mapper.toResponse(service.entrada(id, request.quantidade()));
    }

    @Operation(operationId = "saidaEstoque", summary = "Registrar saída de estoque", description = "Subtrai quantidade positiva do saldo. Operação acumulativa e não idempotente. Saldo insuficiente retorna 400 sem alterar o saldo; o estoque nunca pode ficar negativo. Quantidades inválidas são rejeitadas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Operação concluída", useReturnTypeSchema = true, content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "400", description = "Quantidade inválida ou unidades disponíveis insuficientes", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro400", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Dados inválidos\",\"path\":\"/api/jogos/1/estoque/saidas\",\"fields\":{\"quantidade\":\"deve ser maior que 0\"}}"))),
        @ApiResponse(responseCode = "404", description = "Jogo não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro404", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Jogo não encontrado\",\"path\":\"/api/jogos/1/estoque/saidas\",\"fields\":{}}")))
    })
    @PostMapping("/{id}/estoque/saidas")
    public JogoResponse saida(@Parameter(description = "Identificador do recurso, representado por Long", example = "1") @PathVariable Long id, @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Quantidade positiva que não pode superar o saldo disponível.", required = true)
            @Valid @RequestBody MovimentacaoEstoqueRequest request) {
        return mapper.toResponse(service.saida(id, request.quantidade()));
    }

    @Operation(operationId = "excluirJogo", summary = "Excluir jogo", description = "Remove fisicamente o jogo. Sucesso retorna 204 sem corpo; consulta posterior retorna 404. Conflitos de integridade, caso detectados pelo banco, retornam 409. Não existe regra adicional de jogo em uso neste domínio.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Exclusão concluída, sem corpo", content = @Content),
        @ApiResponse(responseCode = "400", description = "Identificador não numérico", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro400", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Method parameter 'id': Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: \\\"abc\\\"\",\"path\":\"/api/jogos/abc\",\"fields\":{}}"))),
        @ApiResponse(responseCode = "404", description = "Jogo não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro404", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Jogo não encontrado\",\"path\":\"/api/jogos/1\",\"fields\":{}}"))),
        @ApiResponse(responseCode = "409", description = "Conflito de integridade detectado pelo banco", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(name = "Erro409", value = "{\"timestamp\":\"2026-11-04T18:30:00\",\"status\":409,\"error\":\"Conflict\",\"message\":\"Operação em conflito com a integridade dos dados\",\"path\":\"/api/jogos/1\",\"fields\":{}}")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@Parameter(description = "Identificador do recurso, representado por Long", example = "1") @PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
