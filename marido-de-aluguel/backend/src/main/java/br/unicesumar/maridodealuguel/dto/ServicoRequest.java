package br.unicesumar.maridodealuguel.dto;

import br.unicesumar.maridodealuguel.domain.TipoCobranca;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** RF02: categoria, descrição, raio de atendimento, modelo de cobrança e valor base (criação e edição). */
public record ServicoRequest(
        @NotBlank(message = "Informe a categoria") @Size(max = 50) String categoria,
        @NotBlank(message = "Descreva o serviço")
        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres") String descricao,
        @NotNull(message = "Informe o raio de atendimento")
        @Min(value = 1, message = "O raio mínimo é 1 km") @Max(value = 200, message = "O raio máximo é 200 km")
        Integer raioAtendimentoKm,
        @NotNull(message = "Escolha o modelo de cobrança") TipoCobranca tipoCobranca,
        @NotNull(message = "Informe o valor base")
        @DecimalMin(value = "0.01", message = "O valor base deve ser maior que zero")
        @DecimalMax(value = "99999999.99", message = "Valor base muito alto")
        @Digits(integer = 8, fraction = 2, message = "Use no máximo 2 casas decimais")
        BigDecimal valorBase) {
}
