package br.com.emilly.jogos.controller;

import br.com.emilly.jogos.application.GeneroJogoService;
import br.com.emilly.jogos.mapper.GeneroJogoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import br.com.emilly.jogos.dto.*;
import br.com.emilly.jogos.domain.Status;
import org.springframework.data.domain.Pageable;

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

    @PostMapping
    public ResponseEntity<GeneroJogoResponse> cadastrar(
            @Valid @RequestBody GeneroJogoRequest request) {

        var genero = service.cadastrar(request.nome());
        var response = mapper.toResponse(genero);

        return ResponseEntity
                .created(URI.create("/api/generos/" + genero.getId()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneroJogoResponse> buscarPorId(
            @PathVariable Long id) {

        var genero = service.buscarPorId(id);

        return ResponseEntity.ok(
                mapper.toResponse(genero)
        );
    }

    @GetMapping
    public ResponseEntity<PaginaResponse<GeneroJogoResponse>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Status status, Pageable pageable) {
        return ResponseEntity.ok(PaginaResponse.de(service.pesquisar(nome, status, pageable).map(mapper::toResponse)));
    }

    @PutMapping("/{id}")
    public GeneroJogoResponse alterar(@PathVariable Long id, @Valid @RequestBody GeneroJogoAtualizacaoRequest request) {
        return mapper.toResponse(service.alterar(id, request.nome()));
    }

    @PutMapping("/{id}/status")
    public GeneroJogoResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return mapper.toResponse(service.alterarStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
