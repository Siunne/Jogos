package br.com.emilly.jogos.security;

import com.nimbusds.jose.JWSObject;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Inspeciona o JSON do JWS antes de JWTClaimsSet converter NumericDate em Date ou o Spring
 * converter sub em String. Nenhum dado desta leitura autentica o portador: assinatura, algoritmo,
 * emissor e tempo continuam a cargo do delegate.
 */
final class ClaimsOriginaisJwtDecoder implements JwtDecoder {
  // O JsonMapper já pertence ao starter WebMVC. Não compartilhar/mudar o mapper
  // HTTP: aqui números precisam manter precisão decimal, sem passar por double.
  private static final JsonMapper JSON =
      JsonMapper.builder()
          .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
          .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
          .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
          .build();
  // DateUtils do Nimbus multiplica segundos long por 1000. A divisão truncada
  // define um intervalo conservador que não pode estourar em nenhum dos sinais.
  private static final BigDecimal MIN_SECONDS = BigDecimal.valueOf(Long.MIN_VALUE / 1000);
  private static final BigDecimal MAX_SECONDS = BigDecimal.valueOf(Long.MAX_VALUE / 1000);
  private final JwtDecoder delegate;
  private final Clock clock;
  private final BigDecimal durationSeconds;

  ClaimsOriginaisJwtDecoder(JwtDecoder delegate, Clock clock, Duration expiration) {
    this.delegate = delegate;
    this.clock = clock;
    this.durationSeconds =
        BigDecimal.valueOf(expiration.getSeconds())
            .add(BigDecimal.valueOf(expiration.getNano(), 9));
  }

  @Override
  public Jwt decode(String token) {
    if (token == null) throw invalid();
    try {
      // Não usar SignedJWT.getJWTClaimsSet(): nessa etapa o overflow já ocorreu.
      Map<String, Object> original =
          JSON.readValue(
              JWSObject.parse(token).getPayload().toString(),
              new TypeReference<Map<String, Object>>() {});
      if (original == null || !(original.get("sub") instanceof String)) throw invalid();
      var issued = validateTime(original.get("iat"));
      var expires = validateTime(original.get("exp"));
      var instant = clock.instant();
      var now =
          BigDecimal.valueOf(instant.getEpochSecond())
              .add(BigDecimal.valueOf(instant.getNano(), 9));
      // Comparar também os instantes originais: truncar uma fração poderia
      // esconder iat/nbf futuro ou uma duração diferente da configurada.
      if (issued.compareTo(now) > 0
          || expires.compareTo(now) <= 0
          || expires.subtract(issued).compareTo(durationSeconds) != 0) throw invalid();
      if (original.containsKey("nbf") && validateTime(original.get("nbf")).compareTo(now) > 0)
        throw invalid();
    } catch (ParseException | JacksonException | IllegalArgumentException malformed) {
      // Não incluir o token, valores de claims ou a causa na mensagem de erro.
      throw invalid();
    }
    return delegate.decode(token);
  }

  private static BigDecimal validateTime(Object original) {
    if (!(original instanceof Number number)) throw invalid();
    // Comparar antes de longValue(), multiplicação ou qualquer conversão em data.
    // BigDecimal também rejeita representações não finitas de Number.
    var seconds = new BigDecimal(number.toString());
    if (seconds.compareTo(MIN_SECONDS) < 0 || seconds.compareTo(MAX_SECONDS) > 0) throw invalid();
    return seconds;
  }

  private static BadJwtException invalid() {
    return new BadJwtException("Token inválido");
  }
}
