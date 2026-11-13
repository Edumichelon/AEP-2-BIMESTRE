// RF05 (acompanhamento da ordem e mudanças de status) e RF06 (avaliação após a conclusão).
import { api } from '../api.js';
import { ehCliente } from '../estado.js';
import {
    carimbo, carregando, COBRANCA, data, dataHora, enviando, esc, estrelas, moeda, mostrarErros, numero,
    telaErro, toast,
} from '../ui.js';

const FILTROS = {
    todas: { rotulo: 'Todas', aceita: () => true },
    andamento: { rotulo: 'Em andamento', aceita: (o) => ['ABERTA', 'ACEITA', 'EM_EXECUCAO'].includes(o.status) },
    concluidas: { rotulo: 'Concluídas', aceita: (o) => o.status === 'CONCLUIDA' },
    canceladas: { rotulo: 'Canceladas', aceita: (o) => o.status === 'CANCELADA' },
};

const ACOES_STATUS = {
    ACEITA: { rotulo: 'Aceitar ordem', classe: '' },
    EM_EXECUCAO: { rotulo: 'Iniciar execução', classe: '' },
    CONCLUIDA: { rotulo: 'Marcar como concluída', classe: '' },
    CANCELADA: { rotulo: 'Cancelar ordem', classe: 'botao-perigo', confirmar: 'Cancelar esta ordem de serviço? Essa ação não pode ser desfeita.' },
};

export async function telaOrdens(app) {
    const cliente = ehCliente();
    app.innerHTML = `
        <div class="cabecalho-tela">
            <div>
                <div class="rotulo-tela">${cliente ? 'Etapa 3 · contratação' : 'Agenda'}</div>
                <h1>${cliente ? 'Minhas ordens de serviço' : 'Ordens recebidas'}</h1>
            </div>
            ${cliente ? '<a class="botao" href="#/buscar">Nova busca</a>' : ''}
        </div>
        <div class="abas" role="group" aria-label="Filtrar ordens">
            ${Object.entries(FILTROS).map(([chave, f], i) =>
                `<button type="button" data-filtro="${chave}" aria-pressed="${i === 0}">${f.rotulo}</button>`).join('')}
        </div>
        <div id="lista-ordens">${carregando()}</div>`;

    const alvo = app.querySelector('#lista-ordens');
    let ordens;
    try {
        ordens = await api('/ordens');
    } catch (erro) {
        alvo.innerHTML = `<div class="folha vazio">${esc(erro.message)}</div>`;
        return undefined;
    }

    const desenhar = (filtro) => {
        const lista = ordens.filter(FILTROS[filtro].aceita);
        if (!lista.length) {
            alvo.innerHTML = `<div class="folha vazio"><p>Nenhuma ordem por aqui.</p>
                ${cliente ? '<a class="botao" href="#/buscar">Buscar prestadores</a>' : ''}</div>`;
            return;
        }
        alvo.innerHTML = `<div class="lista">${lista.map((o) => `
            <a class="ficha" href="#/ordens/${o.id}" style="text-decoration:none;color:inherit">
                <div>
                    <span class="os-numero" style="font-size:1rem">${esc(o.codigo)}</span>
                    <h3>${esc(o.servico.descricao)}</h3>
                    <p class="sub">${cliente ? 'Prestador' : 'Cliente'}: ${esc(cliente ? o.prestador.nome : o.cliente.nome)}
                        · aberta em ${data(o.dataAbertura)}</p>
                </div>
                <div class="preco">
                    ${carimbo(o.status)}
                    <strong style="margin-top:.4rem">${moeda(o.valorFinal)}</strong>
                    ${o.podeAvaliar ? '<small>aguardando sua avaliação</small>' : ''}
                    ${!cliente && o.transicoesPermitidas.includes('ACEITA') ? '<small>aguardando seu aceite</small>' : ''}
                </div>
            </a>`).join('')}</div>`;
    };

    app.querySelector('.abas').addEventListener('click', (e) => {
        const botao = e.target.closest('[data-filtro]');
        if (!botao) return;
        app.querySelectorAll('[data-filtro]').forEach((b) => b.setAttribute('aria-pressed', String(b === botao)));
        desenhar(botao.dataset.filtro);
    });
    desenhar('todas');
    return undefined;
}

