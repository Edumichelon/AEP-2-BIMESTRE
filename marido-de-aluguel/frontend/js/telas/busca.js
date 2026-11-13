// RF03: busca por categoria com filtros combináveis e ordenação. Os resultados alimentam a comparação (RF04).
import { api } from '../api.js';
import { alternarComparacao, estado, estaNaComparacao, eventos, MAX_COMPARACAO } from '../estado.js';
import { carregando, esc, estrelas, km, moeda, numero, precoComUnidade, COBRANCA, toast } from '../ui.js';

const FILTROS_PADRAO = {
    categoria: '', precoMin: '', precoMax: '', notaMin: '', distanciaMaxKm: '', ordenarPor: 'DISTANCIA', horas: '2',
};

export async function telaBuscar(app) {
    const filtros = { ...FILTROS_PADRAO, ...(estado.busca?.filtros ?? {}) };
    app.innerHTML = `
        <div class="cabecalho-tela">
            <div>
                <div class="rotulo-tela">Etapa 1 · busca</div>
                <h1>Encontre um prestador</h1>
                <p>Filtre por preço, nota e distância. Escolha até ${MAX_COMPARACAO} para comparar lado a lado.</p>
            </div>
        </div>
        <div class="grade-busca">
            <form id="form-busca" class="folha" novalidate>
                <div class="folha-titulo"><strong>Filtros</strong></div>
                <div class="campo">
                    <label for="categoria">Categoria</label>
                    <select id="categoria" name="categoria" required><option value="">Carregando…</option></select>
                </div>
                <div class="linha-campos">
                    <div class="campo">
                        <label for="precoMin">Preço base mín. (R$)</label>
                        <input id="precoMin" name="precoMin" type="number" min="0" step="1" inputmode="decimal" value="${esc(filtros.precoMin)}">
                    </div>
                    <div class="campo">
                        <label for="precoMax">Preço base máx. (R$)</label>
                        <input id="precoMax" name="precoMax" type="number" min="0" step="1" inputmode="decimal" value="${esc(filtros.precoMax)}">
                    </div>
                </div>
                <div class="campo">
                    <label for="notaMin">Nota mínima</label>
                    <select id="notaMin" name="notaMin">
                        ${[['', 'Qualquer nota'], ['3', '3 ou mais'], ['4', '4 ou mais'], ['4.5', '4,5 ou mais']]
                            .map(([v, t]) => `<option value="${v}" ${filtros.notaMin === v ? 'selected' : ''}>${t}</option>`).join('')}
                    </select>
                </div>
                <div class="campo">
                    <label for="distanciaMaxKm">Distância máxima</label>
                    <select id="distanciaMaxKm" name="distanciaMaxKm">
                        ${[['', 'Qualquer distância'], ['2', 'Até 2 km'], ['5', 'Até 5 km'], ['10', 'Até 10 km'], ['20', 'Até 20 km']]
                            .map(([v, t]) => `<option value="${v}" ${filtros.distanciaMaxKm === v ? 'selected' : ''}>${t}</option>`).join('')}
                    </select>
                </div>
                <div class="campo">
                    <label for="ordenarPor">Ordenar por</label>
                    <select id="ordenarPor" name="ordenarPor">
                        ${[['DISTANCIA', 'Menor distância'], ['PRECO', 'Menor preço'], ['NOTA', 'Maior nota']]
                            .map(([v, t]) => `<option value="${v}" ${filtros.ordenarPor === v ? 'selected' : ''}>${t}</option>`).join('')}
                    </select>
                </div>
                <div class="campo">
                    <label for="horas">Horas estimadas</label>
                    <input id="horas" name="horas" type="number" min="0.5" max="200" step="0.5" value="${esc(filtros.horas)}">
                    <span class="dica">Usado no valor estimado de serviços cobrados por hora.</span>
                </div>
                <button class="botao botao-bloco" type="submit">Buscar</button>
            </form>
            <section aria-labelledby="titulo-resultados">
                <h2 id="titulo-resultados" class="sr">Resultados</h2>
                <div id="resultados">${carregando('Escolha uma categoria para começar.')}</div>
            </section>
        </div>`;

    const form = app.querySelector('#form-busca');
    const resultados = app.querySelector('#resultados');

    try {
        const categorias = await api('/servicos/categorias');
        form.categoria.innerHTML = `<option value="">Selecione…</option>${categorias
            .map((c) => `<option ${c === filtros.categoria ? 'selected' : ''}>${esc(c)}</option>`).join('')}`;
    } catch (erro) {
        form.categoria.innerHTML = '<option value="">Erro ao carregar</option>';
        toast(erro.message, 'erro');
    }

    const desenhar = () => {
        if (!estado.busca) return;
        resultados.innerHTML = renderResultados(estado.busca.resultados);
    };

    const buscar = async () => {
        const dados = Object.fromEntries(new FormData(form).entries());
        if (!dados.categoria) {
            resultados.innerHTML = '<div class="folha vazio">Escolha uma categoria para buscar.</div>';
            return;
        }
        resultados.innerHTML = carregando('Buscando prestadores…');
        try {
            const lista = await api('/servicos/busca', { params: dados });
            estado.busca = { filtros: dados, resultados: lista };
            desenhar();
        } catch (erro) {
            resultados.innerHTML = `<div class="folha vazio">${esc(erro.message)}</div>`;
        }
    };

    form.addEventListener('submit', (e) => { e.preventDefault(); buscar(); });
    form.addEventListener('change', () => buscar());

    resultados.addEventListener('click', (e) => {
        const botao = e.target.closest('[data-comparar]');
        if (!botao) return;
        const item = estado.busca.resultados.find((r) => r.servicoId === Number(botao.dataset.comparar));
        const aviso = alternarComparacao(item);
        if (aviso) toast(aviso, 'erro');
    });

    const aoMudarComparacao = () => desenhar();
    eventos.addEventListener('comparacao', aoMudarComparacao);

    if (estado.busca && filtros.categoria) desenhar();
    else if (filtros.categoria) buscar();

    return () => eventos.removeEventListener('comparacao', aoMudarComparacao);
}

