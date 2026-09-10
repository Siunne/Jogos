package br.com.emilly.jogos.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GeneroJogoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveCadastrarGeneroERetornar201() throws Exception {
        String json = """
                {
                  "nome": "RPG"
                }
                """;

        mockMvc.perform(post("/api/generos")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void deveRetornar400QuandoNomeForVazio() throws Exception {
        String json = """
                {
                  "nome": ""
                }
                """;

        mockMvc.perform(post("/api/generos")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fields.nome").exists());
    }

    @Test
    void deveRetornar404QuandoGeneroNaoExistir() throws Exception {
        mockMvc.perform(get("/api/generos/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Gênero de jogo não encontrado"));
    }

    @Test
    void deveRetornar409QuandoGeneroForDuplicado() throws Exception {
        String json = """
                {
                  "nome": "Aventura"
                }
                """;

        mockMvc.perform(post("/api/generos")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/generos")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void deveRetornar400QuandoJsonForMalformado() throws Exception {
        String json = """
                {
                  "nome": "RPG"
                """;

        mockMvc.perform(post("/api/generos")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("JSON inválido ou malformado"));
    }
}
