package br.unicesumar.maridodealuguel.service;

import br.unicesumar.maridodealuguel.domain.Cliente;
import br.unicesumar.maridodealuguel.domain.ServicoBase;
import br.unicesumar.maridodealuguel.domain.ServicoPorEmpreitada;
import br.unicesumar.maridodealuguel.domain.ServicoPorHora;
import br.unicesumar.maridodealuguel.domain.Prestador;
import br.unicesumar.maridodealuguel.domain.StatusOrdem;
import br.unicesumar.maridodealuguel.dto.ServicoRequest;
import br.unicesumar.maridodealuguel.dto.ServicoResponse;
import br.unicesumar.maridodealuguel.dto.OrdenacaoBusca;
import br.unicesumar.maridodealuguel.dto.ResultadoBuscaResponse;
import br.unicesumar.maridodealuguel.exception.AcessoNegadoException;
import br.unicesumar.maridodealuguel.exception.ConflitoException;
import br.unicesumar.maridodealuguel.exception.RecursoNaoEncontradoException;
import br.unicesumar.maridodealuguel.exception.RegraNegocioException;
import br.unicesumar.maridodealuguel.repository.ClienteRepository;
import br.unicesumar.maridodealuguel.repository.ServicoRepository;
import br.unicesumar.maridodealuguel.repository.OrdemServicoRepository;
import br.unicesumar.maridodealuguel.repository.PrestadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** RF02 (CRUD de ofertas pelo prestador) e RF03 (busca pelo cliente). */
@Service
public class ServicoService {

    private static final BigDecimal PRECO_MAXIMO = new BigDecimal("99999999.99");
    private static final double DISTANCIA_SEM_LIMITE = 20_000;

    private final ServicoRepository servicoRepository;
    private final PrestadorRepository prestadorRepository;
    private final ClienteRepository clienteRepository;
    private final OrdemServicoRepository ordemRepository;

    public ServicoService(ServicoRepository servicoRepository, PrestadorRepository prestadorRepository,
                                ClienteRepository clienteRepository, OrdemServicoRepository ordemRepository) {
        this.servicoRepository = servicoRepository;
        this.prestadorRepository = prestadorRepository;
        this.clienteRepository = clienteRepository;
        this.ordemRepository = ordemRepository;
    }

    // ---------- CRUD (RF02) ----------

