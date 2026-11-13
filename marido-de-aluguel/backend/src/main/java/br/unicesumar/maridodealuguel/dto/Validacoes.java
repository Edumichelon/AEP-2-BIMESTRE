package br.unicesumar.maridodealuguel.dto;

/** Expressões de validação compartilhadas entre os DTOs. */
final class Validacoes {

    /** Telefone brasileiro com DDD: (44) 99811-2233, 44 3333-4444, 44998112233... */
    static final String TELEFONE = "^\\(?\\d{2}\\)?\\s?9?\\d{4}-?\\d{4}$";
    static final String TELEFONE_MENSAGEM = "Telefone inválido. Use DDD + número, ex.: (44) 99811-2233";

    private Validacoes() {
    }
}
