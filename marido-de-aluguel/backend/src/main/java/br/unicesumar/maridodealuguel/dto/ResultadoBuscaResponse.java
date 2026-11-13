package br.unicesumar.maridodealuguel.dto;

import br.unicesumar.maridodealuguel.domain.TipoCobranca;

import java.math.BigDecimal;

/** Uma linha do resultado de busca (RF03), já com tudo o que a comparação (RF04) exibe. */
public record ResultadoBuscaResponse(Integer servicoId, String categoria, String descricao,
                                     TipoCobranca tipoCobranca, BigDecimal valorBase, BigDecimal valorEstimado,
                                     BigDecimal horasConsideradas, Integer raioAtendimentoKm,
                                     Integer prestadorId, String prestadorNome, String prestadorTelefone,
                                     BigDecimal notaMedia, Integer quantidadeAvaliacoes, long servicosConcluidos,
                                     double distanciaKm) {
}
