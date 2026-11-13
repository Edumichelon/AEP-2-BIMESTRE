package br.unicesumar.maridodealuguel.dto;

import br.unicesumar.maridodealuguel.domain.Cliente;
import br.unicesumar.maridodealuguel.domain.HistoricoStatusOrdem;
import br.unicesumar.maridodealuguel.domain.ServicoBase;
import br.unicesumar.maridodealuguel.domain.OrdemServico;
import br.unicesumar.maridodealuguel.domain.Prestador;
import br.unicesumar.maridodealuguel.domain.StatusOrdem;
import br.unicesumar.maridodealuguel.domain.TipoCobranca;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Ordem de serviço com histórico e avaliação. transicoesPermitidas traz os próximos status que o
 * usuário autenticado pode aplicar, para a tela mostrar só os botões válidos.
 */
public record OrdemServicoResponse(Integer id, String codigo, StatusOrdem status, String statusDescricao,
                                   LocalDate dataAbertura, BigDecimal horasEstimadas, BigDecimal valorFinal,
                                   ServicoResumo servico, Pessoa cliente, Pessoa prestador,
                                   List<Historico> historico, AvaliacaoResponse avaliacao,
                                   boolean podeAvaliar, List<StatusOrdem> transicoesPermitidas) {

    public record ServicoResumo(Integer id, String descricao, String categoria, TipoCobranca tipoCobranca,
                                BigDecimal valorBase) {
    }

    public record Pessoa(Integer id, String nome, String telefone, String endereco) {
    }

    public record Historico(StatusOrdem status, String descricao, LocalDateTime dataHora) {
    }

    public static OrdemServicoResponse de(OrdemServico ordem, boolean podeAvaliar,
                                          List<StatusOrdem> transicoesPermitidas) {
        ServicoBase servico = ordem.getServico();
        Cliente cliente = ordem.getCliente();
        Prestador prestador = ordem.getPrestador();
        List<Historico> historico = ordem.getHistorico().stream()
                .map(OrdemServicoResponse::historico)
                .toList();
        return new OrdemServicoResponse(ordem.getId(), ordem.getCodigo(), ordem.getStatus(),
                ordem.getStatus().getDescricao(), ordem.getDataAbertura(), ordem.getHorasEstimadas(),
                ordem.getValorFinal(),
                new ServicoResumo(servico.getId(), servico.getDescricao(), servico.getCategoria(),
                        servico.getTipoCobranca(), servico.getValorBase()),
                new Pessoa(cliente.getId(), cliente.getNome(), cliente.getTelefone(), cliente.getEndereco()),
                new Pessoa(prestador.getId(), prestador.getNome(), prestador.getTelefone(), null),
                historico,
                ordem.getAvaliacao() == null ? null : AvaliacaoResponse.de(ordem.getAvaliacao()),
                podeAvaliar, transicoesPermitidas);
    }

    private static Historico historico(HistoricoStatusOrdem h) {
        return new Historico(h.getStatus(), h.getStatus().getDescricao(), h.getDataHora());
    }
}
