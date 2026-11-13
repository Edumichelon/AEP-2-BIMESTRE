package br.unicesumar.maridodealuguel.exception;

import org.springframework.http.HttpStatus;

/**
 * Raiz das exceções de negócio. Cada subclasse sobrescreve getStatus() com o código HTTP adequado,
 * e o GlobalExceptionHandler trata todas de forma polimórfica, sem um if por tipo.
 */
public abstract class NegocioException extends RuntimeException {

    protected NegocioException(String mensagem) {
        super(mensagem);
    }

    public abstract HttpStatus getStatus();
}
