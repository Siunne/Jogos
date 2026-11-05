package br.com.emilly.jogos.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.com.emilly.jogos.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.core.util.Yaml;
import jakarta.validation.Validator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationTest {
  @Autowired MockMvc mvc;
  @Autowired Validator validator;
  @Autowired RequestMappingHandlerMapping mappings;
  private final ObjectMapper json = new ObjectMapper();
  private static final Set<String> METODOS = Set.of("get", "post", "put", "patch", "delete");
  private static final Set<String> METADADOS =
      Set.of(
          "conteudo", "pagina", "tamanho", "totalElementos", "totalPaginas", "primeira", "ultima");

  private JsonNode documento() throws Exception {
    var response =
        mvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andReturn()
            .getResponse();
    return json.readTree(response.getContentAsString());
  }

  private Set<String> chaves(JsonNode node) {
    var values = new HashSet<String>();
    node.fieldNames().forEachRemaining(values::add);
    return values;
  }

  private Set<String> valores(JsonNode array) {
    var values = new HashSet<String>();
    array.forEach(item -> values.add(item.asText()));
    return values;
  }

  private JsonNode schema(JsonNode doc, String nome) {
    var schema = doc.path("components").path("schemas").path(nome);
    assertFalse(schema.isMissingNode(), nome);
    return schema;
  }

  private Map<String, JsonNode> operacoes(JsonNode doc) {
    var result = new TreeMap<String, JsonNode>();
    doc.path("paths")
        .fields()
        .forEachRemaining(
            path ->
                path.getValue()
                    .fields()
                    .forEachRemaining(
                        method -> {
                          if (METODOS.contains(method.getKey()))
                            result.put(method.getKey() + " " + path.getKey(), method.getValue());
                        }));
    return result;
  }

  @Test
  void publicarMetadadosGlobaisSemInventarContatoPessoal() throws Exception {
    var doc = documento();
    assertTrue(doc.path("openapi").asText().matches("3\\.\\d+\\.\\d+"));
    assertEquals("Jogos API", doc.at("/info/title").asText());
    assertEquals("1.0.0", doc.at("/info/version").asText());
    assertEquals(
        "API didática para cadastro de jogos, gêneros e fornecedores, movimentações de estoque e"
            + " pesquisas com filtros, paginação e ordenação. Inclui pessoas, clientes,"
            + " colaboradores e vendas com preço histórico e finalização transacional.",
        doc.at("/info/description").asText());
    assertFalse(doc.at("/info/contact/name").asText().isBlank());
    assertTrue(doc.at("/info/contact/email").isMissingNode());
    assertEquals("https://github.com/Siunne/Jogos", doc.at("/externalDocs/url").asText());
    assertNotEquals(doc.path("openapi").asText(), doc.at("/info/version").asText());
  }

  @Test
  void publicarOperacoesAnterioresEAsVinteNovasSemInventarPatch() throws Exception {
    var operations = operacoes(documento());
    assertEquals(
        Set.of(
            "get /api/health",
            "get /api/generos",
            "post /api/generos",
            "get /api/generos/{id}",
            "put /api/generos/{id}",
            "delete /api/generos/{id}",
            "put /api/generos/{id}/status",
            "get /api/jogos",
            "post /api/jogos",
            "get /api/jogos/{id}",
            "put /api/jogos/{id}",
            "delete /api/jogos/{id}",
            "put /api/jogos/{id}/status",
            "post /api/jogos/{id}/estoque/entradas",
            "post /api/jogos/{id}/estoque/saidas",
            "get /api/fornecedores",
            "post /api/fornecedores",
            "get /api/fornecedores/{id}",
            "post /api/pessoas",
            "get /api/pessoas/{id}",
            "get /api/pessoas",
            "put /api/pessoas/{id}",
            "put /api/pessoas/{id}/status",
            "post /api/clientes",
            "get /api/clientes/{id}",
            "get /api/clientes",
            "put /api/clientes/{id}",
            "post /api/colaboradores",
            "get /api/colaboradores/{id}",
            "get /api/colaboradores",
            "put /api/colaboradores/{id}",
            "post /api/vendas",
            "get /api/vendas/{id}",
            "get /api/vendas",
            "post /api/vendas/{id}/itens",
            "delete /api/vendas/{vendaId}/itens/{itemId}",
            "post /api/vendas/{id}/finalizacao",
            "post /api/vendas/{id}/cancelamento"),
        operations.keySet());
    var reais = new HashSet<String>();
    mappings
        .getHandlerMethods()
        .forEach(
            (mapping, handler) ->
                mapping.getPatternValues().stream()
                    .filter(path -> path.startsWith("/api/"))
                    .forEach(
                        path ->
                            mapping
                                .getMethodsCondition()
                                .getMethods()
                                .forEach(
                                    method ->
                                        reais.add(
                                            method.name().toLowerCase(Locale.ROOT) + " " + path))));
    assertEquals(reais, operations.keySet());
  }

  @Test
  void todasOperacoesPossuemIntencaoETagCorreta() throws Exception {
    var doc = documento();
    assertEquals(
        Set.of(
            "Jogos",
            "Gêneros de jogos",
            "Fornecedores",
            "Infraestrutura",
            "Pessoas",
            "Clientes",
            "Colaboradores",
            "Vendas"),
        valores(json.valueToTree(doc.path("tags").findValuesAsText("name"))));
    var ids = new HashSet<String>();
    operacoes(doc)
        .forEach(
            (path, op) -> {
              assertFalse(op.path("summary").asText().isBlank(), path);
              assertFalse(op.path("description").asText().isBlank(), path);
              assertTrue(ids.add(op.path("operationId").asText()), path);
              String tag =
                  path.contains("/generos")
                      ? "Gêneros de jogos"
                      : path.contains("/jogos")
                          ? "Jogos"
                          : path.contains("/fornecedores")
                              ? "Fornecedores"
                              : path.contains("/pessoas")
                                  ? "Pessoas"
                                  : path.contains("/clientes")
                                      ? "Clientes"
                                      : path.contains("/colaboradores")
                                          ? "Colaboradores"
                                          : path.contains("/vendas") ? "Vendas" : "Infraestrutura";
              assertEquals(Set.of(tag), valores(op.path("tags")), path);
            });
  }

  @Test
  void respostasDeErroReferenciamApiErrorEExclusoesNaoPossuemCorpo() throws Exception {
    var doc = documento();
    Map<String, Set<String>> expected =
        Map.ofEntries(
            Map.entry("post /api/generos", Set.of("201", "400", "409")),
            Map.entry("get /api/generos/{id}", Set.of("200", "400", "404")),
            Map.entry("get /api/generos", Set.of("200", "400")),
            Map.entry("put /api/generos/{id}", Set.of("200", "400", "404", "409")),
            Map.entry("put /api/generos/{id}/status", Set.of("200", "400", "404")),
            Map.entry("delete /api/generos/{id}", Set.of("204", "400", "404", "409")),
            Map.entry("post /api/jogos", Set.of("201", "400", "404", "409")),
            Map.entry("get /api/jogos/{id}", Set.of("200", "400", "404")),
            Map.entry("get /api/jogos", Set.of("200", "400")),
            Map.entry("put /api/jogos/{id}", Set.of("200", "400", "404", "409")),
            Map.entry("put /api/jogos/{id}/status", Set.of("200", "400", "404")),
            Map.entry("post /api/jogos/{id}/estoque/entradas", Set.of("200", "400", "404", "409")),
            Map.entry("post /api/jogos/{id}/estoque/saidas", Set.of("200", "400", "404", "409")),
            Map.entry("delete /api/jogos/{id}", Set.of("204", "400", "404", "409")),
            Map.entry("post /api/fornecedores", Set.of("201", "400", "409")),
            Map.entry("get /api/fornecedores", Set.of("200")),
            Map.entry("get /api/fornecedores/{id}", Set.of("200", "400", "404")),
            Map.entry("get /api/health", Set.of("200")),
            Map.entry("post /api/pessoas", Set.of("201", "400", "409")),
            Map.entry("get /api/pessoas/{id}", Set.of("200", "400", "404")),
            Map.entry("get /api/pessoas", Set.of("200", "400")),
            Map.entry("put /api/pessoas/{id}", Set.of("200", "400", "404", "409")),
            Map.entry("put /api/pessoas/{id}/status", Set.of("200", "400", "404")),
            Map.entry("post /api/clientes", Set.of("201", "400", "404", "409")),
            Map.entry("get /api/clientes/{id}", Set.of("200", "400", "404")),
            Map.entry("get /api/clientes", Set.of("200", "400")),
            Map.entry("put /api/clientes/{id}", Set.of("200", "400", "404")),
            Map.entry("post /api/colaboradores", Set.of("201", "400", "404", "409")),
            Map.entry("get /api/colaboradores/{id}", Set.of("200", "400", "404")),
            Map.entry("get /api/colaboradores", Set.of("200", "400")),
            Map.entry("put /api/colaboradores/{id}", Set.of("200", "400", "404", "409")),
            Map.entry("post /api/vendas", Set.of("201", "400", "404")),
            Map.entry("get /api/vendas/{id}", Set.of("200", "400", "404")),
            Map.entry("get /api/vendas", Set.of("200", "400")),
            Map.entry("post /api/vendas/{id}/itens", Set.of("200", "400", "404", "409")),
            Map.entry("delete /api/vendas/{vendaId}/itens/{itemId}", Set.of("204", "400", "404", "409")),
            Map.entry("post /api/vendas/{id}/finalizacao", Set.of("200", "400", "404", "409")),
            Map.entry("post /api/vendas/{id}/cancelamento", Set.of("200", "400", "404", "409")));
    operacoes(doc)
        .forEach(
            (path, op) -> {
              assertEquals(expected.get(path), chaves(op.path("responses")), path);
              op.path("responses")
                  .fields()
                  .forEachRemaining(
                      response -> {
                        var value = response.getValue();
                        if (Integer.parseInt(response.getKey()) >= 400) {
                          assertEquals(
                              "#/components/schemas/ApiError",
                              value.at("/content/application~1json/schema/$ref").asText(),
                              path);
                        } else if (response.getKey().equals("204")) {
                          assertTrue(
                              value.path("content").isMissingNode()
                                  || value.path("content").isEmpty(),
                              path);
                        } else if (!path.contains("health")) {
                          assertFalse(
                              value.at("/content/application~1json/schema").isMissingNode(), path);
                        }
                      });
              if (op.path("responses").has("201")) {
                assertEquals(
                    "string", op.at("/responses/201/headers/Location/schema/type").asText());
              }
            });
    assertEquals(
        "string",
        doc.at("/paths/~1api~1health/get/responses/200/content/text~1plain/schema/type").asText());
  }

  @Test
  void schemasDeEntradaESaidaSaoCompletosEExplicados() throws Exception {
    var doc = documento();
    for (String nome :
        List.of(
            "JogoRequest",
            "JogoAtualizacaoRequest",
            "JogoResponse",
            "GeneroJogoRequest",
            "GeneroJogoAtualizacaoRequest",
            "GeneroJogoResponse",
            "FornecedorRequest",
            "FornecedorResponse",
            "StatusRequest",
            "MovimentacaoEstoqueRequest",
            "ApiError",
            "PaginaResponseJogoResponse",
            "PaginaResponseGeneroJogoResponse")) {
      var value = schema(doc, nome);
      assertFalse(value.path("description").asText().isBlank(), nome);
      value
          .path("properties")
          .fields()
          .forEachRemaining(
              prop ->
                  assertFalse(
                      prop.getValue().path("description").asText().isBlank(),
                      nome + ":" + prop.getKey()));
    }
  }

  @Test
  void camposProtegidosNaoSaoRequisitadosESaoExplicados() throws Exception {
    var doc = documento();
    assertEquals(
        Set.of(
            "codigo",
            "nome",
            "unidadesDisponiveis",
            "preco",
            "estoqueMinimo",
            "generoId",
            "fornecedorId"),
        chaves(schema(doc, "JogoRequest").path("properties")));
    assertEquals(
        Set.of("nome", "preco", "estoqueMinimo", "generoId", "fornecedorId"),
        chaves(schema(doc, "JogoAtualizacaoRequest").path("properties")));
    assertTrue(
        doc.at("/paths/~1api~1jogos/post/description").asText().contains("definida pelo servidor"));
    assertEquals(
        "date", schema(doc, "JogoResponse").at("/properties/dataCadastro/format").asText());
    assertTrue(schema(doc, "JogoResponse").at("/properties/dataCadastro/readOnly").asBoolean());
  }

  @Test
  void fornecedorOpcionalAceitaNullEObrigatoriosContinuamObrigatorios() throws Exception {
    var doc = documento();
    for (String nome : List.of("JogoRequest", "JogoAtualizacaoRequest", "JogoResponse")) {
      var value = schema(doc, nome);
      assertFalse(valores(value.path("required")).contains("fornecedorId"), nome);
      assertEquals(
          Set.of("integer", "null"), valores(value.at("/properties/fornecedorId/type")), nome);
      assertTrue(valores(value.path("required")).contains("generoId"), nome);
    }
    assertEquals(
        Set.of("codigo", "nome", "unidadesDisponiveis", "preco", "estoqueMinimo", "generoId"),
        valores(schema(doc, "JogoRequest").path("required")));
    assertEquals(
        Set.of("nome", "preco", "estoqueMinimo", "generoId"),
        valores(schema(doc, "JogoAtualizacaoRequest").path("required")));
  }

  @Test
  void paginasPublicamExatamenteSeteCamposETipoDosItens() throws Exception {
    var doc = documento();
    for (String tipo : List.of("JogoResponse", "GeneroJogoResponse")) {
      var value = schema(doc, "PaginaResponse" + tipo);
      assertEquals(METADADOS, chaves(value.path("properties")));
      assertEquals(METADADOS, valores(value.path("required")));
      assertEquals(
          "#/components/schemas/" + tipo, value.at("/properties/conteudo/items/$ref").asText());
    }
    assertEquals(
        "#/components/schemas/PaginaResponseJogoResponse",
        doc.at("/paths/~1api~1jogos/get/responses/200/content/application~1json/schema/$ref")
            .asText());
  }

  @Test
  void parametrosReaisDeFiltrosEPaginacaoSaoPublicadosSemPageableObrigatorio() throws Exception {
    var doc = documento();
    for (String recurso : List.of("jogos", "generos")) {
      var parameters = new HashMap<String, JsonNode>();
      doc.path("paths")
          .path("/api/" + recurso)
          .path("get")
          .path("parameters")
          .forEach(param -> parameters.put(param.path("name").asText(), param));
      assertEquals(
          recurso.equals("jogos")
              ? Set.of(
                  "nome",
                  "status",
                  "generoId",
                  "fornecedorId",
                  "abaixoEstoqueMinimo",
                  "page",
                  "size",
                  "sort")
              : Set.of("nome", "status", "page", "size", "sort"),
          parameters.keySet());
      parameters.forEach(
          (name, param) -> {
            assertEquals("query", param.path("in").asText(), name);
            assertFalse(param.path("required").asBoolean(), name);
            assertFalse(param.path("description").asText().isBlank(), name);
          });
      assertEquals(0, parameters.get("page").at("/schema/default").asInt());
      assertEquals(0, parameters.get("page").at("/schema/minimum").asInt());
      assertEquals(20, parameters.get("size").at("/schema/default").asInt());
      assertEquals(100, parameters.get("size").at("/schema/maximum").asInt());
      assertEquals(1, parameters.get("size").at("/schema/minimum").asInt());
      assertEquals("array", parameters.get("sort").at("/schema/type").asText());
      assertTrue(parameters.get("sort").path("example").isArray());
    }
  }

  @Test
  void apiErrorPreservaCamposETiposReaisSemInventarInstant() throws Exception {
    var value = schema(documento(), "ApiError");
    assertEquals(
        Set.of("timestamp", "status", "error", "message", "path", "fields"),
        chaves(value.path("properties")));
    assertEquals("local-date-time", value.at("/properties/timestamp/format").asText());
    assertEquals("integer", value.at("/properties/status/type").asText());
    assertEquals("object", value.at("/properties/fields/type").asText());
    assertEquals("string", value.at("/properties/fields/additionalProperties/type").asText());
  }

  @Test
  void exemplosDeTodosOsRequestsRespeitamBeanValidation() throws Exception {
    var doc = documento();
    for (Class<?> tipo :
        List.of(
            JogoRequest.class,
            JogoAtualizacaoRequest.class,
            GeneroJogoRequest.class,
            GeneroJogoAtualizacaoRequest.class,
            FornecedorRequest.class,
            StatusRequest.class,
            MovimentacaoEstoqueRequest.class)) {
      ObjectNode example = json.createObjectNode();
      schema(doc, tipo.getSimpleName())
          .path("properties")
          .fields()
          .forEachRemaining(
              field -> {
                assertTrue(
                    field.getValue().has("example"), tipo.getSimpleName() + ":" + field.getKey());
                example.set(field.getKey(), field.getValue().path("example"));
              });
      Object dto = json.treeToValue(example, tipo);
      assertTrue(
          validator.validate(dto).isEmpty(), tipo.getSimpleName() + ":" + validator.validate(dto));
    }
  }

  @Test
  void restricoesEnumsETamanhosCorrespondemAoDominio() throws Exception {
    var doc = documento();
    assertEquals(100, schema(doc, "GeneroJogoRequest").at("/properties/nome/maxLength").asInt());
    assertEquals(
        120, schema(doc, "GeneroJogoAtualizacaoRequest").at("/properties/nome/maxLength").asInt());
    assertEquals(
        150, schema(doc, "JogoAtualizacaoRequest").at("/properties/nome/maxLength").asInt());
    assertEquals(
        Set.of("ATIVO", "INATIVO"),
        valores(schema(doc, "StatusRequest").at("/properties/status/enum")));
    assertTrue(
        schema(doc, "MovimentacaoEstoqueRequest")
            .at("/properties/quantidade")
            .has("exclusiveMinimum"));
    assertEquals(
        0,
        schema(doc, "MovimentacaoEstoqueRequest")
            .at("/properties/quantidade/exclusiveMinimum")
            .asInt());
  }

  @Test
  void yamlPodeSerInterpretadoECorrespondeAoJson() throws Exception {
    var doc = documento();
    var response =
        mvc.perform(get("/v3/api-docs.yaml")).andExpect(status().isOk()).andReturn().getResponse();
    var yaml = Yaml.mapper().readTree(response.getContentAsByteArray());
    assertEquals(doc.path("info"), yaml.path("info"));
    assertEquals(doc.path("paths"), yaml.path("paths"));
    assertEquals(doc.path("components"), yaml.path("components"));
  }

  @Test
  void swaggerUiDisponibilizaHtmlJavascriptEConfiguracao() throws Exception {
    mvc.perform(get("/swagger-ui.html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/swagger-ui/index.html"));
    var html =
        mvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertTrue(html.contains("swagger-ui-bundle.js"));
    mvc.perform(get("/swagger-ui/swagger-ui-bundle.js")).andExpect(status().isOk());
    var config =
        json.readTree(
            mvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
    assertEquals("method", config.path("operationsSorter").asText());
    assertEquals("alpha", config.path("tagsSorter").asText());
    assertEquals("/v3/api-docs", config.path("url").asText());
  }

  @Test
  void exemplosDeErrosCorrespondemAoCodigoDaResposta() throws Exception {
    operacoes(documento())
        .forEach(
            (path, op) ->
                op.path("responses")
                    .fields()
                    .forEachRemaining(
                        response -> {
                          int code = Integer.parseInt(response.getKey());
                          if (code < 400) return;
                          var example =
                              response
                                  .getValue()
                                  .at("/content/application~1json/examples/Erro" + code + "/value");
                          assertFalse(example.isMissingNode(), path);
                          assertEquals(code, example.path("status").asInt(), path);
                          assertEquals(
                              code == 400 ? "Bad Request" : code == 404 ? "Not Found" : "Conflict",
                              example.path("error").asText());
                          assertEquals(
                              Set.of("timestamp", "status", "error", "message", "path", "fields"),
                              chaves(example));
                          assertTrue(example.path("fields").isObject());
                        }));
  }

  @Test
  void textosNotBlankNoContratoRejeitamVazioEEspacosComoOsDtos() throws Exception {
    var doc = documento();
    for (String nome :
        List.of(
            "GeneroJogoRequest",
            "GeneroJogoAtualizacaoRequest",
            "FornecedorRequest",
            "JogoRequest",
            "JogoAtualizacaoRequest")) {
      var properties = schema(doc, nome).path("properties");
      for (String campo : List.of("nome", "codigo", "razaoSocial")) {
        if (!properties.has(campo)) continue;
        var propriedade = properties.path(campo);
        assertEquals(1, propriedade.path("minLength").asInt(), nome + campo);
        var pattern = java.util.regex.Pattern.compile(propriedade.path("pattern").asText());
        for (String texto : List.of("", " ", "\t\n", "\u2003"))
          assertFalse(pattern.matcher(texto).find(), nome + campo);
        assertTrue(pattern.matcher(" Aventura ").find());
        assertTrue(propriedade.path("description").asText().contains("somente por espaços"));
      }
    }
    for (String texto : List.of("", " ", "\t\n", "\u2003", " Aventura ", "\u00a0")) {
      for (Object dto :
          List.of(
              new GeneroJogoRequest(texto),
              new GeneroJogoAtualizacaoRequest(texto),
              new FornecedorRequest(texto, "11222333000181"))) {
        String tipo = dto.getClass().getSimpleName();
        String campo = dto instanceof FornecedorRequest ? "razaoSocial" : "nome";
        var pattern =
            java.util.regex.Pattern.compile(
                schema(doc, tipo).path("properties").path(campo).path("pattern").asText());
        assertEquals(
            validator.validate(dto).isEmpty(), pattern.matcher(texto).find(), tipo + texto);
      }
    }
  }

  @Test
  void exemplos400DasOperacoesCorrespondemAoHandlerReal() throws Exception {
    for (var entry : operacoes(documento()).entrySet()) {
      var op = entry.getValue();
      if (entry.getKey().matches(".* /api/(pessoas|clientes|colaboradores|vendas).*")) continue;
      if (!op.path("responses").has("400")) continue;
      var exemplo = op.at("/responses/400/content/application~1json/examples/Erro400/value");
      String id = op.path("operationId").asText();
      String[] route = entry.getKey().split(" ", 2);
      String path = route[1].replace("{id}", "1");
      var req =
          request(org.springframework.http.HttpMethod.valueOf(route[0].toUpperCase()), path)
              .contentType("application/json");
      if (id.startsWith("pesquisar")) req.param("page", "-1");
      else if (id.startsWith("consultar") || id.startsWith("excluir")) {
        req =
            request(
                org.springframework.http.HttpMethod.valueOf(route[0].toUpperCase()),
                route[1].replace("{id}", "abc"));
      } else {
        String payload;
        if (id.startsWith("alterarStatus")) payload = "{\"status\":\"INVALIDO\"}";
        else if (id.equals("entradaEstoque") || id.equals("saidaEstoque"))
          payload = "{\"quantidade\":0}";
        else if (id.equals("cadastrarFornecedor"))
          payload = "{\"razaoSocial\":\"\",\"cnpj\":\"11222333000181\"}";
        else if (id.equals("cadastrarJogo"))
          payload =
              "{\"codigo\":\"TESTE\",\"nome\":\"\",\"unidadesDisponiveis\":10,\"preco\":1,\"estoqueMinimo\":1,\"generoId\":1}";
        else if (id.equals("alterarJogo"))
          payload = "{\"nome\":\"\",\"preco\":1,\"estoqueMinimo\":1,\"generoId\":1}";
        else payload = "{\"nome\":\"\"}";
        req.content(payload);
      }
      // Exemplos em português: o Hibernate Validator localiza mensagens pelo Accept-Language.
      var atual =
          json.readTree(
              mvc.perform(req.locale(Locale.forLanguageTag("pt-BR")))
                  .andExpect(status().isBadRequest())
                  .andReturn()
                  .getResponse()
                  .getContentAsString());
      for (String campo : List.of("status", "error", "message", "path", "fields"))
        assertEquals(atual.path(campo), exemplo.path(campo), id + ":" + campo);
    }
  }

  @Test
  void exemplo404DeCadastroRefleteReferenciaInexistenteEmVezDeJogo() throws Exception {
    var exemplo =
        documento()
            .at(
                "/paths/~1api~1jogos/post/responses/404/content/application~1json/examples/Erro404/value");
    String payload =
        "{\"codigo\":\"A09-"
            + UUID.randomUUID()
            + "\",\"nome\":\"Reino das Estrelas\","
            + "\"unidadesDisponiveis\":10,\"preco\":149.90,\"estoqueMinimo\":3,\"generoId\":9223372036854775807}";
    var atual =
        json.readTree(
            mvc.perform(
                    request(org.springframework.http.HttpMethod.POST, "/api/jogos")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString());
    for (String campo : List.of("status", "error", "message", "path", "fields"))
      assertEquals(atual.path(campo), exemplo.path(campo), campo);
    assertEquals("Gênero de jogo não encontrado", exemplo.path("message").asText());
  }

  @Test
  void contratoExportadoEReproduzivelETodasAsReferenciasResolvem() throws Exception {
    var doc = documento();
    var exportado =
        Yaml.mapper().readTree(Files.readAllBytes(Path.of("docs/aula-11-openapi.yaml")));
    // A URL de servidor depende da porta usada na captura; o contrato não depende dela.
    for (String part : List.of("openapi", "info", "paths", "components", "tags", "externalDocs")) {
      assertEquals(doc.path(part), exportado.path(part), part);
    }
    verificarReferencias(doc, doc);
  }

  private void verificarReferencias(JsonNode root, JsonNode node) {
    if (node.has("$ref")) {
      String ref = node.path("$ref").asText();
      assertTrue(ref.startsWith("#/components/schemas/"), ref);
      assertFalse(root.at(ref.substring(1)).isMissingNode(), ref);
    }
    node.forEach(child -> verificarReferencias(root, child));
  }
}
