package br.unicesumar.maridodealuguel.exception;

import org.springframework.http.HttpStatus;

/** Recurso inexistente (HTTP 404). */
public class RecursoNaoEncontradoException extends NegocioException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
