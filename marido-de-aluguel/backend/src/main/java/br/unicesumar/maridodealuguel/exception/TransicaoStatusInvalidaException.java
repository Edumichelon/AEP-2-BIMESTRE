package br.unicesumar.maridodealuguel.exception;

/** Mudança de status da ordem que não respeita o ciclo Aberta -> Aceita -> Em execução -> Concluída. */
public class TransicaoStatusInvalidaException extends RegraNegocioException {

    public TransicaoStatusInvalidaException(String mensagem) {
        super(mensagem);
    }
}
