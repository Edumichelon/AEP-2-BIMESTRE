package br.unicesumar.maridodealuguel.exception;

import org.springframework.http.HttpStatus;

/** E-mail ou senha inválidos (HTTP 401). */
public class CredenciaisInvalidasException extends NegocioException {

    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}