export async function telaOrdem(app, { id }) {
    app.innerHTML = carregando();
    let ordem;
    try {
        ordem = await api(`/ordens/${id}`);
    } catch (erro) {
        telaErro(app, erro);
        return;
    }

    const desenhar = () => {
        const cliente = ehCliente();
        app.innerHTML = `
            <p><a href="#/ordens">← ${cliente ? 'Minhas ordens' : 'Ordens recebidas'}</a></p>
            <article class="folha folha-media">
                <div class="folha-titulo"><strong>Ordem de serviço</strong><span>aberta em ${data(ordem.dataAbertura)}</span></div>
                <div class="os-cabecalho">
                    <div>
                        <div class="rotulo-tela">Nº</div>
                        <div class="os-numero">${esc(ordem.codigo)}</div>
                    </div>
                    ${carimbo(ordem.status, true)}
                </div>
                <dl class="os-campos">
                    ${campo('Serviço', ordem.servico.descricao)}
                    ${campo('Categoria', ordem.servico.categoria)}
                    ${campo('Cobrança', COBRANCA[ordem.servico.tipoCobranca])}
                    ${campo('Horas estimadas', ordem.horasEstimadas ? `${numero(ordem.horasEstimadas, 1)} h` : '—', true)}
                    ${campo('Valor', moeda(ordem.valorFinal), true)}
                    ${campo('Cliente', ordem.cliente.nome)}
                    ${campo('Telefone do cliente', ordem.cliente.telefone, true)}
                    ${campo('Endereço do serviço', ordem.cliente.endereco || '—')}
                    ${campo('Prestador', ordem.prestador.nome)}
                    ${campo('Telefone do prestador', ordem.prestador.telefone, true)}
                </dl>
                ${ordem.transicoesPermitidas.length ? `
                    <div class="acoes" id="acoes-status">
                        ${ordem.transicoesPermitidas.map((s) => `
                            <button type="button" class="botao ${ACOES_STATUS[s].classe}" data-status="${s}">${ACOES_STATUS[s].rotulo}</button>`).join('')}
                    </div>` : ''}
            </article>

            <section class="folha folha-media">
                <div class="folha-titulo"><strong>Histórico de status</strong><span>${ordem.historico.length} registro(s)</span></div>
                <ol class="linha-tempo">
                    ${ordem.historico.map((h) => `<li><strong>${esc(h.descricao)}</strong><time datetime="${esc(h.dataHora)}">${dataHora(h.dataHora)}</time></li>`).join('')}
                </ol>
            </section>

            <section class="folha folha-media" id="secao-avaliacao">
                <div class="folha-titulo"><strong>Avaliação</strong><span>1 por ordem · imutável</span></div>
                ${secaoAvaliacao(ordem, cliente)}
            </section>`;
        ligar();
    };

    const ligar = () => {
        app.querySelector('#acoes-status')?.addEventListener('click', async (e) => {
            const botao = e.target.closest('[data-status]');
            if (!botao) return;
            const acao = ACOES_STATUS[botao.dataset.status];
            if (acao.confirmar && !confirm(acao.confirmar)) return;
            botao.disabled = true;
            try {
                ordem = await api(`/ordens/${ordem.id}/status`, { metodo: 'PATCH', corpo: { status: botao.dataset.status } });
                toast(`Ordem ${ordem.codigo}: ${ordem.statusDescricao}`);
                desenhar();
            } catch (erro) {
                toast(erro.message, 'erro');
                botao.disabled = false;
            }
        });

        const form = app.querySelector('#form-avaliacao');
        form?.addEventListener('submit', async (e) => {
            e.preventDefault();
            const nota = form.querySelector('input[name="nota"]:checked')?.value;
            if (!nota) {
                mostrarErros(form, { message: 'Escolha uma nota de 1 a 5', campos: { nota: 'Escolha uma nota de 1 a 5' } });
                return;
            }
            if (!confirm('Enviar a avaliação? Depois de enviada ela não poderá ser alterada.')) return;
            try {
                const avaliacao = await enviando(form, () => api(`/ordens/${ordem.id}/avaliacao`, {
                    metodo: 'POST', corpo: { nota: Number(nota), comentario: form.comentario.value },
                }));
                ordem = { ...ordem, avaliacao, podeAvaliar: false };
                toast('Obrigado! Avaliação registrada.');
                desenhar();
            } catch (erro) {
                mostrarErros(form, erro);
            }
        });
    };

    desenhar();
}

function campo(rotulo, valor, mono = false) {
    return `<div class="os-campo"><dt>${esc(rotulo)}</dt><dd${mono ? ' class="mono"' : ''}>${esc(valor)}</dd></div>`;
}

function secaoAvaliacao(ordem, cliente) {
    if (ordem.avaliacao) {
        const a = ordem.avaliacao;
        return `<div class="avaliacao-registrada">
            <p>${estrelas(a.nota)} <strong>${a.nota}/5</strong> · ${data(a.dataAvaliacao)}</p>
            ${a.comentario ? `<p>“${esc(a.comentario)}”</p>` : ''}
            <small>Avaliação registrada. Ela compõe a nota média do prestador e não pode ser editada.</small>
        </div>`;
    }
    if (ordem.podeAvaliar) {
        return `<form id="form-avaliacao" novalidate>
            <fieldset class="campo">
                <legend class="legenda">Sua nota para ${esc(ordem.prestador.nome)}</legend>
                <div class="estrelas-input">
                    ${[5, 4, 3, 2, 1].map((n) => `
                        <input type="radio" id="nota-${n}" name="nota" value="${n}">
                        <label for="nota-${n}" title="${n} estrela(s)">★<span class="sr">${n}</span></label>`).join('')}
                </div>
            </fieldset>
            <div class="campo">
                <label for="comentario">Comentário</label>
                <textarea id="comentario" name="comentario" maxlength="1000" placeholder="Como foi o serviço?"></textarea>
            </div>
            <p class="erro-geral" role="alert"></p>
            <button class="botao" type="submit">Enviar avaliação</button>
        </form>`;
    }
    if (ordem.status === 'CANCELADA') return '<p>Ordem cancelada: não há avaliação.</p>';
    return cliente
        ? '<p class="aviso">Você poderá avaliar o prestador quando a ordem for marcada como <strong>concluída</strong>.</p>'
        : '<p>O cliente poderá avaliar depois que a ordem for concluída.</p>';
}
