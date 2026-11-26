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
              "bearerAuth",
              new SecurityScheme()
                  .type(SecurityScheme.Type.HTTP)
                  .scheme("bearer")
                  .bearerFormat("JWT")
                  .description(
                      "JWT Bearer HS256 stateless. Use HTTPS/TLS; payload não é confidencial."));
      api.setSecurity(List.of(new SecurityRequirement().addList("bearerAuth")));
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
      api.getComponents().addSchemas("ErroAutorizacao",
          new ObjectSchema()
              .addProperty("status", new IntegerSchema().example(403))
              .addProperty("message", new StringSchema().example("Acesso negado"))
              .required(List.of("status", "message")));
      var schemas = api.getComponents().getSchemas();
      var perfis = new StringSchema();
      perfis.setEnum(List.of("OPERADOR", "ADMINISTRADOR"));
      schemas.get("PerfisContaRequest").addProperty("perfis",
          new ArraySchema().items(perfis).minItems(1)).required(List.of("perfis"));
      schemas.remove("JsonNode");
      api.getPaths()
          .forEach(
              (path, item) ->
                  item.readOperationsMap()
                      .forEach(
                          (method, op) -> {
                            boolean publica =
                                method == PathItem.HttpMethod.GET && path.equals("/api/health")
                                    || method == PathItem.HttpMethod.POST
                                        && path.equals("/api/autenticacao/login");
                            if (publica) op.setSecurity(List.of());
                            else {
                              op.setSecurity(
                                  List.of(new SecurityRequirement().addList("bearerAuth")));
                              op.getResponses()
                                  .addApiResponse(
                                      "401",
                                      new ApiResponse()
                                          .description(
                                              "Token ausente, inválido, alterado ou expirado;"
                                                  + " bloqueio não revoga tokens emitidos")
                                          .addHeaderObject(
                                              "WWW-Authenticate",
                                              new Header()
                                                  .schema(new StringSchema())
                                                  .description("Desafio Bearer"))
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
                            boolean administrativa =
                                method == PathItem.HttpMethod.DELETE
                                    || method == PathItem.HttpMethod.POST
                                        && path.equals("/api/contas-acesso")
                                    || method == PathItem.HttpMethod.PUT
                                        && Set.of("/api/contas-acesso/{id}/perfis",
                                            "/api/contas-acesso/{id}/status",
                                            "/api/pessoas/{id}/status").contains(path);
                            if (administrativa) {
                              String descricao = op.getDescription();
                              if (descricao == null || !descricao.contains("Exige ADMINISTRADOR."))
                                op.setDescription("Exige ADMINISTRADOR. " + (descricao == null ? "" : descricao));
                              op.getResponses().addApiResponse("403",
                                  new ApiResponse().description("Perfil insuficiente")
                                      .content(new Content().addMediaType("application/json",
                                          new MediaType().schema(new Schema<>()
                                              .$ref("#/components/schemas/ErroAutorizacao"))
                                              .addExamples("Erro403",
                                                  new Example().value(Map.of("status", 403,
                                                      "message", "Acesso negado"))))));
                            }
                            if (path.equals("/api/autenticacao/login")) {
                              for (String code : List.of("400", "401")) {
                                op.getResponses()
                                    .get(code)
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
                                                        : "Unauthorized",
                                                    "message",
                                                    code.equals("400")
                                                        ? "Dados inválidos"
                                                        : "Login ou senha inválidos",
                                                    "path",
                                                    path,
                                                    "fields",
                                                    code.equals("400")
                                                        ? Map.of(
                                                            "login",
                                                            "não deve estar em branco",
                                                            "senha",
                                                            "não deve estar em branco")
                                                        : Map.of())));
                              }
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
                                                ? path.endsWith("/perfis") ? "Informe ao menos um perfil"
                                                    : path.endsWith("/status") ? "Status inválido" : "Dados inválidos"
                                                : code.equals("404")
                                                    ? path.contains("{id}") ? "Conta não encontrada" : "Pessoa não encontrada"
                                                    : "Login já cadastrado";
                                        var fields =
                                            code.equals("400") && !path.contains("{id}")
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
                                                            path.replace("{id}", "1"),
                                                            "fields",
                                                            fields)));
                                      });
                            }
                          }));
    };
  }
}
