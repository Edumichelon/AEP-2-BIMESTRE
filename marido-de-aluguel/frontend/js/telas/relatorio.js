// RF07: relatório consolidado do prestador, com exportação segura em CSV.
import { api, baixar } from '../api.js';
import { carregando, data, dataHora, esc, hojeIso, moeda, numero, toast } from '../ui.js';

const MESES = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez'];

export function telaRelatorio(app) {
    app.innerHTML = `
        <div class="cabecalho-tela">
            <div>
                <div class="rotulo-tela">Indicadores</div>
                <h1>Relatório do prestador</h1>
                <p>Só ordens efetivamente concluídas entram no faturamento.</p>
            </div>
        </div>
        <form id="form-periodo" class="folha" novalidate>
            <div class="linha-campos" style="align-items:end">
                <div class="campo"><label for="inicio">De</label><input id="inicio" name="inicio" type="date" value="${hojeIso(-90)}"></div>
                <div class="campo"><label for="fim">Até</label><input id="fim" name="fim" type="date" value="${hojeIso()}"></div>
                <div class="campo acoes" style="margin-top:0">
                    <button class="botao" type="submit">Atualizar</button>
                    <button class="botao botao-secundario" type="button" id="exportar">Exportar CSV</button>
                </div>
            </div>
        </form>
        <div id="relatorio" style="margin-top:1rem">${carregando()}</div>`;

    const form = app.querySelector('#form-periodo');
    const alvo = app.querySelector('#relatorio');

    app.querySelector('#exportar').addEventListener('click', async (e) => {
        const botao = e.currentTarget;
        botao.disabled = true;
        try {
            const nome = await baixar('/relatorios/prestador/exportar', { params: { inicio: form.inicio.value, fim: form.fim.value } });
            toast(`Relatório exportado: ${nome}`);
        } catch (erro) {
            toast(erro.message, 'erro');
        } finally {
            botao.disabled = false;
        }
    });

    const carregar = async () => {
        alvo.innerHTML = carregando();
        try {
            const r = await api('/relatorios/prestador', { params: { inicio: form.inicio.value, fim: form.fim.value } });
            alvo.innerHTML = renderRelatorio(r);
        } catch (erro) {
            alvo.innerHTML = `<div class="folha vazio">${esc(erro.message)}</div>`;
        }
    };

    form.addEventListener('submit', (e) => { e.preventDefault(); carregar(); });
    carregar();
}

function renderRelatorio(r) {
    const rotuloMes = (mes) => {
        const [ano, m] = mes.split('-');
        return `${MESES[Number(m) - 1]}/${ano.slice(2)}`;
    };
    return `
        <div class="indicadores">
            <div class="indicador"><span>Serviços concluídos</span><strong>${r.servicosConcluidos}</strong>
                <small>${data(r.inicio)} a ${data(r.fim)}</small></div>
            <div class="indicador"><span>Faturamento do período</span><strong>${moeda(r.faturamentoTotal)}</strong>
                <small>soma dos valores das ordens concluídas</small></div>
            <div class="indicador"><span>Nota média atual</span><strong>${numero(r.notaMediaAtual, 2)}</strong>
                <small>${r.quantidadeAvaliacoes} avaliação(ões)</small></div>
        </div>

        <section class="folha">
            <div class="folha-titulo"><strong>Evolução da nota média</strong><span>média acumulada por mês</span></div>
            ${r.evolucaoNotaMedia.length ? `
                <div class="grafico-barras" role="img" aria-label="Evolução da nota média por mês">
                    ${r.evolucaoNotaMedia.map((p) => `
                        <div class="barra" title="${p.avaliacoesNoMes} avaliação(ões) no mês">
                            <span class="valor">${numero(p.notaMediaAcumulada, 2)}</span>
                            <span class="coluna" style="height:${(Number(p.notaMediaAcumulada) / 5) * 100}%"></span>
                            <span class="mes">${rotuloMes(p.mes)}</span>
                        </div>`).join('')}
                </div>` : '<p class="vazio">Ainda não há avaliações.</p>'}
        </section>

        <section class="folha">
            <div class="folha-titulo"><strong>Ordens concluídas no período</strong><span>${r.ordensConcluidas.length}</span></div>
            ${r.ordensConcluidas.length ? `
                <div class="tabela-rolagem"><table>
                    <thead><tr><th>OS</th><th>Serviço</th><th>Cliente</th><th>Concluída em</th><th class="num">Valor</th></tr></thead>
                    <tbody>${r.ordensConcluidas.map((o) => `
                        <tr><td><a href="#/ordens/${o.ordemId}">${esc(o.codigo)}</a></td><td>${esc(o.descricaoServico)}</td>
                            <td>${esc(o.clienteNome)}</td><td>${dataHora(o.concluidaEm)}</td><td class="num">${moeda(o.valorFinal)}</td></tr>`).join('')}
                    </tbody>
                    <tfoot><tr><th colspan="4">Total</th><th class="num">${moeda(r.faturamentoTotal)}</th></tr></tfoot>
                </table></div>` : '<p class="vazio">Nenhuma ordem concluída neste período.</p>'}
        </section>`;
}
