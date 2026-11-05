package br.com.emilly.jogos.api;

import br.com.emilly.jogos.config.OpenApiConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OpenApiProfilesTest {
    // Não há component scan de repositories/Services nem configuração de DataSource.
    // São carregados os arquivos application*.properties reais e a infraestrutura documental.
    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration(excludeName = {
            "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
            "org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration"
    })
    @Import({OpenApiConfig.class, HealthController.class})
    static class DocumentacaoSemBanco {}

    private void verificar(String profile, boolean habilitada) {
        new WebApplicationContextRunner()
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .withPropertyValues("spring.profiles.active=" + profile)
                .withUserConfiguration(DocumentacaoSemBanco.class)
                .run(context -> {
                    assertThat(context).hasNotFailed().doesNotHaveBean(DataSource.class);
                    assertThat(context.getEnvironment().getActiveProfiles()).containsExactly(profile);
                    assertThat(context.getEnvironment().getProperty("spring.jpa.show-sql", Boolean.class, false)).isFalse();
                    assertThat(context.getEnvironment().getProperty("springdoc.api-docs.enabled", Boolean.class, true)).isEqualTo(habilitada);
                    assertThat(context.getEnvironment().getProperty("springdoc.swagger-ui.enabled", Boolean.class, true)).isEqualTo(habilitada);
                    var mvc = MockMvcBuilders.webAppContextSetup(context).build();
                    mvc.perform(get("/api/health")).andExpect(status().isOk());
                    mvc.perform(get("/v3/api-docs")).andExpect(habilitada ? status().isOk() : status().isNotFound());
                    mvc.perform(get("/v3/api-docs.yaml")).andExpect(habilitada ? status().isOk() : status().isNotFound());
                    mvc.perform(get("/swagger-ui.html")).andExpect(habilitada ? status().is3xxRedirection() : status().isNotFound());
                    mvc.perform(get("/swagger-ui/index.html")).andExpect(habilitada ? status().isOk() : status().isNotFound());
                    mvc.perform(get("/v3/api-docs/swagger-config")).andExpect(habilitada ? status().isOk() : status().isNotFound());
                });
    }

    @Test void desenvolvimentoPublicaDocumentacaoSemAcessarSeuBanco() {
        verificar("dev", true);
    }
    @Test void testePublicaDocumentacao() {
        verificar("test", true);
    }
    @Test void producaoNaoPublicaDocumentacaoENaoAbreConexao() {
        verificar("prod", false);
    }
}
