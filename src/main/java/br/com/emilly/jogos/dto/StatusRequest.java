package br.com.emilly.jogos.dto;
import br.com.emilly.jogos.domain.Status;
import jakarta.validation.constraints.NotNull;
public record StatusRequest(@NotNull Status status) {}
