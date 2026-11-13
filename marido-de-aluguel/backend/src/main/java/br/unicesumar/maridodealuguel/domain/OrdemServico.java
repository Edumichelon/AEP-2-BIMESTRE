package br.unicesumar.maridodealuguel.domain;

import br.unicesumar.maridodealuguel.exception.ConflitoException;
import br.unicesumar.maridodealuguel.exception.RegraNegocioException;
import br.unicesumar.maridodealuguel.exception.TransicaoStatusInvalidaException;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contratação de uma oferta por um cliente (RF05).
 * Composição com Avaliacao: no máximo uma, e ela vive e morre com a ordem (cascade + orphanRemoval).
 */
@Entity
@Table(name = "ordens_servico")
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false, updatable = false)
    private Cliente cliente;

    /** Tipo estático é a classe abstrata: a ordem nunca sabe se a oferta é por hora ou por empreitada. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servico_id", nullable = false, updatable = false)
    private ServicoBase servico;

    @Column(name = "data_abertura", nullable = false, updatable = false)
    private LocalDate dataAbertura;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusOrdem status;

    @Column(name = "horas_estimadas", precision = 5, scale = 2, updatable = false)
    private BigDecimal horasEstimadas;

    @Column(name = "valor_final", precision = 10, scale = 2)
    private BigDecimal valorFinal;

    @OneToOne(mappedBy = "ordem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Avaliacao avaliacao;

    @OneToMany(mappedBy = "ordem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataHora ASC, id ASC")
    private List<HistoricoStatusOrdem> historico = new ArrayList<>();

    protected OrdemServico() {
        // exigido pelo JPA
    }

    public OrdemServico(Cliente cliente, ServicoBase servico, BigDecimal horasEstimadas) {
        this.cliente = cliente;
        this.servico = servico;
        this.horasEstimadas = horasEstimadas;
        this.dataAbertura = LocalDate.now();
        this.status = StatusOrdem.ABERTA;
        // Polimorfismo: a mesma chamada executa a lógica de preço do tipo concreto da oferta.
        this.valorFinal = servico.calcularOrcamento(horasEstimadas);
        registrarHistorico(StatusOrdem.ABERTA);
    }

    /** Muda o status respeitando o ciclo definido em StatusOrdem e registra data e hora. */
    public void alterarStatus(StatusOrdem novoStatus) {
        if (!status.podeTransitarPara(novoStatus)) {
            throw new TransicaoStatusInvalidaException(
                    "Não é possível mudar a ordem de " + status.getDescricao() + " para " + novoStatus.getDescricao());
        }
        this.status = novoStatus;
        registrarHistorico(novoStatus);
    }

    /**
     * Regra inegociável do produto: só ordem concluída é avaliada, uma única vez.
     * A nota média do prestador é recalculada na mesma operação.
     */
    public Avaliacao avaliar(Integer nota, String comentario) {
        if (status != StatusOrdem.CONCLUIDA) {
            throw new RegraNegocioException("Só é possível avaliar uma ordem de serviço concluída");
        }
        if (avaliacao != null) {
            throw new ConflitoException("Esta ordem de serviço já foi avaliada e a avaliação não pode ser alterada");
        }
        this.avaliacao = new Avaliacao(this, nota, comentario, LocalDate.now());
        getPrestador().registrarAvaliacao(avaliacao);
        return avaliacao;
    }

    private void registrarHistorico(StatusOrdem novoStatus) {
        historico.add(new HistoricoStatusOrdem(this, novoStatus, LocalDateTime.now()));
    }

    public boolean pertenceAoCliente(Integer clienteId) {
        return cliente.getId().equals(clienteId);
    }

    public boolean pertenceAoPrestador(Integer prestadorId) {
        return servico.pertenceA(prestadorId);
    }

    /** Código legível da ordem, como no diagrama de classes (derivado do id, sem coluna própria). */
    public String getCodigo() {
        return id == null ? null : String.format("OS-%05d", id);
    }

    public Prestador getPrestador() {
        return servico.getPrestador();
    }

    public Integer getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public ServicoBase getServico() {
        return servico;
    }

    public LocalDate getDataAbertura() {
        return dataAbertura;
    }

    public StatusOrdem getStatus() {
        return status;
    }

    public BigDecimal getHorasEstimadas() {
        return horasEstimadas;
    }

    public BigDecimal getValorFinal() {
        return valorFinal;
    }

    public Avaliacao getAvaliacao() {
        return avaliacao;
    }

    public List<HistoricoStatusOrdem> getHistorico() {
        return Collections.unmodifiableList(historico);
    }
}
