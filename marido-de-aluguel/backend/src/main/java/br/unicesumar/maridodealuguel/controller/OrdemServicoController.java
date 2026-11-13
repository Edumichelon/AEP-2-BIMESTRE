package br.unicesumar.maridodealuguel.controller;

import br.unicesumar.maridodealuguel.dto.AbrirOrdemRequest;
import br.unicesumar.maridodealuguel.dto.AlterarStatusRequest;
import br.unicesumar.maridodealuguel.dto.AvaliacaoRequest;
import br.unicesumar.maridodealuguel.dto.AvaliacaoResponse;
import br.unicesumar.maridodealuguel.dto.OrdemServicoResponse;
import br.unicesumar.maridodealuguel.security.UsuarioAutenticado;
import br.unicesumar.maridodealuguel.service.OrdemServicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** RF05 e RF06. Não há rota de edição ou exclusão de avaliação (RNF04). */
@RestController
@RequestMapping("/api/ordens")
public class OrdemServicoController {

    private final OrdemServicoService ordemService;

    public OrdemServicoController(OrdemServicoService ordemService) {
        this.ordemService = ordemService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CLIENTE')")
    public OrdemServicoResponse abrir(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                      @Valid @RequestBody AbrirOrdemRequest dados) {
        return ordemService.abrir(usuario, dados);
    }

    /** Cliente vê as ordens que abriu; prestador vê as que recebeu. */
    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'PRESTADOR')")
    public List<OrdemServicoResponse> listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ordemService.listar(usuario);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PRESTADOR')")
    public OrdemServicoResponse detalhar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                         @PathVariable Integer id) {
        return ordemService.detalhar(usuario, id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PRESTADOR')")
    public OrdemServicoResponse alterarStatus(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                              @PathVariable Integer id,
                                              @Valid @RequestBody AlterarStatusRequest dados) {
        return ordemService.alterarStatus(usuario, id, dados.status());
    }

    @PostMapping("/{id}/avaliacao")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CLIENTE')")
    public AvaliacaoResponse avaliar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                     @PathVariable Integer id,
                                     @Valid @RequestBody AvaliacaoRequest dados) {
        return ordemService.avaliar(usuario, id, dados);
    }
}
