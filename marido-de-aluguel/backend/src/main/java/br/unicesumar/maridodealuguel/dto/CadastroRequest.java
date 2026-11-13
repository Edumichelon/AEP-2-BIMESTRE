package br.unicesumar.maridodealuguel.dto;

import br.unicesumar.maridodealuguel.domain.PerfilUsuario;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** RF01: dados de cadastro de cliente ou prestador. */
public record CadastroRequest(
        @NotBlank(message = "Informe o nome completo")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "Informe o e-mail")
        @Email(regexp = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$", message = "E-mail em formato inválido")
        @Size(max = 100, message = "O e-mail deve ter no máximo 100 caracteres")
        String email,

        @NotBlank(message = "Informe o telefone")
        @Pattern(regexp = Validacoes.TELEFONE, message = Validacoes.TELEFONE_MENSAGEM)
        String telefone,

        @NotBlank(message = "Informe a senha")
        @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres")
        String senha,

        @Size(max = 150, message = "O endereço deve ter no máximo 150 caracteres")
        String endereco,

        @NotNull(message = "Informe a localização")
        @DecimalMin(value = "-90", message = "Latitude inválida") @DecimalMax(value = "90", message = "Latitude inválida")
        Double latitude,

        @NotNull(message = "Informe a localização")
        @DecimalMin(value = "-180", message = "Longitude inválida") @DecimalMax(value = "180", message = "Longitude inválida")
        Double longitude,

        @NotNull(message = "Escolha o tipo de perfil")
        PerfilUsuario perfil) {
}
