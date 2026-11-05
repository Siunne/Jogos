package br.com.emilly.jogos.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import java.math.BigDecimal;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  public OpenAPI jogosOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Jogos API")
                .version("1.0.0")
                .description(
                    "API didática para cadastro de jogos, gêneros e fornecedores, movimentações de"
                        + " estoque e pesquisas com filtros, paginação e ordenação. Inclui pessoas,"
                        + " clientes, colaboradores e vendas com preço histórico e finalização"
                        + " transacional.")
                .contact(new Contact().name("Projeto acadêmico Jogos — Sistemas de Informação")))
        .externalDocs(
            new ExternalDocumentation()
                .description("Repositório e documentação do projeto Jogos")
                .url("https://github.com/Siunne/Jogos"));
  }

  @Bean
  public OpenApiCustomizer textosObrigatorios() {
    return openAPI -> {
      if (openAPI.getComponents() == null || openAPI.getComponents().getSchemas() == null) return;
      // @Size(min=0) pode sobrepor o mínimo inferido de @NotBlank.
      // Combina os espaços de @NotBlank com caracteres removidos pela normalização trim.
      String naoBranco = SchemaPatterns.NORMALIZADO_NAO_BRANCO;
      var schemas = openAPI.getComponents().getSchemas();
      for (String nome :
          java.util.List.of(
              "GeneroJogoRequest",
              "GeneroJogoAtualizacaoRequest",
              "FornecedorRequest",
              "JogoRequest",
              "JogoAtualizacaoRequest")) {
        var schema = schemas.get(nome);
        if (schema == null || schema.getProperties() == null) continue;
        for (String campo : java.util.List.of("nome", "codigo", "razaoSocial")) {
          var propriedade =
              (io.swagger.v3.oas.models.media.Schema<?>) schema.getProperties().get(campo);
          if (propriedade == null) continue;
          propriedade.setMinLength(1);
          propriedade.setPattern(naoBranco);
          propriedade.setDescription(
              propriedade.getDescription()
                  + "; obrigatório, não aceita texto vazio ou formado somente por espaços em"
                  + " branco, inclusive após normalização trim");
        }
      }
    };
  }

  @Bean
  public OpenApiCustomizer quantidadeEstritamentePositiva() {
    return openAPI -> {
      if (openAPI.getComponents() == null || openAPI.getComponents().getSchemas() == null) return;
      var movimento = openAPI.getComponents().getSchemas().get("MovimentacaoEstoqueRequest");
      if (movimento != null) {
        var quantidade =
            (io.swagger.v3.oas.models.media.Schema<?>) movimento.getProperties().get("quantidade");
        // Em OpenAPI 3.1 o limite exclusivo é numérico. O zero é omitido
        // pelo atributo int da anotação nesta versão do swagger-core.
        quantidade.setMinimum(null);
        quantidade.setExclusiveMinimum(null);
        quantidade.setExclusiveMinimumValue(BigDecimal.ZERO);
      }
    };
  }
}
