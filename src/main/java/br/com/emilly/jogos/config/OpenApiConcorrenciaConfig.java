package br.com.emilly.jogos.config;

import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Map;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/** Incremento da Aula 11; os contratos históricos permanecem em docs. */
@Configuration
public class OpenApiConcorrenciaConfig {
  @Bean
  @Order(100)
  OpenApiCustomizer conflitosConcorrentes() {
    return api ->
        api.getPaths()
            .forEach(
                (path, item) ->
                    item.readOperations()
                        .forEach(
                            op -> {
                              if (!Set.of(
                                      "entradaEstoque",
                                      "saidaEstoque",
                                      "finalizarVenda",
                                      "cancelarVenda",
                                      "adicionarItemVenda",
                                      "removerItemVenda")
                                  .contains(op.getOperationId())) return;
                              op.setDescription(
                                  op.getDescription()
                                      + " Comandos usam bloqueio pessimista transacional; podem"
                                      + " aguardar outra transação. Timeout/deadlock retorna 409"
                                      + " sem retry automático.");
                              if (!op.getResponses().containsKey("409"))
                                op.getResponses()
                                    .addApiResponse(
                                        "409",
                                        new ApiResponse()
                                            .description(
                                                "Conflito concorrente (timeout ou deadlock)")
                                            .content(
                                                new Content()
                                                    .addMediaType(
                                                        "application/json",
                                                        new MediaType()
                                                            .schema(
                                                                new Schema<>()
                                                                    .$ref(
                                                                        "#/components/schemas/ApiError")))));
                              var media =
                                  op.getResponses().get("409").getContent().get("application/json");
                              var example =
                                  new Example()
                                      .description(
                                          "Timeout ou deadlock; nenhuma baixa parcial é confirmada")
                                      .value(
                                          Map.of(
                                              "timestamp",
                                              "2026-11-05T12:00:00",
                                              "status",
                                              409,
                                              "error",
                                              "Conflict",
                                              "message",
                                              "Conflito concorrente; operação não concluída",
                                              "path",
                                              path.replace("{id}", "1")
                                                  .replace("{vendaId}", "1")
                                                  .replace("{itemId}", "1"),
                                              "fields",
                                              Map.of()));
                              if (media.getExamples() == null
                                  || !media.getExamples().containsKey("Erro409"))
                                media.addExamples("Erro409", example);
                              else media.addExamples("ConflitoConcorrente", example);
                            }));
  }
}