function renderResultados(lista) {
    if (!lista.length) {
        return `<div class="folha vazio">
            <p><strong>Nenhum prestador encontrado.</strong></p>
            <p>Tente afrouxar os filtros. Só aparecem prestadores cujo raio de atendimento alcança o seu endereço.</p>
        </div>`;
    }
    const cheio = estado.comparacao.length >= MAX_COMPARACAO;
    return `<p class="carregando">${lista.length} resultado(s) · ${estado.comparacao.length}/${MAX_COMPARACAO} na comparação</p>
        <div class="lista">${lista.map((r) => {
            const selecionado = estaNaComparacao(r.servicoId);
            return `
            <article class="ficha ${selecionado ? 'selecionada' : ''}">
                <div>
                    <h3>${esc(r.descricao)}</h3>
                    <p class="sub"><a href="#/prestadores/${r.prestadorId}">${esc(r.prestadorNome)}</a>
                        · <span class="etiqueta">${esc(COBRANCA[r.tipoCobranca])}</span></p>
                </div>
                <div class="preco">
                    <strong>${moeda(r.valorEstimado)}</strong>
                    <small>estimado${r.tipoCobranca === 'POR_HORA' ? ` p/ ${numero(r.horasConsideradas, 1)} h` : ''} · ${esc(precoComUnidade(r))}</small>
                </div>
                <div class="metricas">
                    <span class="metrica"><span>Nota</span><b>${estrelas(r.notaMedia)} ${numero(r.notaMedia, 1)} <small>(${r.quantidadeAvaliacoes})</small></b></span>
                    <span class="metrica"><span>Concluídos</span><b>${r.servicosConcluidos}</b></span>
                    <span class="metrica"><span>Distância</span><b>${km(r.distanciaKm)}</b></span>
                    <span class="metrica"><span>Atende até</span><b>${r.raioAtendimentoKm} km</b></span>
                </div>
                <div class="acoes">
                    <button type="button" class="botao botao-pequeno ${selecionado ? 'botao-secundario' : ''}"
                            data-comparar="${r.servicoId}" aria-pressed="${selecionado}"
                            ${!selecionado && cheio ? 'disabled title="Limite de 3 na comparação"' : ''}>
                        ${selecionado ? '✓ Na comparação' : '+ Comparar'}
                    </button>
                </div>
            </article>`;
        }).join('')}</div>`;
}
