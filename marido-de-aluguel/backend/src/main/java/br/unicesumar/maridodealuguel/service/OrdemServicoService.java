package br.unicesumar.maridodealuguel.service;

import br.unicesumar.maridodealuguel.domain.Avaliacao;
import br.unicesumar.maridodealuguel.domain.Cliente;
import br.unicesumar.maridodealuguel.domain.ServicoBase;
import br.unicesumar.maridodealuguel.domain.OrdemServico;
import br.unicesumar.maridodealuguel.domain.PerfilUsuario;
import br.unicesumar.maridodealuguel.domain.Prestador;
import br.unicesumar.maridodealuguel.domain.StatusOrdem;
import br.unicesumar.maridodealuguel.domain.TipoCobranca;
import br.unicesumar.maridodealuguel.dto.AbrirOrdemRequest;
import br.unicesumar.maridodealuguel.dto.AvaliacaoRequest;
import br.unicesumar.maridodealuguel.dto.AvaliacaoResponse;
import br.unicesumar.maridodealuguel.dto.OrdemServicoResponse;
import br.unicesumar.maridodealuguel.exception.AcessoNegadoException;
import br.unicesumar.maridodealuguel.exception.RecursoNaoEncontradoException;
import br.unicesumar.maridodealuguel.exception.RegraNegocioException;
import br.unicesumar.maridodealuguel.repository.ClienteRepository;
import br.unicesumar.maridodealuguel.repository.ServicoRepository;
import br.unicesumar.maridodealuguel.repository.OrdemServicoRepository;
import br.unicesumar.maridodealuguel.security.UsuarioAutenticado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/** RF05 (abertura e acompanhamento da ordem) e RF06 (avaliação após a conclusão). */
@Service
public class OrdemServicoService {

    private final OrdemServicoRepository ordemRepository;
    private final ServicoRepository servicoRepository;
    private final ClienteRepository clienteRepository;

    public OrdemServicoService(OrdemServicoRepository ordemRepository, ServicoRepository servicoRepository,
                               ClienteRepository clienteRepository) {
        this.ordemRepository = ordemRepository;
        this.servicoRepository = servicoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public OrdemServicoResponse abrir(UsuarioAutenticado usuario, AbrirOrdemRequest dados) {
        Cliente cliente = clienteRepository.findById(usuario.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
        ServicoBase servico = servicoRepository.findById(dados.servicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado"));

        Prestador prestador = servico.getPrestador();
        double distancia = Haversine.distanciaKm(cliente.getLatitude(), cliente.getLongitude(),
                prestador.getLatitude(), prestador.getLongitude());
        if (distancia > servico.getRaioAtendimentoKm()) {
            throw new RegraNegocioException("Este prestador não atende a sua região (raio de "
                    + servico.getRaioAtendimentoKm() + " km)");
        }

        BigDecimal horas = servico.getTipoCobranca() == TipoCobranca.POR_HORA ? dados.horasEstimadas() : null;
        OrdemServico ordem = ordemRepository.save(new OrdemServico(cliente, servico, horas));
        return paraResposta(ordem, usuario);
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listar(UsuarioAutenticado usuario) {
        List<OrdemServico> ordens = usuario.perfil() == PerfilUsuario.CLIENTE
                ? ordemRepository.listarDoCliente(usuario.id())
                : ordemRepository.listarDoPrestador(usuario.id());
        return ordens.stream().map(ordem -> paraResposta(ordem, usuario)).toList();
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponse detalhar(UsuarioAutenticado usuario, Integer ordemId) {
        return paraResposta(carregarComoParticipante(usuario, ordemId), usuario);
    }

    @Transactional
    public OrdemServicoResponse alterarStatus(UsuarioAutenticado usuario, Integer ordemId, StatusOrdem novoStatus) {
        OrdemServico ordem = carregarComoParticipante(usuario, ordemId);
        if (!perfilPodeAplicar(usuario.perfil(), novoStatus)) {
            throw new AcessoNegadoException("Seu perfil não pode mudar a ordem para " + novoStatus.getDescricao());
        }
        ordem.alterarStatus(novoStatus); // valida a transição dentro do domínio
        ordemRepository.flush();
        return paraResposta(ordem, usuario);
    }

    @Transactional
    public AvaliacaoResponse avaliar(UsuarioAutenticado usuario, Integer ordemId, AvaliacaoRequest dados) {
        OrdemServico ordem = carregar(ordemId);
        if (!ordem.pertenceAoCliente(usuario.id())) {
            throw new AcessoNegadoException("Só o cliente que contratou pode avaliar esta ordem");
        }
        String comentario = dados.comentario() == null || dados.comentario().isBlank()
                ? null : dados.comentario().trim();
        Avaliacao avaliacao = ordem.avaliar(dados.nota(), comentario);
        ordemRepository.flush(); // UNIQUE(ordem_id) no banco barra uma avaliação concorrente
        return AvaliacaoResponse.de(avaliacao);
    }

    /** Quem aplica cada status: o prestador conduz a execução; os dois lados podem cancelar. */
    private boolean perfilPodeAplicar(PerfilUsuario perfil, StatusOrdem destino) {
        return switch (destino) {
            case ACEITA, EM_EXECUCAO, CONCLUIDA -> perfil == PerfilUsuario.PRESTADOR;
            case CANCELADA -> true;
            case ABERTA -> false;
        };
    }

    private OrdemServicoResponse paraResposta(OrdemServico ordem, UsuarioAutenticado usuario) {
        List<StatusOrdem> transicoes = Arrays.stream(StatusOrdem.values())
                .filter(destino -> ordem.getStatus().podeTransitarPara(destino))
                .filter(destino -> perfilPodeAplicar(usuario.perfil(), destino))
                .toList();
        boolean podeAvaliar = usuario.perfil() == PerfilUsuario.CLIENTE
                && ordem.getStatus() == StatusOrdem.CONCLUIDA
                && ordem.getAvaliacao() == null;
        return OrdemServicoResponse.de(ordem, podeAvaliar, transicoes);
    }

    private OrdemServico carregar(Integer ordemId) {
        return ordemRepository.findById(ordemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada"));
    }

    /** RNF02: só o cliente e o prestador da ordem enxergam e alteram a ordem. */
    private OrdemServico carregarComoParticipante(UsuarioAutenticado usuario, Integer ordemId) {
        OrdemServico ordem = carregar(ordemId);
        boolean participante = usuario.perfil() == PerfilUsuario.CLIENTE
                ? ordem.pertenceAoCliente(usuario.id())
                : ordem.pertenceAoPrestador(usuario.id());
        if (!participante) {
            throw new AcessoNegadoException("Você não participa desta ordem de serviço");
        }
        return ordem;
    }
}
