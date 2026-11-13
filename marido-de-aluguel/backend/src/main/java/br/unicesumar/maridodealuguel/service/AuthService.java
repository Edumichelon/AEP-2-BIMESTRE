package br.unicesumar.maridodealuguel.service;

import br.unicesumar.maridodealuguel.domain.Cliente;
import br.unicesumar.maridodealuguel.domain.Prestador;
import br.unicesumar.maridodealuguel.domain.Usuario;
import br.unicesumar.maridodealuguel.dto.AuthResponse;
import br.unicesumar.maridodealuguel.dto.CadastroRequest;
import br.unicesumar.maridodealuguel.dto.LoginRequest;
import br.unicesumar.maridodealuguel.dto.UsuarioResponse;
import br.unicesumar.maridodealuguel.exception.ConflitoException;
import br.unicesumar.maridodealuguel.exception.CredenciaisInvalidasException;
import br.unicesumar.maridodealuguel.repository.UsuarioRepository;
import br.unicesumar.maridodealuguel.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RF01: cadastro e autenticação. */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse cadastrar(CadastroRequest dados) {
        String email = dados.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Já existe uma conta com este e-mail");
        }
        String senhaHash = passwordEncoder.encode(dados.senha());
        String nome = dados.nome().trim();
        String telefone = dados.telefone().trim();
        Usuario usuario = switch (dados.perfil()) {
            case CLIENTE -> new Cliente(nome, email, telefone, senhaHash, dados.endereco(),
                    dados.latitude(), dados.longitude());
            case PRESTADOR -> new Prestador(nome, email, telefone, senhaHash, dados.endereco(),
                    dados.latitude(), dados.longitude());
        };
        usuario = usuarioRepository.save(usuario);
        return new AuthResponse(jwtService.gerarToken(usuario), UsuarioResponse.de(usuario));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest dados) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(dados.email().trim())
                .filter(u -> passwordEncoder.matches(dados.senha(), u.getSenhaHash()))
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail ou senha inválidos"));
        return new AuthResponse(jwtService.gerarToken(usuario), UsuarioResponse.de(usuario));
    }
}
