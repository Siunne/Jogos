package br.com.emilly.jogos.controller;

import br.com.emilly.jogos.application.GeneroJogoService;
import br.com.emilly.jogos.dto.GeneroJogoRequest;
import br.com.emilly.jogos.dto.GeneroJogoResponse;
import br.com.emilly.jogos.mapper.GeneroJogoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

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
    public ResponseEntity<List<GeneroJogoResponse>> listar() {

        var response = service.listar()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }
}