// Estado da SPA. A comparação (RF04) fica só em memória: some ao recarregar a página.

const CHAVE_SESSAO = 'mda.sessao';
export const MAX_COMPARACAO = 3;

function lerSessao() {
    try {
        return JSON.parse(localStorage.getItem(CHAVE_SESSAO));
    } catch {
        return null;
    }
}

function salvarSessao() {
    try {
        localStorage.setItem(CHAVE_SESSAO, JSON.stringify({ token: estado.token, usuario: estado.usuario }));
    } catch {
        // navegação privada: a sessão vale só enquanto a aba estiver aberta
    }
}

const sessao = lerSessao();

export const estado = {
    token: sessao?.token ?? null,
    usuario: sessao?.usuario ?? null,
    comparacao: [],      // itens do resultado de busca escolhidos para comparar
    busca: null,         // { filtros, resultados } da última busca, para voltar sem refazer
};

/** Avisa a interface quando a comparação muda (barra inferior, contador do menu). */
export const eventos = new EventTarget();

export function entrar({ token, usuario }) {
    estado.token = token;
    estado.usuario = usuario;
    salvarSessao();
}

export function atualizarUsuario(usuario) {
    estado.usuario = usuario;
    salvarSessao();
}

export function sair() {
    estado.token = null;
    estado.usuario = null;
    estado.comparacao = [];
    estado.busca = null;
    try {
        localStorage.removeItem(CHAVE_SESSAO);
    } catch {
        // nada a limpar
    }
    eventos.dispatchEvent(new Event('comparacao'));
}

export const ehCliente = () => estado.usuario?.perfil === 'CLIENTE';
export const ehPrestador = () => estado.usuario?.perfil === 'PRESTADOR';

export const estaNaComparacao = (servicoId) => estado.comparacao.some((item) => item.servicoId === servicoId);

/** Adiciona ou remove um resultado da comparação; devolve uma mensagem quando o limite foi atingido. */
export function alternarComparacao(item) {
    if (estaNaComparacao(item.servicoId)) {
        estado.comparacao = estado.comparacao.filter((i) => i.servicoId !== item.servicoId);
    } else if (estado.comparacao.length >= MAX_COMPARACAO) {
        return `Você pode comparar no máximo ${MAX_COMPARACAO} prestadores. Remova um para trocar.`;
    } else {
        estado.comparacao = [...estado.comparacao, item];
    }
    eventos.dispatchEvent(new Event('comparacao'));
    return null;
}

export function removerDaComparacao(servicoId) {
    estado.comparacao = estado.comparacao.filter((i) => i.servicoId !== servicoId);
    eventos.dispatchEvent(new Event('comparacao'));
}
