package br.com.emilly.jogos.mapper;

import br.com.emilly.jogos.domain.*;
import br.com.emilly.jogos.dto.*;
import org.springframework.stereotype.Component;

@Component
public class VendaMapper {
  public VendaResponse toResponse(Venda v) {
    return new VendaResponse(
        v.getId(),
        v.getCliente().getId(),
        v.getCliente().getPessoa().getNome(),
        v.getVendedor().getId(),
        v.getVendedor().getPessoa().getNome(),
        v.getDataVenda(),
        v.getStatus(),
        v.getItens().stream()
            .map(
                i ->
                    new ItemVendaResponse(
                        i.getId(),
                        i.getJogo().getId(),
                        i.getJogo().getNome(),
                        i.getQuantidade(),
                        i.getValorUnitario(),
                        i.calcularSubtotal()))
            .toList(),
        v.calcularTotal());
  }

  public VendaResumoResponse toResumo(Venda v) {
    return new VendaResumoResponse(
        v.getId(),
        v.getCliente().getId(),
        v.getCliente().getPessoa().getNome(),
        v.getVendedor().getId(),
        v.getVendedor().getPessoa().getNome(),
        v.getDataVenda(),
        v.getStatus());
  }
}
