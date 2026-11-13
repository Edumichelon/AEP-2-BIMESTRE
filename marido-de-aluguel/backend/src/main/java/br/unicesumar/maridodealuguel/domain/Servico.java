package br.unicesumar.maridodealuguel.domain;

import java.math.BigDecimal;

/**
 * Contrato de precificação de um serviço (interface do diagrama de classes).
 * Cada modelo de cobrança implementa o cálculo do seu jeito; quem chama não conhece o tipo concreto.
 */
public interface Servico {

    /**
     * Calcula o orçamento do serviço.
     *
     * @param horasEstimadas duração estimada do atendimento; usada apenas pelos modelos que cobram por tempo
     * @return valor do orçamento em reais
     */
    BigDecimal calcularOrcamento(BigDecimal horasEstimadas);
}
