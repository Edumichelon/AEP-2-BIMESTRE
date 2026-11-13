// RF04: comparação lado a lado de até 3 prestadores (estado em memória) e contratação sem recarregar (RF05).
import { api } from '../api.js';
import { estado, eventos, removerDaComparacao } from '../estado.js';
import { COBRANCA, esc, estrelas, km, moeda, numero, precoComUnidade, toast } from '../ui.js';

/** Linhas comparadas; "melhor" diz qual extremo vence (menor ou maior). */
const LINHAS = [
    { rotulo: 'Valor estimado', valor: (r) => Number(r.valorEstimado), texto: (r) => moeda(r.valorEstimado), melhor: 'menor' },
    { rotulo: 'Nota média', valor: (r) => Number(r.notaMedia), texto: (r) => `${numero(r.notaMedia, 1)} (${r.quantidadeAvaliacoes})`, melhor: 'maior' },
    { rotulo: 'Serviços concluídos', valor: (r) => r.servicosConcluidos, texto: (r) => String(r.servicosConcluidos), melhor: 'maior' },
    { rotulo: 'Distância', valor: (r) => r.distanciaKm, texto: (r) => km(r.distanciaKm), melhor: 'menor' },
    { rotulo: 'Cobrança', texto: (r) => COBRANCA[r.tipoCobranca] },
    { rotulo: 'Preço base', texto: (r) => precoComUnidade(r) },
];

export function telaComparar(app) {
    const desenhar = () => {
        const itens = estado.comparacao;
        if (!itens.length) {
            app.innerHTML = `
                <div class="cabecalho-tela"><div><div class="rotulo-tela">Etapa 2 · comparação</div><h1>Comparar prestadores</h1></div></div>
                <div class="folha vazio">
                    <p>Nenhum prestador selecionado. Na busca, toque em <strong>+ Comparar</strong> em até 3 resultados.</p>
                    <a class="botao" href="#/buscar">Ir para a busca</a>
                </div>`;
            return;
        }
        const vencedores = calcularVencedores(itens);
        app.innerHTML = `
            <div class="cabecalho-tela">
                <div>
                    <div class="rotulo-tela">Etapa 2 · comparação</div>
                    <h1>Comparar prestadores</h1>
                    <p>${itens.length} de 3 selecionados. Destaque verde = melhor valor da linha.</p>
                </div>
                <a class="botao botao-secundario" href="#/buscar">← Voltar à busca</a>
            </div>
            <div class="comparacao" style="--colunas:${itens.length}">
                ${itens.map((r) => coluna(r, vencedores)).join('')}
            </div>`;
    };

    app.addEventListener('click', aoClicar);
    app.addEventListener('input', aoDigitarHoras);
    app.addEventListener('submit', aoContratar);
    eventos.addEventListener('comparacao', desenhar);
    desenhar();

    return () => {
        app.removeEventListener('click', aoClicar);
        app.removeEventListener('input', aoDigitarHoras);
        app.removeEventListener('submit', aoContratar);
        eventos.removeEventListener('comparacao', desenhar);
    };
}

function calcularVencedores(itens) {
    const vencedores = {};
    if (itens.length < 2) return vencedores;
    LINHAS.filter((l) => l.melhor).forEach((linha) => {
        const valores = itens.map(linha.valor);
        const alvo = linha.melhor === 'menor' ? Math.min(...valores) : Math.max(...valores);
        // só destaca quando há diferença entre os prestadores
        if (valores.some((v) => v !== alvo)) {
            vencedores[linha.rotulo] = new Set(itens.filter((r) => linha.valor(r) === alvo).map((r) => r.servicoId));
        }
    });
    return vencedores;
}

function coluna(r, vencedores) {
    return `
        <article class="folha coluna-comp" data-servico="${r.servicoId}">
            <header>
                <h2>${esc(r.prestadorNome)}</h2>
                <p class="sub" style="margin:0">${esc(r.descricao)}</p>
                ${estrelas(r.notaMedia)}
            </header>
            <dl>
                ${LINHAS.map((l) => `
                    <div class="linha-comp ${vencedores[l.rotulo]?.has(r.servicoId) ? 'melhor' : ''}">
                        <dt>${l.rotulo}</dt><dd>${esc(l.texto(r))}</dd>
                    </div>`).join('')}
            </dl>
            <footer>
                <form class="contratar" data-contratar="${r.servicoId}" novalidate>
                    ${r.tipoCobranca === 'POR_HORA' ? `
                        <div class="campo">
                            <label for="horas-${r.servicoId}">Horas estimadas</label>
                            <input id="horas-${r.servicoId}" name="horasEstimadas" type="number" min="0.5" max="200" step="0.5"
                                   value="${esc(r.horasConsideradas)}" required>
                        </div>` : ''}
                    <p class="total-estimado">Total: <strong data-total>${moeda(r.valorEstimado)}</strong></p>
                    <button class="botao botao-bloco" type="submit">Contratar · abrir OS</button>
                </form>
                <div class="acoes">
                    <a class="botao botao-secundario botao-pequeno" href="#/prestadores/${r.prestadorId}">Ver perfil</a>
                    <button type="button" class="botao botao-perigo botao-pequeno" data-remover="${r.servicoId}">Remover</button>
                </div>
            </footer>
        </article>`;
}

function aoClicar(e) {
    const remover = e.target.closest('[data-remover]');
    if (remover) removerDaComparacao(Number(remover.dataset.remover));
}

/** Atualiza o total da coluna enquanto o cliente ajusta as horas (mesma regra do back-end). */
function aoDigitarHoras(e) {
    if (e.target.name !== 'horasEstimadas') return;
    const form = e.target.closest('[data-contratar]');
    const item = estado.comparacao.find((r) => r.servicoId === Number(form.dataset.contratar));
    const horas = Number(e.target.value);
    form.querySelector('[data-total]').textContent = horas > 0 ? moeda(Number(item.valorBase) * horas) : '—';
}

async function aoContratar(e) {
    const form = e.target.closest('[data-contratar]');
    if (!form) return;
    e.preventDefault();
    const servicoId = Number(form.dataset.contratar);
    const item = estado.comparacao.find((r) => r.servicoId === servicoId);
    const horas = form.horasEstimadas ? Number(form.horasEstimadas.value) : null;
    if (form.horasEstimadas && !(horas >= 0.5)) {
        toast('Informe as horas estimadas (mínimo 0,5 h)', 'erro');
        form.horasEstimadas.focus();
        return;
    }
    if (!confirm(`Abrir ordem de serviço com ${item.prestadorNome} para "${item.descricao}"?`)) return;
    const botao = form.querySelector('[type="submit"]');
    botao.disabled = true;
    try {
        const ordem = await api('/ordens', { metodo: 'POST', corpo: { servicoId, horasEstimadas: horas } });
        toast(`Ordem ${ordem.codigo} aberta! Aguarde o aceite do prestador.`);
        removerDaComparacao(servicoId);
        location.hash = `#/ordens/${ordem.id}`;
    } catch (erro) {
        toast(erro.message, 'erro');
        botao.disabled = false;
    }
}
