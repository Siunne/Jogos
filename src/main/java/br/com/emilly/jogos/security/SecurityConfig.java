package br.com.emilly.jogos.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
  @Bean
  public PasswordEncoder passwordEncoder() {
    var delegate = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    return new PasswordEncoder() {
      @Override
      public String encode(CharSequence raw) {
        br.com.emilly.jogos.domain.PoliticaSenha.validar(raw == null ? null : raw.toString());
        return delegate.encode(raw);
      }

      @Override
      public boolean matches(CharSequence raw, String encoded) {
        // A proteção de 72 bytes vale também no Basic, evitando truncamento/erro do BCrypt.
        if (!br.com.emilly.jogos.domain.PoliticaSenha.valida(raw == null ? null : raw.toString()))
          return false;
        try {
          return delegate.matches(raw, encoded);
        } catch (IllegalArgumentException hashInvalido) {
          // Dado corrompido não deve expor hash, causa interna ou produzir erro500 no login.
          return false;
        }
      }

      @Override
      public boolean upgradeEncoding(String encoded) {
        return delegate.upgradeEncoding(encoded);
      }
    };
  }

  @Bean
  public AuthenticationEntryPoint credenciaisInvalidas() {
    return (request, response, failure) -> {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setHeader("WWW-Authenticate", "Basic realm=\"Jogos\", charset=\"UTF-8\"");
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      // Resposta genérica, sem serializar falha, credencial, header ou path fornecido pelo cliente.
      response
          .getWriter()
          .write(
              "{\"status\":401,\"message\":\"Autenticação necessária ou credenciais inválidas\"}");
    };
  }

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, AuthenticationEntryPoint credenciaisInvalidas) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .requestCache(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        HttpMethod.GET,
                        "/api/health",
                        "/v3/api-docs",
                        "/v3/api-docs.yaml",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/pessoas", "/api/contas-acesso")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(e -> e.authenticationEntryPoint(credenciaisInvalidas))
        .httpBasic(b -> b.authenticationEntryPoint(credenciaisInvalidas))
        .build();
  }
}
