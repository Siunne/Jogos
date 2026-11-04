package br.com.emilly.jogos.dto;
import jakarta.validation.constraints.*;
public record GeneroJogoAtualizacaoRequest(@NotBlank @Size(max = 120) String nome) {}
