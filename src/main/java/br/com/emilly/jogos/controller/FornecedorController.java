package br.com.emilly.jogos.controller;

import br.com.emilly.jogos.application.FornecedorService;
import br.com.emilly.jogos.dto.FornecedorRequest;
import br.com.emilly.jogos.dto.FornecedorResponse;
import br.com.emilly.jogos.mapper.FornecedorMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

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

    @PostMapping
    public ResponseEntity<FornecedorResponse> cadastrar(
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

    @GetMapping("/{id}")
    public ResponseEntity<FornecedorResponse> buscarPorId(
            @PathVariable Long id) {

        var fornecedor = service.buscarPorId(id);

        return ResponseEntity.ok(
                mapper.toResponse(fornecedor)
        );
    }

    @GetMapping
    public ResponseEntity<List<FornecedorResponse>> listar() {

        var response = service.listar()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }
}