    @Transactional
    public ServicoResponse criar(Integer prestadorId, ServicoRequest dados) {
        Prestador prestador = prestadorRepository.findById(prestadorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prestador não encontrado"));
        ServicoBase servico = novoServico(prestador, dados);
        prestador.adicionarServico(servico);
        return ServicoResponse.de(servicoRepository.save(servico));
    }

    @Transactional(readOnly = true)
    public List<ServicoResponse> listarDoPrestador(Integer prestadorId) {
        return servicoRepository.findByPrestadorIdOrderByIdDesc(prestadorId).stream()
                .map(ServicoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServicoResponse buscarPorId(Integer id) {
        return ServicoResponse.de(carregar(id));
    }

    @Transactional
    public ServicoResponse atualizar(Integer prestadorId, Integer id, ServicoRequest dados) {
        ServicoBase servico = carregarDoPrestador(prestadorId, id);
        if (servico.getTipoCobranca() != dados.tipoCobranca()) {
            servicoRepository.alterarTipoCobranca(id, dados.tipoCobranca().name());
            servico = carregar(id); // recarrega já como a nova subclasse
        }
        servico.atualizar(dados.categoria().trim(), dados.descricao().trim(), dados.raioAtendimentoKm(),
                dados.valorBase());
        return ServicoResponse.de(servico);
    }

    @Transactional
    public void excluir(Integer prestadorId, Integer id) {
        ServicoBase servico = carregarDoPrestador(prestadorId, id);
        if (ordemRepository.existsByServicoId(id)) {
            throw new ConflitoException(
                    "Este serviço já foi contratado e faz parte do histórico de ordens de serviço, por isso não pode ser excluído");
        }
        servico.getPrestador().removerServico(servico); // orphanRemoval apaga a linha
    }

    @Transactional(readOnly = true)
    public List<String> listarCategorias() {
        return servicoRepository.listarCategorias();
    }

    // ---------- Busca (RF03) ----------

    @Transactional(readOnly = true)
    public List<ResultadoBuscaResponse> buscar(Integer clienteId, String categoria, BigDecimal precoMin,
                                               BigDecimal precoMax, BigDecimal notaMin, Double distanciaMaxKm,
                                               OrdenacaoBusca ordenarPor, BigDecimal horas) {
        if (categoria == null || categoria.isBlank()) {
            throw new RegraNegocioException("Escolha uma categoria para buscar");
        }
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
        BigDecimal horasConsideradas = horas == null || horas.signum() <= 0 ? BigDecimal.ONE : horas;

        List<Object[]> linhas = servicoRepository.buscarComDistancia(categoria.trim(),
                precoMin == null ? BigDecimal.ZERO : precoMin,
                precoMax == null ? PRECO_MAXIMO : precoMax,
                notaMin == null ? BigDecimal.ZERO : notaMin,
                distanciaMaxKm == null ? DISTANCIA_SEM_LIMITE : distanciaMaxKm,
                cliente.getLatitude(), cliente.getLongitude(),
                (ordenarPor == null ? OrdenacaoBusca.DISTANCIA : ordenarPor).name());
        if (linhas.isEmpty()) {
            return List.of();
        }

        Map<Integer, Double> distancias = new HashMap<>();
        for (Object[] linha : linhas) {
            distancias.put(((Number) linha[0]).intValue(), ((Number) linha[1]).doubleValue());
        }
        Map<Integer, ServicoBase> servicos = servicoRepository.findComPrestadorByIdIn(distancias.keySet()).stream()
                .collect(Collectors.toMap(ServicoBase::getId, Function.identity()));
        Map<Integer, Long> concluidos = contarConcluidos(servicos.values());

        return linhas.stream()
                .map(linha -> servicos.get(((Number) linha[0]).intValue()))
                .map(servico -> paraResultado(servico, horasConsideradas, distancias.get(servico.getId()),
                        concluidos.getOrDefault(servico.getPrestador().getId(), 0L)))
                .toList();
    }

    private ResultadoBuscaResponse paraResultado(ServicoBase servico, BigDecimal horas, double distanciaKm,
                                                 long servicosConcluidos) {
        Prestador prestador = servico.getPrestador();
        return new ResultadoBuscaResponse(servico.getId(), servico.getCategoria(), servico.getDescricao(), servico.getTipoCobranca(), servico.getValorBase(),
                servico.calcularOrcamento(horas), // polimorfismo: por hora ou por empreitada
                horas, servico.getRaioAtendimentoKm(), prestador.getId(), prestador.getNome(),
                prestador.getTelefone(), prestador.getNotaMedia(), prestador.getQuantidadeAvaliacoes(),
                servicosConcluidos,
                BigDecimal.valueOf(distanciaKm).setScale(1, RoundingMode.HALF_UP).doubleValue());
    }

    private Map<Integer, Long> contarConcluidos(Collection<ServicoBase> servicos) {
        List<Integer> prestadorIds = servicos.stream().map(o -> o.getPrestador().getId()).distinct().toList();
        Map<Integer, Long> resultado = new HashMap<>();
        for (Object[] linha : ordemRepository.contarPorPrestadorEStatus(prestadorIds, StatusOrdem.CONCLUIDA)) {
            resultado.put((Integer) linha[0], (Long) linha[1]);
        }
        return resultado;
    }

    // ---------- apoio ----------

    /** Fábrica: escolhe a subclasse concreta conforme o modelo de cobrança. */
    private ServicoBase novoServico(Prestador prestador, ServicoRequest dados) {
        return switch (dados.tipoCobranca()) {
            case POR_HORA -> new ServicoPorHora(prestador, dados.categoria().trim(), dados.descricao().trim(),
                    dados.raioAtendimentoKm(), dados.valorBase());
            case POR_EMPREITADA -> new ServicoPorEmpreitada(prestador, dados.categoria().trim(),
                    dados.descricao().trim(), dados.raioAtendimentoKm(), dados.valorBase());
        };
    }

    private ServicoBase carregar(Integer id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado"));
    }

    /** RNF02: o prestador só mexe nas próprias ofertas. */
    private ServicoBase carregarDoPrestador(Integer prestadorId, Integer id) {
        ServicoBase servico = carregar(id);
        if (!servico.pertenceA(prestadorId)) {
            throw new AcessoNegadoException("Este serviço pertence a outro prestador");
        }
        return servico;
    }
}
