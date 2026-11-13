package br.unicesumar.maridodealuguel.domain;

import br.unicesumar.maridodealuguel.exception.RegraNegocioException;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Cobrança por tempo: valor base é o preço da hora. */
@Entity
@DiscriminatorValue("POR_HORA")
public class ServicoPorHora extends ServicoBase {

    protected ServicoPorHora() {
        // exigido pelo JPA
    }

    public ServicoPorHora(Prestador prestador, String categoria, String descricao,
                          Integer raioAtendimentoKm, BigDecimal valorHora) {
        super(prestador, categoria, descricao, raioAtendimentoKm, valorHora);
    }

    /** Valor base x horas estimadas. */
    @Override
    public BigDecimal calcularOrcamento(BigDecimal horasEstimadas) {
        if (horasEstimadas == null || horasEstimadas.signum() <= 0) {
            throw new RegraNegocioException("Serviço cobrado por hora exige horas estimadas maiores que zero");
        }
        return getValorBase().multiply(horasEstimadas).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public TipoCobranca getTipoCobranca() {
        return TipoCobranca.POR_HORA;
    }
}
