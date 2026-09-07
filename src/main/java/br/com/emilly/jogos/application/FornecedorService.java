package br.com.emilly.jogos.application;

import br.com.emilly.jogos.domain.Fornecedor;
import br.com.emilly.jogos.exception.RecursoDuplicadoException;
import br.com.emilly.jogos.repository.FornecedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FornecedorService {

    private final FornecedorRepository repository;

    public FornecedorService(FornecedorRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Fornecedor cadastrar(String razaoSocial, String cnpj) {

        if (repository.existsByCnpj(cnpj)) {
            throw new RecursoDuplicadoException(
                    "CNPJ já cadastrado");
        }

        return repository.save(
                new Fornecedor(razaoSocial, cnpj));
    }
}