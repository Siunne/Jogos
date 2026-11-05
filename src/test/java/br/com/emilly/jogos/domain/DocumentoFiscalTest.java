package br.com.emilly.jogos.domain;

import static org.junit.jupiter.api.Assertions.*;

import br.com.emilly.jogos.api.validation.*;
import br.com.emilly.jogos.dto.*;
import jakarta.validation.Validation;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

class DocumentoFiscalTest {
  @Test
  void exemplosOficiais() {
    assertTrue(DocumentoFiscal.cpfValido("52998224725"));
    assertFalse(DocumentoFiscal.cpfValido("52998224724"));
    assertTrue(DocumentoFiscal.cnpjValido("11222333000181"));
    assertFalse(DocumentoFiscal.cnpjValido("11222333000182"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(
      strings = {
        " ",
        "11111111111",
        "00000000000",
        "529.982.247-25",
        "5299822472",
        "529982247250",
        "5299822472a",
        "52998224724"
      })
  void cpfRejeitaFronteiras(String cpf) {
    assertFalse(DocumentoFiscal.cpfValido(cpf));
    assertThrows(
        IllegalArgumentException.class, () -> new Pessoa("Pessoa", "pessoa@example.com", cpf));
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      assertFalse(
          factory
              .getValidator()
              .validate(new PessoaRequest("Pessoa", "pessoa@example.com", cpf))
              .isEmpty());
    }
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(
      strings = {
        " ",
        "11111111111111",
        "00000000000000",
        "11.222.333/0001-81",
        "1122233300018",
        "112223330001810",
        "1122233300018a",
        "11222333000182"
      })
  void cnpjRejeitaFronteiras(String cnpj) {
    assertFalse(DocumentoFiscal.cnpjValido(cnpj));
    assertThrows(IllegalArgumentException.class, () -> new Fornecedor("Empresa", cnpj));
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      assertFalse(
          factory.getValidator().validate(new FornecedorRequest("Empresa", cnpj)).isEmpty());
    }
  }

  @Test
  void validadoresSeparamObrigatoriedade() {
    assertTrue(new CpfValidator().isValid(null, null));
    assertTrue(new CnpjValidator().isValid(null, null));
    assertFalse(new CpfValidator().isValid("", null));
    assertFalse(new CnpjValidator().isValid(" ", null));
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      var v = factory.getValidator();
      assertFalse(v.validate(new PessoaRequest("Pessoa", "pessoa@example.com", null)).isEmpty());
      assertFalse(
          v.validate(new PessoaRequest("Pessoa", "pessoa@example.com", "52998224724")).isEmpty());
      assertFalse(v.validate(new FornecedorRequest("Empresa", "11222333000182")).isEmpty());
      assertTrue(v.validate(new FornecedorRequest("Empresa", "11222333000181")).isEmpty());
    }
  }
}
