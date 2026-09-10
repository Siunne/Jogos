package br.com.emilly.jogos.controller;

import br.com.emilly.jogos.application.JogoService;
import br.com.emilly.jogos.dto.JogoRequest;
import br.com.emilly.jogos.dto.JogoResponse;
import br.com.emilly.jogos.mapper.JogoMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

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
    public ResponseEntity<List<JogoResponse>> listar() {

        var response = service.listar()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }
}