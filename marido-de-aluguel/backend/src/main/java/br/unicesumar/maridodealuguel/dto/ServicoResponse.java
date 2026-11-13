package br.unicesumar.maridodealuguel.dto;

import br.unicesumar.maridodealuguel.domain.ServicoBase;
import br.unicesumar.maridodealuguel.domain.TipoCobranca;

import java.math.BigDecimal;

public record ServicoResponse(Integer id, String categoria, String descricao, Integer raioAtendimentoKm,
                              TipoCobranca tipoCobranca, BigDecimal valorBase,
                              Integer prestadorId, String prestadorNome) {

    public static ServicoResponse de(ServicoBase servico) {
        return new ServicoResponse(servico.getId(), servico.getCategoria(), servico.getDescricao(),
                servico.getRaioAtendimentoKm(), servico.getTipoCobranca(), servico.getValorBase(),
                servico.getPrestador().getId(), servico.getPrestador().getNome());
    }
}
