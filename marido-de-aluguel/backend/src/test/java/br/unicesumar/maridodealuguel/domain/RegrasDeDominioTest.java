package br.unicesumar.maridodealuguel.domain;

import br.unicesumar.maridodealuguel.exception.ConflitoException;
import br.unicesumar.maridodealuguel.exception.RegraNegocioException;
import br.unicesumar.maridodealuguel.exception.TransicaoStatusInvalidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Testes de unidade das regras de negócio do domínio (sem banco). */
class RegrasDeDominioTest {

    private final Prestador prestador = new Prestador("Douglas", "d@email.com", "(44) 99999-0000", "hash",
            "Zona 05", -23.4196, -51.9497);
    private final Cliente cliente = new Cliente("Camila", "c@email.com", "(44) 98888-0000", "hash",
            "Zona 07", -23.4105, -51.9372);

    @Test
    void perfilVemDaSubclasse() {
        Usuario u1 = cliente;
        Usuario u2 = prestador;
        assertThat(u1.getPerfil()).isEqualTo(PerfilUsuario.CLIENTE);
        assertThat(u2.getPerfil()).isEqualTo(PerfilUsuario.PRESTADOR);
    }

    @Test
    void orcamentoPolimorfico() {
        Servico porHora = new ServicoPorHora(prestador, "Elétrica", "Instalação elétrica", 10, new BigDecimal("75.00"));
        Servico empreitada = new ServicoPorEmpreitada(prestador, "Elétrica", "Troca de resistência de chuveiro", 10, new BigDecimal("80.00"));
        BigDecimal tresHoras = new BigDecimal("3");

        assertThat(porHora.calcularOrcamento(tresHoras)).isEqualByComparingTo("225.00");
        assertThat(empreitada.calcularOrcamento(tresHoras)).isEqualByComparingTo("80.00");
        assertThatThrownBy(() -> porHora.calcularOrcamento(null)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void transicoesDeStatus() {
        assertThat(StatusOrdem.ABERTA.podeTransitarPara(StatusOrdem.ACEITA)).isTrue();
        assertThat(StatusOrdem.ABERTA.podeTransitarPara(StatusOrdem.CANCELADA)).isTrue();
        assertThat(StatusOrdem.ABERTA.podeTransitarPara(StatusOrdem.CONCLUIDA)).isFalse();
        assertThat(StatusOrdem.ACEITA.podeTransitarPara(StatusOrdem.EM_EXECUCAO)).isTrue();
        assertThat(StatusOrdem.ACEITA.podeTransitarPara(StatusOrdem.CANCELADA)).isTrue();
        assertThat(StatusOrdem.EM_EXECUCAO.podeTransitarPara(StatusOrdem.CONCLUIDA)).isTrue();
        assertThat(StatusOrdem.EM_EXECUCAO.podeTransitarPara(StatusOrdem.CANCELADA)).isFalse();
        for (StatusOrdem destino : StatusOrdem.values()) {
            assertThat(StatusOrdem.CONCLUIDA.podeTransitarPara(destino)).isFalse();
            assertThat(StatusOrdem.CANCELADA.podeTransitarPara(destino)).isFalse();
        }
    }

    @Test
    void cicloCompletoComHistoricoEAvaliacaoUnica() {
        ServicoBase servico = new ServicoPorHora(prestador, "Elétrica", "Instalação elétrica", 10, new BigDecimal("75.00"));
        OrdemServico ordem = new OrdemServico(cliente, servico, new BigDecimal("2"));
        assertThat(ordem.getValorFinal()).isEqualByComparingTo("150.00");

        assertThatThrownBy(() -> ordem.avaliar(5, "cedo demais")).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> ordem.alterarStatus(StatusOrdem.CONCLUIDA))
                .isInstanceOf(TransicaoStatusInvalidaException.class);

        ordem.alterarStatus(StatusOrdem.ACEITA);
        ordem.alterarStatus(StatusOrdem.EM_EXECUCAO);
        ordem.alterarStatus(StatusOrdem.CONCLUIDA);
        assertThat(ordem.getHistorico()).extracting(HistoricoStatusOrdem::getStatus).containsExactly(
                StatusOrdem.ABERTA, StatusOrdem.ACEITA, StatusOrdem.EM_EXECUCAO, StatusOrdem.CONCLUIDA);

        ordem.avaliar(4, "bom");
        assertThat(prestador.getNotaMedia()).isEqualByComparingTo("4.00");
        assertThat(prestador.getQuantidadeAvaliacoes()).isEqualTo(1);
        assertThatThrownBy(() -> ordem.avaliar(5, "de novo")).isInstanceOf(ConflitoException.class);
    }

    @Test
    void notaMediaRecalculada() {
        ServicoBase servico = new ServicoPorEmpreitada(prestador, "Elétrica", "Troca de resistência de chuveiro", 10, new BigDecimal("80"));
        int[] notas = {5, 4, 5, 4};
        for (int nota : notas) {
            OrdemServico ordem = new OrdemServico(cliente, servico, null);
            ordem.alterarStatus(StatusOrdem.ACEITA);
            ordem.alterarStatus(StatusOrdem.EM_EXECUCAO);
            ordem.alterarStatus(StatusOrdem.CONCLUIDA);
            ordem.avaliar(nota, null);
        }
        assertThat(prestador.getNotaMedia()).isEqualByComparingTo("4.50");
        assertThat(prestador.getQuantidadeAvaliacoes()).isEqualTo(4);
    }
}
