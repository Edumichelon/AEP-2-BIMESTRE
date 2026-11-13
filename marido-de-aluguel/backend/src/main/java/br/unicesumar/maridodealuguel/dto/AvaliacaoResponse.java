package br.unicesumar.maridodealuguel.dto;

import br.unicesumar.maridodealuguel.domain.Avaliacao;

import java.time.LocalDate;

public record AvaliacaoResponse(Integer id, Integer ordemId, Integer nota, String comentario,
                                LocalDate dataAvaliacao, String clienteNome) {

    public static AvaliacaoResponse de(Avaliacao avaliacao) {
        return new AvaliacaoResponse(avaliacao.getId(), avaliacao.getOrdem().getId(), avaliacao.getNota(),
                avaliacao.getComentario(), avaliacao.getDataAvaliacao(), avaliacao.getOrdem().getCliente().getNome());
    }
}
