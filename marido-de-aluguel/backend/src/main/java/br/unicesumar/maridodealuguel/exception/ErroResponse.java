package br.unicesumar.maridodealuguel.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/** Corpo padronizado de erro devolvido pela API. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(LocalDateTime timestamp, int status, String erro, String mensagem,
                           Map<String, String> campos) {

    public static ErroResponse de(HttpStatus status, String mensagem) {
        return new ErroResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensagem, null);
    }

    public static ErroResponse de(HttpStatus status, String mensagem, Map<String, String> campos) {
        return new ErroResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensagem, campos);
    }
}
