package br.unicesumar.maridodealuguel.exception;

import org.springframework.http.HttpStatus;

/** Conflito com o estado atual dos dados, como e-mail repetido ou avaliação duplicada (HTTP 409). */
public class ConflitoException extends NegocioException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}
