package br.com.emilly.jogos.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@org.springframework.security.test.context.support.WithMockUser(username="operador-teste", roles="OPERADOR")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiConcorrenciaTest {
  @Autowired MockMvc mvc;

  @Test
  void finalizacaoEMovimentosPublicamPoliticaDeConcorrenciaSemNovasRotas() throws Exception {
    var doc =
        new ObjectMapper()
            .readTree(
                mvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString());
    assertEquals(27, doc.path("paths").size());
    assertEquals(34, doc.at("/components/schemas").size());
    assertEquals(9, doc.path("tags").size());
    for (String path :
        List.of(
            "/api/vendas/{id}/finalizacao",
            "/api/jogos/{id}/estoque/entradas",
            "/api/jogos/{id}/estoque/saidas")) {
      var op = doc.path("paths").path(path).path("post");
      assertTrue(op.path("description").asText().contains("bloqueio pessimista"), path);
      assertEquals(
          "#/components/schemas/ApiError",
          op.at("/responses/409/content/application~1json/schema/$ref").asText());
      assertTrue(op.path("responses").has("400"));
    }
    var description =
        doc.path("paths")
            .path("/api/vendas/{id}/finalizacao")
            .path("post")
            .path("description")
            .asText();
    assertTrue(description.contains("PESSIMISTIC_WRITE"));
    assertTrue(description.contains("ID crescente"));
    assertFalse(description.contains("Sem locks"));
  }
}
