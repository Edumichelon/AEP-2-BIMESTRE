// Cliente HTTP da API REST (fetch + token JWT).
import { estado, sair } from './estado.js';

// Servida pelo próprio Spring (qualquer porta) usa a mesma origem. Aberta por um servidor estático
// de desenvolvimento (ex.: Live Server do VS Code, porta 5500) aponta para o back-end local na 8080.
const PORTAS_SERVIDOR_ESTATICO = ['5500', '5501', '5173', '3000'];
const BASE = window.API_BASE
    ?? (PORTAS_SERVIDOR_ESTATICO.includes(location.port) ? 'http://localhost:8080' : '');

export class ErroApi extends Error {
    constructor(status, corpo) {
        super(corpo?.mensagem || 'Falha na comunicação com o servidor');
        this.status = status;
        this.campos = corpo?.campos ?? {};
    }
}

export async function api(caminho, { metodo = 'GET', corpo, params } = {}) {
    const url = new URL(`${BASE}/api${caminho}`, location.href);
    Object.entries(params ?? {}).forEach(([chave, valor]) => {
        if (valor !== '' && valor !== null && valor !== undefined) url.searchParams.set(chave, valor);
    });

    const headers = { Accept: 'application/json' };
    if (corpo !== undefined) headers['Content-Type'] = 'application/json';
    if (estado.token) headers.Authorization = `Bearer ${estado.token}`;

    let resposta;
    try {
        resposta = await fetch(url, {
            method: metodo,
            headers,
            body: corpo === undefined ? undefined : JSON.stringify(corpo),
        });
    } catch {
        throw new ErroApi(0, { mensagem: 'Não foi possível falar com o servidor. Verifique se o back-end está rodando.' });
    }

    if (resposta.status === 204) return null;
    const dados = await resposta.json().catch(() => null);
    if (!resposta.ok) tratarErro(resposta.status, dados);
    return dados;
}

/**
 * Baixa um arquivo protegido (ex.: exportação do relatório, RF07). O token vai no cabeçalho
 * Authorization, nunca na URL, e o arquivo é salvo pelo navegador com o nome sugerido pela API.
 */
export async function baixar(caminho, { params } = {}) {
    const url = new URL(`${BASE}/api${caminho}`, location.href);
    Object.entries(params ?? {}).forEach(([chave, valor]) => {
        if (valor !== '' && valor !== null && valor !== undefined) url.searchParams.set(chave, valor);
    });
    let resposta;
    try {
        resposta = await fetch(url, { headers: { Authorization: `Bearer ${estado.token}` } });
    } catch {
        throw new ErroApi(0, { mensagem: 'Não foi possível falar com o servidor. Verifique se o back-end está rodando.' });
    }
    if (!resposta.ok) tratarErro(resposta.status, await resposta.json().catch(() => null));

    const disposicao = resposta.headers.get('Content-Disposition') ?? '';
    const nome = /filename="?([^";]+)"?/.exec(disposicao)?.[1] ?? 'arquivo';
    const link = document.createElement('a');
    link.href = URL.createObjectURL(await resposta.blob());
    link.download = nome;
    document.body.append(link);
    link.click();
    link.remove();
    setTimeout(() => URL.revokeObjectURL(link.href), 1000);
    return nome;
}

function tratarErro(status, dados) {
    if (status === 401 && estado.token) {
        // token expirado ou inválido: volta para o login
        sair();
        location.hash = '#/entrar';
    }
    throw new ErroApi(status, dados);
}
