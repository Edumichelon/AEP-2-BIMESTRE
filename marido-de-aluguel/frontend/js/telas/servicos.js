// RF02: CRUD completo das ofertas de serviço do prestador (tela -> API -> PostgreSQL).
import { api } from '../api.js';
import {
    CATEGORIAS, COBRANCA, carregando, dadosForm, enviando, esc, moeda, mostrarErros, telaErro, toast,
} from '../ui.js';

// Read: lista + Delete
export async function telaMeusServicos(app) {
    app.innerHTML = `
        <div class="cabecalho-tela">
            <div>
                <div class="rotulo-tela">Vitrine</div>
                <h1>Meus serviços</h1>
                <p>É o que os clientes encontram na busca.</p>
            </div>
            <a class="botao" href="#/servicos/novo">+ Novo serviço</a>
        </div>
        <div id="lista-servicos">${carregando()}</div>`;

    const alvo = app.querySelector('#lista-servicos');
    const carregar = async () => {
        try {
            desenhar(await api('/servicos/meus'));
        } catch (erro) {
            alvo.innerHTML = `<div class="folha vazio">${esc(erro.message)}</div>`;
        }
    };

    const desenhar = (servicos) => {
        if (!servicos.length) {
            alvo.innerHTML = `<div class="folha vazio"><p>Você ainda não cadastrou serviços.</p>
                <a class="botao" href="#/servicos/novo">Cadastrar o primeiro</a></div>`;
            return;
        }
        alvo.innerHTML = `<div class="lista">${servicos.map((s) => `
            <article class="ficha">
                <div>
                    <h3>${esc(s.descricao)}</h3>
                    <p class="sub"><span class="etiqueta">${esc(s.categoria)}</span> · atende até ${s.raioAtendimentoKm} km</p>
                </div>
                <div class="preco">
                    <strong>${moeda(s.valorBase)}</strong>
                    <small>${esc(COBRANCA[s.tipoCobranca])}${s.tipoCobranca === 'POR_HORA' ? ' · valor da hora' : ''}</small>
                </div>
                <div class="acoes">
                    <a class="botao botao-secundario botao-pequeno" href="#/servicos/${s.id}">Editar</a>
                    <button type="button" class="botao botao-perigo botao-pequeno" data-excluir="${s.id}"
                            data-nome="${esc(s.descricao)}">Excluir</button>
                </div>
            </article>`).join('')}</div>`;
    };

    alvo.addEventListener('click', async (e) => {
        const botao = e.target.closest('[data-excluir]');
        if (!botao) return;
        if (!confirm(`Excluir o serviço "${botao.dataset.nome}"?`)) return;
        botao.disabled = true;
        try {
            await api(`/servicos/${botao.dataset.excluir}`, { metodo: 'DELETE' });
            toast('Serviço excluído');
            await carregar();
        } catch (erro) {
            toast(erro.message, 'erro');
            botao.disabled = false;
        }
    });

    await carregar();
}

// Create e Update
export async function telaFormServico(app, { id }) {
    const edicao = id !== 'novo';
    let servico = {
        categoria: '', descricao: '', raioAtendimentoKm: 10, tipoCobranca: 'POR_HORA', valorBase: '',
    };
    if (edicao) {
        app.innerHTML = carregando();
        try {
            servico = await api(`/servicos/${id}`);
        } catch (erro) {
            telaErro(app, erro);
            return;
        }
    }
    const categorias = CATEGORIAS.includes(servico.categoria) || !servico.categoria
        ? CATEGORIAS : [servico.categoria, ...CATEGORIAS];

    app.innerHTML = `
        <p><a href="#/servicos">← Meus serviços</a></p>
        <section class="folha folha-media">
            <div class="folha-titulo"><strong>${edicao ? 'Editar serviço' : 'Novo serviço'}</strong><span>${edicao ? `#${servico.id}` : 'cadastro'}</span></div>
            <form id="form-servico" novalidate>
                <div class="linha-campos">
                    <div class="campo">
                        <label for="categoria">Categoria</label>
                        <select id="categoria" name="categoria" required>
                            <option value="">Selecione…</option>
                            ${categorias.map((c) => `<option ${c === servico.categoria ? 'selected' : ''}>${esc(c)}</option>`).join('')}
                        </select>
                    </div>
                    <div class="campo">
                        <label for="raioAtendimentoKm">Raio de atendimento (km)</label>
                        <input id="raioAtendimentoKm" name="raioAtendimentoKm" type="number" min="1" max="200" step="1"
                               required value="${esc(servico.raioAtendimentoKm)}">
                    </div>
                </div>
                <div class="campo">
                    <label for="descricao">Descrição do serviço</label>
                    <textarea id="descricao" name="descricao" maxlength="2000" required
                              placeholder="Ex.: Troca de resistência de chuveiro, com teste de aterramento">${esc(servico.descricao)}</textarea>
                </div>
                <fieldset>
                    <legend class="campo"><span class="legenda">Modelo de cobrança</span></legend>
                    <div class="opcoes">
                        <label class="opcao"><input type="radio" name="tipoCobranca" value="POR_HORA" ${servico.tipoCobranca === 'POR_HORA' ? 'checked' : ''}>
                            <span><strong>Por hora</strong><small>Orçamento = valor da hora × horas estimadas</small></span></label>
                        <label class="opcao"><input type="radio" name="tipoCobranca" value="POR_EMPREITADA" ${servico.tipoCobranca === 'POR_EMPREITADA' ? 'checked' : ''}>
                            <span><strong>Por empreitada</strong><small>Valor fechado pelo serviço</small></span></label>
                    </div>
                </fieldset>
                <div class="campo">
                    <label for="valorBase" id="rotulo-valor"></label>
                    <input id="valorBase" name="valorBase" type="number" min="0.01" step="0.01" inputmode="decimal"
                           required value="${esc(servico.valorBase)}">
                </div>
                <p class="erro-geral" role="alert"></p>
                <div class="acoes">
                    <button class="botao" type="submit">${edicao ? 'Salvar alterações' : 'Cadastrar serviço'}</button>
                    <a class="botao botao-secundario" href="#/servicos">Cancelar</a>
                </div>
            </form>
        </section>`;

    const form = app.querySelector('#form-servico');
    const rotuloValor = app.querySelector('#rotulo-valor');
    const atualizarRotulo = () => {
        rotuloValor.textContent = form.tipoCobranca.value === 'POR_HORA' ? 'Valor da hora (R$)' : 'Valor fechado (R$)';
    };
    form.addEventListener('change', atualizarRotulo);
    atualizarRotulo();

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const dados = dadosForm(form);
        const corpo = {
            ...dados,
            raioAtendimentoKm: dados.raioAtendimentoKm ? Number(dados.raioAtendimentoKm) : null,
            valorBase: dados.valorBase ? Number(dados.valorBase) : null,
        };
        try {
            await enviando(form, () => api(edicao ? `/servicos/${id}` : '/servicos', {
                metodo: edicao ? 'PUT' : 'POST', corpo,
            }));
            toast(edicao ? 'Serviço atualizado' : 'Serviço cadastrado');
            location.hash = '#/servicos';
        } catch (erro) {
            mostrarErros(form, erro);
        }
    });
    form.categoria.focus();
}
