package br.unicesumar.maridodealuguel.repository;

import br.unicesumar.maridodealuguel.domain.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Na prática só leitura: avaliações nascem em OrdemServico.avaliar() e nunca são alteradas. */
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Integer> {

    @Query("""
            select a from Avaliacao a join fetch a.ordem o join fetch o.cliente
            where o.servico.prestador.id = :prestadorId
            order by a.dataAvaliacao asc, a.id asc""")
    List<Avaliacao> listarDoPrestador(@Param("prestadorId") Integer prestadorId);
}
