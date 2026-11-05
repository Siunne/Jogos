package br.com.emilly.jogos.config;

import io.swagger.v3.oas.models.examples.Example;
import java.util.*;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Exemplos do incremento comercial; não modifica os contratos históricos salvos em docs. */
@Configuration
public class OpenApiComercialConfig {
  @Bean
  OpenApiCustomizer exemplosComerciais() {
    return api -> {
      if (api.getPaths() == null) return;
      api.getPaths()
          .forEach(
              (path, item) -> {
                if (!path.matches("/api/(pessoas|clientes|colaboradores|vendas).*")) return;
                item.readOperationsMap()
                    .forEach(
                        (method, operation) ->
                            operation
                                .getResponses()
                                .forEach(
                                    (code, response) -> {
                                      if (Integer.parseInt(code) < 400) return;
                                      String uri =
                                          path.replace("{id}", "1")
                                              .replace("{vendaId}", "1")
                                              .replace("{itemId}", "1");
                                      String message;
                                      Map<String, String> fields = Map.of();
                                      if (code.equals("400")) {
                                        if (method.name().equals("GET") && !path.contains("{"))
                                          message = "page deve estar entre 0 e 2147483647";
                                        else if (method.name().equals("GET")) {
                                          uri = uri.replace("/1", "/abc");
                                          message =
                                              "Method parameter 'id': Failed to convert value of"
                                                  + " type 'java.lang.String' to required type"
                                                  + " 'java.lang.Long'; For input string: \"abc\"";
                                        } else if (path.endsWith("/finalizacao")
                                            || path.endsWith("/cancelamento")
                                            || method.name().equals("DELETE"))
                                          message = "Somente vendas abertas podem ser alteradas";
                                        else if (path.endsWith("/itens")) {
                                          message = "Dados inválidos";
                                          fields =
                                              Map.of("quantidade", "Quantidade mínima é 0.001");
                                        } else if (path.contains("pessoas")
                                            && !path.endsWith("/status")) {
                                          message = "Dados inválidos";
                                          fields = Map.of("cpf", "CPF inválido");
                                        } else message = "JSON inválido ou malformado";
                                      } else if (code.equals("404")) {
                                        message =
                                            path.contains("pessoas")
                                                ? "Pessoa não encontrada"
                                                : path.contains("clientes")
                                                    ? "Cliente não encontrado"
                                                    : path.contains("colaboradores")
                                                        ? "Colaborador não encontrado"
                                                        : "Venda não encontrada";
                                        if (method.name().equals("POST") && !path.contains("{"))
                                          message =
                                              path.contains("vendas")
                                                  ? "Cliente não encontrado"
                                                  : "Pessoa não encontrada";
                                      } else
                                        message =
                                            path.contains("pessoas")
                                                ? "CPF já cadastrado"
                                                : path.contains("clientes")
                                                    ? "Pessoa já possui cadastro de cliente"
                                                    : path.contains("colaboradores")
                                                        ? "Matrícula já cadastrada"
                                                        : "Operação em conflito com a integridade"
                                                              + " dos dados";
                                      var example = new LinkedHashMap<String, Object>();
                                      example.put("timestamp", "2026-11-05T12:00:00");
                                      example.put("status", Integer.parseInt(code));
                                      example.put(
                                          "error",
                                          code.equals("400")
                                              ? "Bad Request"
                                              : code.equals("404") ? "Not Found" : "Conflict");
                                      example.put("message", message);
                                      example.put("path", uri);
                                      example.put("fields", fields);
                                      response
                                          .getContent()
                                          .get("application/json")
                                          .addExamples(
                                              "Erro" + code,
                                              new Example()
                                                  .description(
                                                      "Exemplo de um cenário possível; mensagens de"
                                                          + " Bean Validation usam pt-BR")
                                                  .value(example));
                                    }));
              });
      var schemas = api.getComponents().getSchemas();
      for (String name :
          List.of("PessoaRequest", "ColaboradorRequest", "ColaboradorAtualizacaoRequest")) {
        var schema = schemas.get(name);
        if (schema == null) continue;
        for (String field : List.of("nome", "email", "matricula")) {
          var property =
              (io.swagger.v3.oas.models.media.Schema<?>) schema.getProperties().get(field);
          if (property != null) {
            property.setMinLength(1);
            property.setDescription(
                property.getDescription() + "; não aceita vazio ou somente espaços");
          }
        }
      }
      for (String name :
          List.of("ClienteRequest", "ClienteAtualizacaoRequest", "ClienteResponse")) {
        var schema = schemas.get(name);
        if (schema != null) {
          var p = (io.swagger.v3.oas.models.media.Schema<?>) schema.getProperties().get("telefone");
          if (p != null) p.setTypes(new LinkedHashSet<>(List.of("string", "null")));
        }
      }
    };
  }
}
