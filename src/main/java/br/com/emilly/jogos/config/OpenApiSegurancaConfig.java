package br.com.emilly.jogos.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.*;
import java.util.*;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;

@Configuration
public class OpenApiSegurancaConfig {
  @Bean
  @Order(200)
  OpenApiCustomizer segurancaOpenApi() {
    return api -> {
      api.getComponents()
          .addSecuritySchemes(
              "basicAuth",
              new SecurityScheme()
                  .type(SecurityScheme.Type.HTTP)
                  .scheme("basic")
                  .description(
                      "HTTP Basic stateless. Use HTTPS/TLS; Base64 não cifra credenciais."));
      api.setSecurity(List.of(new SecurityRequirement().addList("basicAuth")));
      api.getComponents()
          .addSchemas(
              "ErroAutenticacao",
              new ObjectSchema()
                  .addProperty("status", new IntegerSchema().example(401))
                  .addProperty(
                      "message",
                      new StringSchema()
                          .example("Autenticação necessária ou credenciais inválidas"))
                  .required(List.of("status", "message")));
      api.getPaths()
          .forEach(
              (path, item) ->
                  item.readOperationsMap()
                      .forEach(
                          (method, op) -> {
                            boolean publica =
                                method == PathItem.HttpMethod.GET && path.equals("/api/health")
                                    || method == PathItem.HttpMethod.POST
                                        && Set.of("/api/pessoas", "/api/contas-acesso")
                                            .contains(path);
                            if (publica) op.setSecurity(List.of());
                            else {
                              op.setSecurity(
                                  List.of(new SecurityRequirement().addList("basicAuth")));
                              op.getResponses()
                                  .addApiResponse(
                                      "401",
                                      new ApiResponse()
                                          .description(
                                              "Credencial ausente, inválida, conta bloqueada ou"
                                                  + " Pessoa inativa")
                                          .addHeaderObject(
                                              "WWW-Authenticate",
                                              new Header()
                                                  .schema(new StringSchema())
                                                  .description("Desafio HTTP Basic"))
                                          .content(
                                              new Content()
                                                  .addMediaType(
                                                      "application/json",
                                                      new MediaType()
                                                          .schema(
                                                              new Schema<>()
                                                                  .$ref(
                                                                      "#/components/schemas/ErroAutenticacao"))
                                                          .addExamples(
                                                              "Erro401",
                                                              new Example()
                                                                  .value(
                                                                      Map.of(
                                                                          "status",
                                                                          401,
                                                                          "message",
                                                                          "Autenticação necessária"
                                                                              + " ou credenciais"
                                                                              + " inválidas"))))));
                            }
                            if (path.startsWith("/api/contas-acesso")) {
                              if (op.getResponses().containsKey("201"))
                                op.getResponses()
                                    .get("201")
                                    .addHeaderObject(
                                        "Location",
                                        new Header()
                                            .schema(new StringSchema())
                                            .description("/api/contas-acesso/me"));
                              op.getResponses()
                                  .forEach(
                                      (code, response) -> {
                                        if (!Set.of("400", "404", "409").contains(code)) return;
                                        var message =
                                            code.equals("400")
                                                ? "Dados inválidos"
                                                : code.equals("404")
                                                    ? "Pessoa não encontrada"
                                                    : "Login já cadastrado";
                                        var fields =
                                            code.equals("400")
                                                ? (path.endsWith("/senha")
                                                    ? Map.of(
                                                        "senhaAtual",
                                                        "não deve estar em branco",
                                                        "novaSenha",
                                                        "não deve estar em branco")
                                                    : Map.of(
                                                        "pessoaId",
                                                        "não deve ser nulo",
                                                        "login",
                                                        "não deve estar em branco",
                                                        "senha",
                                                        "não deve estar em branco"))
                                                : Map.of();
                                        response
                                            .getContent()
                                            .get("application/json")
                                            .addExamples(
                                                "Erro" + code,
                                                new Example()
                                                    .value(
                                                        Map.of(
                                                            "timestamp",
                                                            "2026-11-05T12:00:00",
                                                            "status",
                                                            Integer.parseInt(code),
                                                            "error",
                                                            code.equals("400")
                                                                ? "Bad Request"
                                                                : code.equals("404")
                                                                    ? "Not Found"
                                                                    : "Conflict",
                                                            "message",
                                                            message,
                                                            "path",
                                                            path,
                                                            "fields",
                                                            fields)));
                                      });
                            }
                          }));
    };
  }
}
