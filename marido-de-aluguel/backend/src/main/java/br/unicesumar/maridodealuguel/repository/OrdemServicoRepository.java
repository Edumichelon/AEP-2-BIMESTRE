package br.unicesumar.maridodealuguel.repository;

import br.unicesumar.maridodealuguel.domain.OrdemServico;
import br.unicesumar.maridodealuguel.domain.StatusOrdem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Integer> {

    @Query("""
            select o from OrdemServico o join fetch o.servico s join fetch s.prestador join fetch o.cliente
            where o.cliente.id = :clienteId order by o.id desc""")
    List<OrdemServico> listarDoCliente(@Param("clienteId") Integer clienteId);

    @Query("""
            select o from OrdemServico o join fetch o.servico s join fetch s.prestador join fetch o.cliente
            where s.prestador.id = :prestadorId order by o.id desc""")
    List<OrdemServico> listarDoPrestador(@Param("prestadorId") Integer prestadorId);

    boolean existsByServicoId(Integer servicoId);

    long countByServicoPrestadorIdAndStatus(Integer prestadorId, StatusOrdem status);

    /** Quantidade de serviços concluídos por prestador, em lote (usado na busca e na comparação). */
    @Query("""
            select o.servico.prestador.id, count(o) from OrdemServico o
            where o.status = :status and o.servico.prestador.id in :prestadorIds
            group by o.servico.prestador.id""")
    List<Object[]> contarPorPrestadorEStatus(@Param("prestadorIds") Collection<Integer> prestadorIds,
                                             @Param("status") StatusOrdem status);

    /** RF07: ordens do prestador concluídas dentro do período (data e hora da mudança para CONCLUIDA). */
    @Query("""
            select distinct o from OrdemServico o join o.historico h
            where o.servico.prestador.id = :prestadorId
              and o.status = :concluida and h.status = :concluida
              and h.dataHora >= :inicio and h.dataHora < :fim""")
    List<OrdemServico> listarConcluidasNoPeriodo(@Param("prestadorId") Integer prestadorId,
                                                  @Param("concluida") StatusOrdem concluida,
                                                  @Param("inicio") LocalDateTime inicio,
                                                  @Param("fim") LocalDateTime fim);
}
