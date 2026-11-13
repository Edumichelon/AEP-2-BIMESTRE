package br.unicesumar.maridodealuguel.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Cobrança por escopo fechado: o valor base já é o preço do serviço inteiro. */
@Entity
@DiscriminatorValue("POR_EMPREITADA")
public class ServicoPorEmpreitada extends ServicoBase {

    protected ServicoPorEmpreitada() {
        // exigido pelo JPA
    }

    public ServicoPorEmpreitada(Prestador prestador, String categoria, String descricao,
                                Integer raioAtendimentoKm, BigDecimal valorFechado) {
        super(prestador, categoria, descricao, raioAtendimentoKm, valorFechado);
    }

    /** Valor fechado: as horas estimadas não alteram o preço. */
    @Override
    public BigDecimal calcularOrcamento(BigDecimal horasEstimadas) {
        return getValorBase().setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public TipoCobranca getTipoCobranca() {
        return TipoCobranca.POR_EMPREITADA;
    }
}
