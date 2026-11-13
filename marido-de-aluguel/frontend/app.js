// Marido de Aluguel - SPA com roteamento por hash: busca, comparação e contratação sem recarregar a página.
import { ehCliente, estado, eventos, MAX_COMPARACAO, sair } from './js/estado.js';
import { esc } from './js/ui.js';
import { telaCadastro, telaEntrar, telaInicialDoPerfil } from './js/telas/acesso.js';
import { telaBuscar } from './js/telas/busca.js';
import { telaComparar } from './js/telas/comparacao.js';
import { telaOrdem, telaOrdens } from './js/telas/ordens.js';
import { telaFormServico, telaMeusServicos } from './js/telas/servicos.js';
import { telaRelatorio } from './js/telas/relatorio.js';
import { telaPerfil, telaPrestador } from './js/telas/perfil.js';

const TODOS = ['CLIENTE', 'PRESTADOR'];

// padrão de rota -> tela e perfis permitidos (null = pública)
const ROTAS = [
    { padrao: '/entrar', tela: telaEntrar, perfis: null, titulo: 'Entrar' },
    { padrao: '/cadastro', tela: telaCadastro, perfis: null, titulo: 'Cadastro' },
    { padrao: '/buscar', tela: telaBuscar, perfis: ['CLIENTE'], titulo: 'Buscar' },
    { padrao: '/comparar', tela: telaComparar, perfis: ['CLIENTE'], titulo: 'Comparar' },
    { padrao: '/ordens', tela: telaOrdens, perfis: TODOS, titulo: 'Ordens' },
    { padrao: '/ordens/:id', tela: telaOrdem, perfis: TODOS, titulo: 'Ordem de serviço' },
    { padrao: '/servicos', tela: telaMeusServicos, perfis: ['PRESTADOR'], titulo: 'Meus serviços' },
    { padrao: '/servicos/:id', tela: telaFormServico, perfis: ['PRESTADOR'], titulo: 'Serviço' },
    { padrao: '/relatorio', tela: telaRelatorio, perfis: ['PRESTADOR'], titulo: 'Relatório' },
    { padrao: '/perfil', tela: telaPerfil, perfis: TODOS, titulo: 'Meu perfil' },
    { padrao: '/prestadores/:id', tela: telaPrestador, perfis: TODOS, titulo: 'Prestador' },
];

const app = document.getElementById('app');
const menu = document.getElementById('menu');
const menuBotao = document.getElementById('menu-botao');
const barra = document.getElementById('barra-comparacao');
let limparTelaAnterior = null;

function encontrarRota(caminho) {
    const partes = caminho.split('/').filter(Boolean);
    for (const rota of ROTAS) {
        const partesPadrao = rota.padrao.split('/').filter(Boolean);
        if (partesPadrao.length !== partes.length) continue;
        const params = {};
        const casou = partesPadrao.every((p, i) => {
            if (p.startsWith(':')) {
                params[p.slice(1)] = decodeURIComponent(partes[i]);
                return true;
            }
            return p === partes[i];
        });
        if (casou) return { rota, params };
    }
    return null;
}

async function navegar() {
    const caminho = location.hash.replace(/^#/, '') || '/';
    const perfil = estado.usuario?.perfil;
    const encontrada = encontrarRota(caminho);

    // guarda de rotas: sem sessão vai para o login; perfil errado vai para a tela inicial do perfil
    if (!encontrada) {
        location.replace(perfil ? telaInicialDoPerfil(perfil) : '#/entrar');
        return;
    }
    const { rota, params } = encontrada;
    if (rota.perfis && !perfil) {
        location.replace('#/entrar');
        return;
    }
    if (rota.perfis && !rota.perfis.includes(perfil)) {
        location.replace(telaInicialDoPerfil(perfil));
        return;
    }
    if (!rota.perfis && perfil) {
        location.replace(telaInicialDoPerfil(perfil));
        return;
    }

    if (typeof limparTelaAnterior === 'function') limparTelaAnterior();
    limparTelaAnterior = null;
    document.title = `${rota.titulo} · Marido de Aluguel`;
    desenharMenu(caminho);
    desenharBarraComparacao(caminho);
    menu.classList.remove('aberto');
    menuBotao.setAttribute('aria-expanded', 'false');
    window.scrollTo(0, 0);

    limparTelaAnterior = await rota.tela(app, params);
    app.focus({ preventScroll: true });
}

function desenharMenu(caminho) {
    const usuario = estado.usuario;
    menuBotao.hidden = !usuario;
    if (!usuario) {
        menu.innerHTML = `<a href="#/entrar">Entrar</a><a href="#/cadastro">Criar conta</a>`;
        marcarAtual(caminho);
        return;
    }
    const links = usuario.perfil === 'CLIENTE'
        ? [['#/buscar', 'Buscar'], ['#/comparar', `Comparar<span class="contador">${estado.comparacao.length}</span>`], ['#/ordens', 'Minhas ordens']]
        : [['#/servicos', 'Meus serviços'], ['#/ordens', 'Ordens recebidas'], ['#/relatorio', 'Relatório']];
    menu.innerHTML = `
        ${links.map(([href, texto]) => `<a href="${href}">${texto}</a>`).join('')}
        <a href="#/perfil">Perfil</a>
        <span class="quem">${esc(usuario.nome.split(' ')[0])} · ${usuario.perfil === 'CLIENTE' ? 'cliente' : 'prestador'}</span>
        <button type="button" data-sair>Sair</button>`;
    marcarAtual(caminho);
}

function marcarAtual(caminho) {
    const base = `#/${caminho.split('/').filter(Boolean)[0] ?? ''}`;
    menu.querySelectorAll('a').forEach((a) => {
        if (a.getAttribute('href') === base) a.setAttribute('aria-current', 'page');
        else a.removeAttribute('aria-current');
    });
}

function desenharBarraComparacao(caminho = location.hash.replace(/^#/, '')) {
    const itens = estado.comparacao;
    const visivel = ehCliente() && itens.length > 0 && caminho !== '/comparar';
    barra.hidden = !visivel;
    if (!visivel) return;
    barra.innerHTML = `
        <span><b>${itens.length}/${MAX_COMPARACAO}</b> na comparação: ${itens.map((i) => esc(i.prestadorNome.split(' ')[0])).join(', ')}</span>
        <a class="botao botao-pequeno" href="#/comparar">Comparar lado a lado →</a>`;
}

menu.addEventListener('click', (e) => {
    if (e.target.closest('[data-sair]')) {
        sair();
        location.hash = '#/entrar';
    }
});

menuBotao.addEventListener('click', () => {
    const aberto = menu.classList.toggle('aberto');
    menuBotao.setAttribute('aria-expanded', String(aberto));
});

eventos.addEventListener('comparacao', () => {
    const caminho = location.hash.replace(/^#/, '');
    desenharBarraComparacao(caminho);
    const contador = menu.querySelector('.contador');
    if (contador) contador.textContent = estado.comparacao.length;
});

window.addEventListener('hashchange', navegar);
navegar();
