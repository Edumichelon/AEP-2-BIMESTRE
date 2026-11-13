package br.unicesumar.maridodealuguel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Registro de data e hora de cada mudança de status da ordem (RF05). */
@Entity
@Table(name = "ordem_status_historico")
public class HistoricoStatusOrdem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_id", nullable = false, updatable = false)
    private OrdemServico ordem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private StatusOrdem status;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    protected HistoricoStatusOrdem() {
        // exigido pelo JPA
    }

    HistoricoStatusOrdem(OrdemServico ordem, StatusOrdem status, LocalDateTime dataHora) {
        this.ordem = ordem;
        this.status = status;
        this.dataHora = dataHora;
    }

    public Integer getId() {
        return id;
    }

    public StatusOrdem getStatus() {
        return status;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
