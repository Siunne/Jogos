package br.com.emilly.jogos.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.*;
import io.swagger.v3.core.util.Yaml;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiAutenticacaoTest {
  @Autowired MockMvc mvc;

  JsonNode doc() throws Exception {
    return new ObjectMapper()
        .readTree(
            mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
  }

  @Test
  void padroesServidosProibemNulSemProibirUnicodeValido() throws Exception {
    var d = doc();
    for (var entry :
        Map.of(
                "ContaAcessoRequest",
                List.of("senha"),
                "AlteracaoSenhaRequest",
                List.of("senhaAtual", "novaSenha"))
            .entrySet()) {
      for (String campo : entry.getValue()) {
        var schema = d.at("/components/schemas/" + entry.getKey() + "/properties/" + campo);
        var pattern = java.util.regex.Pattern.compile(schema.path("pattern").asText());
        assertTrue(schema.path("description").asText().contains("U+0000"));
        for (String s :
            List.of("\0".repeat(12), "\0".repeat(13), "\0".repeat(20), "texto-validado\0fim"))
          assertFalse(pattern.matcher(s).find());
        for (String s :
            List.of(
                "á".repeat(12),
                "😀".repeat(12),
                "\u00a0".repeat(12),
                "Frase\u001b de teste válida")) assertTrue(pattern.matcher(s).find());
      }
    }
  }

  @Test
  void somenteOperacoesPublicasNaoExigemBasic() throws Exception {
    var d = doc();
    assertEquals("http", d.at("/components/securitySchemes/basicAuth/type").asText());
    assertEquals("basic", d.at("/components/securitySchemes/basicAuth/scheme").asText());
    int[] total = {0};
    d.path("paths")
        .fields()
        .forEachRemaining(
            p ->
                p.getValue()
                    .fields()
                    .forEachRemaining(
                        o -> {
                          if (!o.getValue().has("operationId")) return;
                          total[0]++;
                          boolean publica =
                              o.getKey().equals("get") && p.getKey().equals("/api/health")
                                  || o.getKey().equals("post")
                                      && Set.of("/api/pessoas", "/api/contas-acesso")
                                          .contains(p.getKey());
                          if (publica) assertTrue(o.getValue().path("security").isEmpty());
                          else {
                            assertTrue(o.getValue().path("security").get(0).has("basicAuth"));
                            assertEquals(
                                "#/components/schemas/ErroAutenticacao",
                                o.getValue()
                                    .at("/responses/401/content/application~1json/schema/$ref")
                                    .asText());
                          }
                        }));
    assertEquals(41, total[0]);
  }

  @Test
  void contratosNaoAdmitemPerfilStatusOuHashNaEntrada() throws Exception {
    var d = doc();
    var p = d.at("/components/schemas/ContaAcessoRequest/properties");
    assertEquals(3, p.size());
    for (String k : List.of("pessoaId", "login", "senha")) assertTrue(p.has(k));
    assertTrue(p.path("senha").path("writeOnly").asBoolean());
    assertEquals("password", p.path("senha").path("format").asText());
    var r = d.at("/components/schemas/ContaAcessoResponse/properties");
    for (String k : List.of("senha", "senhaHash", "senha_hash")) assertFalse(r.has(k));
    assertTrue(
        d.at("/paths/~1api~1contas-acesso~1me~1senha/put/responses/204/content").isMissingNode());
  }

  @Test
  void jsonYamlServidosEEntregaSaoEquivalentes() throws Exception {
    var d = doc();
    var y =
        Yaml.mapper()
            .readTree(
                mvc.perform(get("/v3/api-docs.yaml"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsByteArray());
    var e = Yaml.mapper().readTree(Files.readAllBytes(Path.of("docs/aula-12-openapi.yaml")));
    for (String k : List.of("info", "paths", "components", "tags", "security")) {
      assertEquals(d.path(k), y.path(k));
      assertEquals(d.path(k), e.path(k));
    }
    assertEquals(27, d.path("paths").size());
    assertEquals(34, d.at("/components/schemas").size());
    assertEquals(9, d.path("tags").size());
  }
}
