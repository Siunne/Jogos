package br.com.emilly.jogos.controller;

import br.com.emilly.jogos.application.JogoService;
import br.com.emilly.jogos.mapper.JogoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import br.com.emilly.jogos.dto.*;
import br.com.emilly.jogos.domain.Status;
import org.springframework.data.domain.Pageable;

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

    @PostMapping
    public ResponseEntity<JogoResponse> cadastrar(
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

    @GetMapping("/{id}")
    public ResponseEntity<JogoResponse> buscarPorId(
            @PathVariable Long id) {

        var jogo = service.buscarPorId(id);

        return ResponseEntity.ok(
                mapper.toResponse(jogo)
        );
    }

    @GetMapping
    public ResponseEntity<PaginaResponse<JogoResponse>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Long generoId,
            @RequestParam(required = false) Long fornecedorId,
            @RequestParam(required = false) Boolean abaixoEstoqueMinimo, Pageable pageable) {
        return ResponseEntity.ok(PaginaResponse.de(service.pesquisar(nome, status, generoId, fornecedorId,
                abaixoEstoqueMinimo, pageable).map(mapper::toResponse)));
    }

    @PutMapping("/{id}")
    public JogoResponse alterar(@PathVariable Long id, @Valid @RequestBody JogoAtualizacaoRequest request) {
        return mapper.toResponse(service.alterar(id, request.nome(), request.preco(), request.estoqueMinimo(),
                request.generoId(), request.fornecedorId()));
    }

    @PutMapping("/{id}/status")
    public JogoResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return mapper.toResponse(service.alterarStatus(id, request.status()));
    }

    @PostMapping("/{id}/estoque/entradas")
    public JogoResponse entrada(@PathVariable Long id, @Valid @RequestBody MovimentacaoEstoqueRequest request) {
        return mapper.toResponse(service.entrada(id, request.quantidade()));
    }

    @PostMapping("/{id}/estoque/saidas")
    public JogoResponse saida(@PathVariable Long id, @Valid @RequestBody MovimentacaoEstoqueRequest request) {
        return mapper.toResponse(service.saida(id, request.quantidade()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
