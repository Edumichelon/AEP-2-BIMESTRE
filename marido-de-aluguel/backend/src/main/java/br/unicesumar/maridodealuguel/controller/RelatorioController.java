package br.unicesumar.maridodealuguel.controller;

import br.unicesumar.maridodealuguel.dto.RelatorioPrestadorResponse;
import br.unicesumar.maridodealuguel.security.UsuarioAutenticado;
import br.unicesumar.maridodealuguel.service.RelatorioCsv;
import br.unicesumar.maridodealuguel.service.RelatorioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * RF07: o prestador consulta e exporta os próprios indicadores.
 * Não existe parâmetro de prestador: o id vem do token JWT, então ninguém exporta dados de outra pessoa.
 */
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private static final MediaType CSV = MediaType.parseMediaType("text/csv;charset=UTF-8");

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/prestador")
    @PreAuthorize("hasRole('PRESTADOR')")
    public RelatorioPrestadorResponse meuRelatorio(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return relatorioService.gerar(usuario.id(), inicio, fim);
    }

    /** Exportação segura (RF07): só com token de prestador, só os próprios dados, sem cache. */
    @GetMapping("/prestador/exportar")
    @PreAuthorize("hasRole('PRESTADOR')")
    public ResponseEntity<byte[]> exportarCsv(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        RelatorioPrestadorResponse relatorio = relatorioService.gerar(usuario.id(), inicio, fim);
        String arquivo = "relatorio-" + relatorio.inicio() + "-a-" + relatorio.fim() + ".csv";
        return ResponseEntity.ok()
                .contentType(CSV)
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(arquivo).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(RelatorioCsv.gerar(relatorio));
    }
}
