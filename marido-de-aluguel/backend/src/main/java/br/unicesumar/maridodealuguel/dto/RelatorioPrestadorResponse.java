package br.unicesumar.maridodealuguel.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** RF07: indicadores consolidados do prestador. */
public record RelatorioPrestadorResponse(Integer prestadorId, String prestadorNome, LocalDate inicio, LocalDate fim,
                                         long servicosConcluidos, BigDecimal faturamentoTotal,
                                         BigDecimal notaMediaAtual, Integer quantidadeAvaliacoes,
                                         List<OrdemConcluida> ordensConcluidas,
                                         List<PontoEvolucaoNota> evolucaoNotaMedia) {

    public record OrdemConcluida(Integer ordemId, String codigo, String descricaoServico, String clienteNome,
                                 LocalDateTime concluidaEm, BigDecimal valorFinal) {
    }

    /** Média acumulada até o fim de cada mês com avaliações. */
    public record PontoEvolucaoNota(String mes, int avaliacoesNoMes, BigDecimal notaMediaAcumulada) {
    }
}
