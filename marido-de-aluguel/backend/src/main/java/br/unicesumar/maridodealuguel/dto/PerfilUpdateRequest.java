package br.unicesumar.maridodealuguel.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Manutenção do próprio perfil (e-mail e perfil não mudam depois do cadastro). */
public record PerfilUpdateRequest(
        @NotBlank(message = "Informe o nome completo") @Size(max = 100) String nome,
        @NotBlank(message = "Informe o telefone")
        @Pattern(regexp = Validacoes.TELEFONE, message = Validacoes.TELEFONE_MENSAGEM)
        String telefone,
        @Size(max = 150) String endereco,
        @NotNull(message = "Informe a localização") @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @NotNull(message = "Informe a localização") @DecimalMin("-180") @DecimalMax("180") Double longitude) {
}
