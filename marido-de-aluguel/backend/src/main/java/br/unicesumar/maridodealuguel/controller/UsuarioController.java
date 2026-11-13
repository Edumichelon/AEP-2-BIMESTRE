package br.unicesumar.maridodealuguel.controller;

import br.unicesumar.maridodealuguel.dto.PerfilUpdateRequest;
import br.unicesumar.maridodealuguel.dto.PrestadorPerfilResponse;
import br.unicesumar.maridodealuguel.dto.UsuarioResponse;
import br.unicesumar.maridodealuguel.security.UsuarioAutenticado;
import br.unicesumar.maridodealuguel.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/usuarios/me")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PRESTADOR')")
    public UsuarioResponse meuPerfil(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuarioService.buscarPerfil(usuario.id());
    }

    @PutMapping("/usuarios/me")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PRESTADOR')")
    public UsuarioResponse atualizarPerfil(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                           @Valid @RequestBody PerfilUpdateRequest dados) {
        return usuarioService.atualizarPerfil(usuario.id(), dados);
    }

    /** Perfil público do prestador (nota média, concluídos, ofertas e avaliações). */
    @GetMapping("/prestadores/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PRESTADOR')")
    public PrestadorPerfilResponse perfilPrestador(@PathVariable Integer id) {
        return usuarioService.perfilPublicoPrestador(id);
    }
}
