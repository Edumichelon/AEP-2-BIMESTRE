package br.unicesumar.maridodealuguel.controller;

import br.unicesumar.maridodealuguel.dto.ServicoRequest;
import br.unicesumar.maridodealuguel.dto.ServicoResponse;
import br.unicesumar.maridodealuguel.dto.OrdenacaoBusca;
import br.unicesumar.maridodealuguel.dto.ResultadoBuscaResponse;
import br.unicesumar.maridodealuguel.security.UsuarioAutenticado;
import br.unicesumar.maridodealuguel.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/** RF02 (CRUD de ofertas do prestador) e RF03 (busca do cliente). */
@RestController
@RequestMapping("/api/servicos")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    // Create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PRESTADOR')")
    public ServicoResponse criar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                       @Valid @RequestBody ServicoRequest dados) {
        return servicoService.criar(usuario.id(), dados);
    }

    // Read (lista do próprio prestador)
    @GetMapping("/meus")
    @PreAuthorize("hasRole('PRESTADOR')")
    public List<ServicoResponse> meusServicos(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicoService.listarDoPrestador(usuario.id());
    }

    // Read (um serviço)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PRESTADOR')")
    public ServicoResponse buscarPorId(@PathVariable Integer id) {
        return servicoService.buscarPorId(id);
    }

    // Update
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRESTADOR')")
    public ServicoResponse atualizar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                           @PathVariable Integer id,
                                           @Valid @RequestBody ServicoRequest dados) {
        return servicoService.atualizar(usuario.id(), id, dados);
    }

    // Delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PRESTADOR')")
    public void excluir(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable Integer id) {
        servicoService.excluir(usuario.id(), id);
    }

    @GetMapping("/categorias")
    @PreAuthorize("hasAnyRole('CLIENTE', 'PRESTADOR')")
    public List<String> categorias() {
        return servicoService.listarCategorias();
    }

    /** RF03: busca por categoria com filtros combináveis e ordenação. */
    @GetMapping("/busca")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<ResultadoBuscaResponse> buscar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                               @RequestParam String categoria,
                                               @RequestParam(required = false) BigDecimal precoMin,
                                               @RequestParam(required = false) BigDecimal precoMax,
                                               @RequestParam(required = false) BigDecimal notaMin,
                                               @RequestParam(required = false) Double distanciaMaxKm,
                                               @RequestParam(defaultValue = "DISTANCIA") OrdenacaoBusca ordenarPor,
                                               @RequestParam(required = false) BigDecimal horas) {
        return servicoService.buscar(usuario.id(), categoria, precoMin, precoMax, notaMin, distanciaMaxKm,
                ordenarPor, horas);
    }
}
