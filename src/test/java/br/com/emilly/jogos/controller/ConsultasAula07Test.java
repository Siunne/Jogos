package br.com.emilly.jogos.controller;

import br.com.emilly.jogos.application.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class ConsultasAula07Test {
    @Autowired MockMvc mvc;
    @Autowired GeneroJogoService generos;
    @Autowired FornecedorService fornecedores;

    @Test void listarGeneros() throws Exception {
        var genero = generos.cadastrar("Consulta RPG");
        mvc.perform(get("/api/generos").param("nome", "Consulta RPG")).andExpect(status().isOk())
            .andExpect(jsonPath("$.conteudo[*].id", org.hamcrest.Matchers.hasItem(genero.getId().intValue())));
    }
    @Test void consultarGenero() throws Exception {
        var genero = generos.cadastrar("Consulta RPG");
        mvc.perform(get("/api/generos/{id}", genero.getId()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Consulta RPG"));
    }
    @Test void listarFornecedores() throws Exception {
        var fornecedor = fornecedores.cadastrar("Distribuidora consulta", "11223344000155");
        mvc.perform(get("/api/fornecedores"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].id", org.hamcrest.Matchers.hasItem(fornecedor.getId().intValue())));
    }
    @Test void consultarFornecedor() throws Exception {
        var fornecedor = fornecedores.cadastrar("Distribuidora consulta", "11223344000155");
        mvc.perform(get("/api/fornecedores/{id}", fornecedor.getId()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.cnpj").value("11223344000155"));
    }
    @Test void fornecedorInexistente() throws Exception {
        mvc.perform(get("/api/fornecedores/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
    }
}
