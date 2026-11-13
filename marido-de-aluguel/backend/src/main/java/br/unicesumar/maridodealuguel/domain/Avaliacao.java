package br.unicesumar.maridodealuguel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Avaliação de uma ordem concluída (RF06 / RNF04).
 * Imutável: sem setters, colunas com updatable = false e construtor acessível só pelo pacote,
 * de modo que a única porta de entrada é OrdemServico.avaliar().
 */
@Entity
@Table(name = "avaliacoes")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_id", nullable = false, unique = true, updatable = false)
    private OrdemServico ordem;

    @Column(nullable = false, updatable = false)
    private Integer nota;

    @Column(columnDefinition = "TEXT", updatable = false)
    private String comentario;

    @Column(name = "data_avaliacao", nullable = false, updatable = false)
    private LocalDate dataAvaliacao;

    protected Avaliacao() {
        // exigido pelo JPA
    }

    Avaliacao(OrdemServico ordem, Integer nota, String comentario, LocalDate dataAvaliacao) {
        this.ordem = ordem;
        this.nota = nota;
        this.comentario = comentario;
        this.dataAvaliacao = dataAvaliacao;
    }

    public Integer getId() {
        return id;
    }

    public OrdemServico getOrdem() {
        return ordem;
    }

    public Integer getNota() {
        return nota;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDate getDataAvaliacao() {
        return dataAvaliacao;
    }
}
