package br.unicesumar.maridodealuguel.service;

import br.unicesumar.maridodealuguel.domain.Avaliacao;
import br.unicesumar.maridodealuguel.domain.HistoricoStatusOrdem;
import br.unicesumar.maridodealuguel.domain.OrdemServico;
import br.unicesumar.maridodealuguel.domain.Prestador;
import br.unicesumar.maridodealuguel.domain.StatusOrdem;
import br.unicesumar.maridodealuguel.dto.RelatorioPrestadorResponse;
import br.unicesumar.maridodealuguel.dto.RelatorioPrestadorResponse.OrdemConcluida;
import br.unicesumar.maridodealuguel.dto.RelatorioPrestadorResponse.PontoEvolucaoNota;
import br.unicesumar.maridodealuguel.exception.RecursoNaoEncontradoException;
import br.unicesumar.maridodealuguel.exception.RegraNegocioException;
import br.unicesumar.maridodealuguel.repository.AvaliacaoRepository;
import br.unicesumar.maridodealuguel.repository.OrdemServicoRepository;
import br.unicesumar.maridodealuguel.repository.PrestadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** RF07: relatório consolidado do prestador (só ordens efetivamente concluídas contam). */
@Service
public class RelatorioService {

    private final PrestadorRepository prestadorRepository;
    private final OrdemServicoRepository ordemRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    public RelatorioService(PrestadorRepository prestadorRepository, OrdemServicoRepository ordemRepository,
                            AvaliacaoRepository avaliacaoRepository) {
        this.prestadorRepository = prestadorRepository;
        this.ordemRepository = ordemRepository;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    @Transactional(readOnly = true)
    public RelatorioPrestadorResponse gerar(Integer prestadorId, LocalDate inicio, LocalDate fim) {
        LocalDate dataFim = fim == null ? LocalDate.now() : fim;
        LocalDate dataInicio = inicio == null ? dataFim.minusDays(30) : inicio;
        if (dataInicio.isAfter(dataFim)) {
            throw new RegraNegocioException("A data inicial deve ser anterior à data final");
        }
        Prestador prestador = prestadorRepository.findById(prestadorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prestador não encontrado"));

        List<OrdemConcluida> concluidas = ordemRepository.listarConcluidasNoPeriodo(prestadorId,
                        StatusOrdem.CONCLUIDA, dataInicio.atStartOfDay(), dataFim.plusDays(1).atStartOfDay())
                .stream()
                .map(this::paraOrdemConcluida)
                .sorted(Comparator.comparing(OrdemConcluida::concluidaEm).reversed())
                .toList();
        BigDecimal faturamento = concluidas.stream()
                .map(OrdemConcluida::valorFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RelatorioPrestadorResponse(prestador.getId(), prestador.getNome(), dataInicio, dataFim,
                concluidas.size(), faturamento, prestador.getNotaMedia(), prestador.getQuantidadeAvaliacoes(),
                concluidas, evolucaoNota(avaliacaoRepository.listarDoPrestador(prestadorId)));
    }

    private OrdemConcluida paraOrdemConcluida(OrdemServico ordem) {
        LocalDateTime concluidaEm = ordem.getHistorico().stream()
                .filter(h -> h.getStatus() == StatusOrdem.CONCLUIDA)
                .map(HistoricoStatusOrdem::getDataHora)
                .findFirst()
                .orElse(null);
        return new OrdemConcluida(ordem.getId(), ordem.getCodigo(), ordem.getServico().getDescricao(),
                ordem.getCliente().getNome(), concluidaEm, ordem.getValorFinal());
    }

    /** Média acumulada mês a mês, na ordem cronológica das avaliações. */
    private List<PontoEvolucaoNota> evolucaoNota(List<Avaliacao> avaliacoes) {
        Map<YearMonth, int[]> porMes = new LinkedHashMap<>(); // [quantidade, soma] de cada mês
        for (Avaliacao avaliacao : avaliacoes) {
            int[] acumulado = porMes.computeIfAbsent(YearMonth.from(avaliacao.getDataAvaliacao()), m -> new int[2]);
            acumulado[0]++;
            acumulado[1] += avaliacao.getNota();
        }
        List<PontoEvolucaoNota> pontos = new ArrayList<>();
        int quantidadeTotal = 0;
        int somaTotal = 0;
        for (Map.Entry<YearMonth, int[]> mes : porMes.entrySet()) {
            quantidadeTotal += mes.getValue()[0];
            somaTotal += mes.getValue()[1];
            BigDecimal media = BigDecimal.valueOf(somaTotal)
                    .divide(BigDecimal.valueOf(quantidadeTotal), 2, RoundingMode.HALF_UP);
            pontos.add(new PontoEvolucaoNota(mes.getKey().toString(), mes.getValue()[0], media));
        }
        return pontos;
    }
}
