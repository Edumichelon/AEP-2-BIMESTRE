package br.unicesumar.maridodealuguel.service;

import br.unicesumar.maridodealuguel.domain.Prestador;
import br.unicesumar.maridodealuguel.domain.StatusOrdem;
import br.unicesumar.maridodealuguel.domain.Usuario;
import br.unicesumar.maridodealuguel.dto.AvaliacaoResponse;
import br.unicesumar.maridodealuguel.dto.ServicoResponse;
import br.unicesumar.maridodealuguel.dto.PerfilUpdateRequest;
import br.unicesumar.maridodealuguel.dto.PrestadorPerfilResponse;
import br.unicesumar.maridodealuguel.dto.UsuarioResponse;
import br.unicesumar.maridodealuguel.exception.RecursoNaoEncontradoException;
import br.unicesumar.maridodealuguel.repository.AvaliacaoRepository;
import br.unicesumar.maridodealuguel.repository.OrdemServicoRepository;
import br.unicesumar.maridodealuguel.repository.PrestadorRepository;
import br.unicesumar.maridodealuguel.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PrestadorRepository prestadorRepository;
    private final OrdemServicoRepository ordemRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, PrestadorRepository prestadorRepository,
                          OrdemServicoRepository ordemRepository, AvaliacaoRepository avaliacaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.prestadorRepository = prestadorRepository;
        this.ordemRepository = ordemRepository;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPerfil(Integer usuarioId) {
        return UsuarioResponse.de(carregar(usuarioId));
    }

    @Transactional
    public UsuarioResponse atualizarPerfil(Integer usuarioId, PerfilUpdateRequest dados) {
        Usuario usuario = carregar(usuarioId);
        usuario.atualizarPerfil(dados.nome().trim(), dados.telefone().trim(), dados.endereco(),
                dados.latitude(), dados.longitude());
        return UsuarioResponse.de(usuario);
    }

    /** Perfil público do prestador, com ofertas e avaliações mais recentes primeiro. */
    @Transactional(readOnly = true)
    public PrestadorPerfilResponse perfilPublicoPrestador(Integer prestadorId) {
        Prestador prestador = prestadorRepository.findById(prestadorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Prestador não encontrado"));
        long concluidos = ordemRepository.countByServicoPrestadorIdAndStatus(prestadorId, StatusOrdem.CONCLUIDA);
        List<ServicoResponse> servicos = prestador.getServicos().stream().map(ServicoResponse::de).toList();
        List<AvaliacaoResponse> avaliacoes = avaliacaoRepository.listarDoPrestador(prestadorId).stream()
                .map(AvaliacaoResponse::de)
                .toList()
                .reversed();
        return new PrestadorPerfilResponse(prestador.getId(), prestador.getNome(), prestador.getTelefone(),
                prestador.getNotaMedia(), prestador.getQuantidadeAvaliacoes(), concluidos, servicos, avaliacoes);
    }

    private Usuario carregar(Integer usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }
}
