package br.unicesumar.maridodealuguel.exception;

import org.springframework.http.HttpStatus;

/** Usuário autenticado sem permissão sobre o recurso (HTTP 403). */
public class AcessoNegadoException extends NegocioException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
