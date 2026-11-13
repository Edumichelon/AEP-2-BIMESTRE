package br.unicesumar.maridodealuguel.security;

import br.unicesumar.maridodealuguel.domain.PerfilUsuario;

/** Principal colocado no SecurityContext a partir do token JWT (sem consulta ao banco: stateless). */
public record UsuarioAutenticado(Integer id, String email, PerfilUsuario perfil) {
}
