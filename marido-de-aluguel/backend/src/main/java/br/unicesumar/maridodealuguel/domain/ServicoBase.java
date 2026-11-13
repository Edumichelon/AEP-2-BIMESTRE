package br.unicesumar.maridodealuguel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Classe abstrata intermediária entre a interface Servico e os modelos de cobrança concretos.
 * Existe para que o JPA consiga mapear o polimorfismo: tabela única servicos com tipo_cobranca
 * como discriminador. O método calcularOrcamento() continua abstrato aqui.
 */
@Entity
@Table(name = "servicos")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_cobranca", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class ServicoBase implements Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prestador_id", nullable = false)
    private Prestador prestador;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "raio_atendimento_km", nullable = false)
    private Integer raioAtendimentoKm;

    @Column(name = "valor_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorBase;

    protected ServicoBase() {
        // exigido pelo JPA
    }

    protected ServicoBase(Prestador prestador, String categoria, String descricao,
                          Integer raioAtendimentoKm, BigDecimal valorBase) {
        this.prestador = prestador;
        atualizar(categoria, descricao, raioAtendimentoKm, valorBase);
    }

    /** Modelo de cobrança da subclasse concreta (cada uma sobrescreve). */
    public abstract TipoCobranca getTipoCobranca();

    /** Campos do RF02: categoria, descrição, raio de atendimento e valor base. */
    public final void atualizar(String categoria, String descricao, Integer raioAtendimentoKm, BigDecimal valorBase) {
        this.categoria = categoria;
        this.descricao = descricao;
        this.raioAtendimentoKm = raioAtendimentoKm;
        this.valorBase = valorBase;
    }

    public boolean pertenceA(Integer prestadorId) {
        return prestador.getId().equals(prestadorId);
    }

    public Integer getId() {
        return id;
    }

    public Prestador getPrestador() {
        return prestador;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public Integer getRaioAtendimentoKm() {
        return raioAtendimentoKm;
    }

    public BigDecimal getValorBase() {
        return valorBase;
    }
}
