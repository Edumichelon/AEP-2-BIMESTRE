package br.unicesumar.maridodealuguel.dto;

import br.unicesumar.maridodealuguel.domain.StatusOrdem;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusRequest(@NotNull(message = "Informe o novo status") StatusOrdem status) {
}
