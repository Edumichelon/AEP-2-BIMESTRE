package br.unicesumar.maridodealuguel.dto;

import br.unicesumar.maridodealuguel.domain.PerfilUsuario;
import br.unicesumar.maridodealuguel.domain.Prestador;
import br.unicesumar.maridodealuguel.domain.Usuario;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UsuarioResponse(Integer id, String nome, String email, String telefone, String endereco,
                              Double latitude, Double longitude, PerfilUsuario perfil,
                              BigDecimal notaMedia, Integer quantidadeAvaliacoes) {

    public static UsuarioResponse de(Usuario usuario) {
        BigDecimal nota = null;
        Integer quantidade = null;
        if (usuario instanceof Prestador prestador) {
            nota = prestador.getNotaMedia();
            quantidade = prestador.getQuantidadeAvaliacoes();
        }
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTelefone(),
                usuario.getEndereco(), usuario.getLatitude(), usuario.getLongitude(), usuario.getPerfil(),
                nota, quantidade);
    }
}
