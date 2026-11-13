package br.unicesumar.maridodealuguel.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** RF06: nota de 1 a 5 e comentário. Não existe request de edição: a avaliação é imutável (RNF04). */
public record AvaliacaoRequest(
        @NotNull(message = "Informe a nota")
        @Min(value = 1, message = "A nota mínima é 1") @Max(value = 5, message = "A nota máxima é 5")
        Integer nota,
        @Size(max = 1000, message = "O comentário deve ter no máximo 1000 caracteres") String comentario) {
}
