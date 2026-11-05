package br.com.emilly.jogos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import java.util.List;
@Schema(description = "Página estável da API, independente da representação interna do Spring Data.")
public record PaginaResponse<T>(@Schema(description = "Elementos da página atual; não representa necessariamente todos os resultados", requiredMode = Schema.RequiredMode.REQUIRED)
        List<T> conteudo, @Schema(description = "Índice da página atual, iniciado em zero", example = "0", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        int pagina, @Schema(description = "Quantidade solicitada por página; padrão 20, máximo 100", example = "20", minimum = "1", maximum = "100", requiredMode = Schema.RequiredMode.REQUIRED)
        int tamanho,
        @Schema(description = "Quantidade total de registros que correspondem aos filtros", example = "37", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        long totalElementos, @Schema(description = "Quantidade total de páginas para o tamanho informado", example = "2", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        int totalPaginas, @Schema(description = "Indica que a página é a primeira", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean primeira, @Schema(description = "Indica que a página é a última", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean ultima) {
    public static <T> PaginaResponse<T> de(Page<T> page) {
        return new PaginaResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }
}
