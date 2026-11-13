package br.unicesumar.maridodealuguel.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** RF05: contratação de uma oferta. horasEstimadas é obrigatório só para serviço cobrado por hora. */
public record AbrirOrdemRequest(
        @NotNull(message = "Informe o serviço") Integer servicoId,
        @DecimalMin(value = "0.5", message = "Mínimo de 0,5 hora")
        @DecimalMax(value = "200", message = "Máximo de 200 horas")
        @Digits(integer = 3, fraction = 2, message = "Use no máximo 2 casas decimais")
        BigDecimal horasEstimadas) {
}
