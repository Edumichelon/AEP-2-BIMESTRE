package br.unicesumar.maridodealuguel.dto;

import java.math.BigDecimal;
import java.util.List;

/** Perfil público do prestador: a nota exibida aqui só muda por avaliações de ordens concluídas (RF06). */
public record PrestadorPerfilResponse(Integer id, String nome, String telefone, BigDecimal notaMedia,
                                      Integer quantidadeAvaliacoes, long servicosConcluidos,
                                      List<ServicoResponse> servicos, List<AvaliacaoResponse> avaliacoes) {
}
