package br.com.emilly.jogos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.core.MethodParameter;
import java.util.List;

@Configuration
public class PaginacaoConfig implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        var resolver = new PageableHandlerMethodArgumentResolver() {
            @Override
            public Pageable resolveArgument(MethodParameter parameter, ModelAndViewContainer container,
                    NativeWebRequest request, WebDataBinderFactory binderFactory) {
                validarNumero(request.getParameter("page"), 0, Integer.MAX_VALUE, "page");
                validarNumero(request.getParameter("size"), 1, 100, "size");
                String[] sorts = request.getParameterValues("sort");
                if (sorts != null) {
                    for (String sort : sorts) {
                        String[] partes = sort.split(",", -1);
                        if (partes.length > 2 || partes[0].isBlank() ||
                                (partes.length == 2 && !partes[1].equalsIgnoreCase("asc") && !partes[1].equalsIgnoreCase("desc"))) {
                            throw new IllegalArgumentException("Ordenação deve usar campo,asc ou campo,desc");
                        }
                    }
                }
                return super.resolveArgument(parameter, container, request, binderFactory);
            }
        };
        resolver.setFallbackPageable(PageRequest.of(0, 20, Sort.by("id")));
        resolver.setMaxPageSize(100);
        resolvers.add(resolver);
    }
    private static void validarNumero(String valor, int minimo, int maximo, String campo) {
        if (valor == null) return;
        try {
            int numero = Integer.parseInt(valor);
            if (numero < minimo || numero > maximo) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(campo + " deve estar entre " + minimo + " e " + maximo);
        }
    }
}
