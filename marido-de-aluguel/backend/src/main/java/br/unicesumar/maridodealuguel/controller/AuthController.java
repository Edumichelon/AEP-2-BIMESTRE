package br.unicesumar.maridodealuguel.controller;

import br.unicesumar.maridodealuguel.dto.AuthResponse;
import br.unicesumar.maridodealuguel.dto.CadastroRequest;
import br.unicesumar.maridodealuguel.dto.LoginRequest;
import br.unicesumar.maridodealuguel.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** RF01: rotas públicas de cadastro e login. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse cadastrar(@Valid @RequestBody CadastroRequest dados) {
        return authService.cadastrar(dados);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest dados) {
        return authService.login(dados);
    }
}
