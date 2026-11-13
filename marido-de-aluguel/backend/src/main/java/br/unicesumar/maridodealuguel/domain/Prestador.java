package br.unicesumar.maridodealuguel.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@DiscriminatorValue("PRESTADOR")
public class Prestador extends Usuario {

    /** Sem setter público: só muda por registrarAvaliacao (encapsulamento da reputação). */
    @Column(name = "nota_media", precision = 3, scale = 2)
    private BigDecimal notaMedia = BigDecimal.ZERO;

    @Column(name = "quantidade_avaliacoes")
    private Integer quantidadeAvaliacoes = 0;

    /** Composição 1:N: a oferta não existe sem o prestador (cascade + orphanRemoval). */
    @OneToMany(mappedBy = "prestador", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<ServicoBase> servicos = new ArrayList<>();

    protected Prestador() {
        // exigido pelo JPA
    }

    public Prestador(String nome, String email, String telefone, String senhaHash,
                     String endereco, Double latitude, Double longitude) {
        super(nome, email, telefone, senhaHash, endereco, latitude, longitude);
    }

    @Override
    public PerfilUsuario getPerfil() {
        return PerfilUsuario.PRESTADOR;
    }

    /**
     * Recalcula a nota média a partir de uma nova avaliação válida (RF06).
     * Média incremental: (média atual x quantidade + nova nota) / (quantidade + 1).
     */
    public void registrarAvaliacao(Avaliacao avaliacao) {
        BigDecimal somaAtual = notaMedia.multiply(BigDecimal.valueOf(quantidadeAvaliacoes));
        quantidadeAvaliacoes = quantidadeAvaliacoes + 1;
        notaMedia = somaAtual.add(BigDecimal.valueOf(avaliacao.getNota()))
                .divide(BigDecimal.valueOf(quantidadeAvaliacoes), 2, RoundingMode.HALF_UP);
    }

    public void adicionarServico(ServicoBase servico) {
        servicos.add(servico);
    }

    public void removerServico(ServicoBase servico) {
        servicos.remove(servico);
    }

    public BigDecimal getNotaMedia() {
        return notaMedia;
    }

    public Integer getQuantidadeAvaliacoes() {
        return quantidadeAvaliacoes;
    }

    public List<ServicoBase> getServicos() {
        return Collections.unmodifiableList(servicos);
    }
}
