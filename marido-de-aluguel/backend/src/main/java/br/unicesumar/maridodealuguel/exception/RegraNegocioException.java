package br.unicesumar.maridodealuguel.exception;

import org.springframework.http.HttpStatus;

/** Violação de regra de negócio (HTTP 422). */
public class RegraNegocioException extends NegocioException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}
