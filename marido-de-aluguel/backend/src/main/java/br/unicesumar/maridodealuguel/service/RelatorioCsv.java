package br.unicesumar.maridodealuguel.service;

import br.unicesumar.maridodealuguel.dto.RelatorioPrestadorResponse;
import br.unicesumar.maridodealuguel.dto.RelatorioPrestadorResponse.OrdemConcluida;
import br.unicesumar.maridodealuguel.dto.RelatorioPrestadorResponse.PontoEvolucaoNota;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * RF07: exportação do relatório em CSV (separador ";" e vírgula decimal, como o Excel em português espera).
 * Células que começam com =, +, - ou @ recebem um apóstrofo na frente para não virarem fórmula
 * ao abrir a planilha (proteção contra CSV injection).
 */
public final class RelatorioCsv {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String BOM = "﻿"; // faz o Excel reconhecer UTF-8 (acentos)

    private RelatorioCsv() {
    }

    public static byte[] gerar(RelatorioPrestadorResponse r) {
        StringBuilder csv = new StringBuilder(BOM);
        linha(csv, "Relatório do prestador", r.prestadorNome());
        linha(csv, "Período", r.inicio().format(DATA) + " a " + r.fim().format(DATA));
        linha(csv, "Serviços concluídos", String.valueOf(r.servicosConcluidos()));
        linha(csv, "Faturamento do período (R$)", decimal(r.faturamentoTotal()));
        linha(csv, "Nota média atual", decimal(r.notaMediaAtual()));
        linha(csv, "Quantidade de avaliações", String.valueOf(r.quantidadeAvaliacoes()));
        csv.append("\r\n");

        linha(csv, "Ordem", "Serviço", "Cliente", "Concluída em", "Valor (R$)");
        for (OrdemConcluida o : r.ordensConcluidas()) {
            linha(csv, o.codigo(), o.descricaoServico(), o.clienteNome(),
                    o.concluidaEm() == null ? "" : o.concluidaEm().format(DATA_HORA), decimal(o.valorFinal()));
        }
        csv.append("\r\n");

        linha(csv, "Mês", "Avaliações no mês", "Nota média acumulada");
        for (PontoEvolucaoNota p : r.evolucaoNotaMedia()) {
            linha(csv, p.mes(), String.valueOf(p.avaliacoesNoMes()), decimal(p.notaMediaAcumulada()));
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void linha(StringBuilder csv, String... celulas) {
        for (int i = 0; i < celulas.length; i++) {
            if (i > 0) csv.append(';');
            csv.append(celula(celulas[i]));
        }
        csv.append("\r\n");
    }

    private static String celula(String valor) {
        String texto = valor == null ? "" : valor;
        if (!texto.isEmpty() && "=+-@".indexOf(texto.charAt(0)) >= 0) {
            texto = "'" + texto;
        }
        if (texto.contains(";") || texto.contains("\"") || texto.contains("\n") || texto.contains("\r")) {
            texto = "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }

    private static String decimal(BigDecimal valor) {
        return valor == null ? "" : valor.toPlainString().replace('.', ',');
    }
}
