package br.unicesumar.maridodealuguel.repository;

import br.unicesumar.maridodealuguel.domain.ServicoBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface ServicoRepository extends JpaRepository<ServicoBase, Integer> {

    List<ServicoBase> findByPrestadorIdOrderByIdDesc(Integer prestadorId);

    @Query("select s from ServicoBase s join fetch s.prestador where s.id in :ids")
    List<ServicoBase> findComPrestadorByIdIn(@Param("ids") Collection<Integer> ids);

    @Query("select distinct s.categoria from ServicoBase s order by s.categoria")
    List<String> listarCategorias();

    /**
     * Troca o modelo de cobrança de uma oferta já cadastrada. O JPA não muda a classe de uma entidade
     * gerenciada, então o discriminador é atualizado direto no banco e o contexto é limpo em seguida.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE servicos SET tipo_cobranca = :tipo WHERE id = :id", nativeQuery = true)
    void alterarTipoCobranca(@Param("id") Integer id, @Param("tipo") String tipo);

    /**
     * Busca do RF03 executada no próprio banco (RNF01): filtra por categoria, faixa de preço, nota mínima
     * e distância, calcula a distância cliente-prestador pela fórmula de Haversine (raio da Terra = 6371 km)
     * e só mantém prestadores cujo raio de atendimento alcança o cliente.
     * Devolve pares [id do serviço, distância em km], já ordenados.
     */
    @Query(value = """
            SELECT b.id, b.distancia_km FROM (
                SELECT s.id, s.valor_base, s.raio_atendimento_km, u.nota_media,
                       2 * 6371 * ASIN(LEAST(1, SQRT(
                           POWER(SIN(RADIANS(u.latitude - :lat) / 2), 2)
                           + COS(RADIANS(:lat)) * COS(RADIANS(u.latitude))
                           * POWER(SIN(RADIANS(u.longitude - :lng) / 2), 2)))) AS distancia_km
                FROM servicos s
                JOIN usuarios u ON u.id = s.prestador_id
                WHERE s.categoria = :categoria
                  AND s.valor_base BETWEEN :precoMin AND :precoMax
                  AND u.nota_media >= :notaMin
            ) b
            WHERE b.distancia_km <= b.raio_atendimento_km
              AND b.distancia_km <= :distanciaMax
            ORDER BY
                CASE WHEN :ordenarPor = 'PRECO' THEN b.valor_base END ASC,
                CASE WHEN :ordenarPor = 'NOTA' THEN b.nota_media END DESC,
                b.distancia_km ASC,
                b.id ASC
            LIMIT 50
            """, nativeQuery = true)
    List<Object[]> buscarComDistancia(@Param("categoria") String categoria,
                                      @Param("precoMin") BigDecimal precoMin,
                                      @Param("precoMax") BigDecimal precoMax,
                                      @Param("notaMin") BigDecimal notaMin,
                                      @Param("distanciaMax") double distanciaMax,
                                      @Param("lat") double latitude,
                                      @Param("lng") double longitude,
                                      @Param("ordenarPor") String ordenarPor);
}
