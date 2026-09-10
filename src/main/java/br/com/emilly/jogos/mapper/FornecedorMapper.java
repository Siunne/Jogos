package br.com.emilly.jogos.mapper;

import br.com.emilly.jogos.domain.Fornecedor;
import br.com.emilly.jogos.dto.FornecedorRequest;
import br.com.emilly.jogos.dto.FornecedorResponse;
import org.springframework.stereotype.Component;

@Component
public class FornecedorMapper {

    public Fornecedor toEntity(FornecedorRequest request) {
        return new Fornecedor(
                request.razaoSocial(),
                request.cnpj()
        );
    }

    public FornecedorResponse toResponse(Fornecedor fornecedor) {
        return new FornecedorResponse(
                fornecedor.getId(),
                fornecedor.getRazaoSocial(),
                fornecedor.getCnpj(),
                fornecedor.getStatus().name()
        );
    }
}