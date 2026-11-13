// Utilitários de interface: formatação, escape de HTML, mensagens e listas fixas.

export const esc = (valor) => String(valor ?? '').replace(/[&<>"']/g, (c) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
})[c]);

export const moeda = (valor) => Number(valor ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
export const numero = (valor, casas = 1) => Number(valor ?? 0).toLocaleString('pt-BR', {
    minimumFractionDigits: casas, maximumFractionDigits: casas,
});
export const km = (valor) => `${numero(valor, 1)} km`;
export const data = (iso) => (iso ? new Date(`${iso}T00:00:00`).toLocaleDateString('pt-BR') : '—');
export const dataHora = (iso) => (iso ? new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' }) : '—');
export const hojeIso = (deslocamentoDias = 0) => {
    const d = new Date();
    d.setDate(d.getDate() + deslocamentoDias);
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

export const COBRANCA = { POR_HORA: 'Por hora', POR_EMPREITADA: 'Por empreitada' };

export const STATUS = {
    ABERTA: 'Aberta',
    ACEITA: 'Aceita',
    EM_EXECUCAO: 'Em execução',
    CONCLUIDA: 'Concluída',
    CANCELADA: 'Cancelada',
};

export const carimbo = (status, grande = false) =>
    `<span class="carimbo carimbo-${esc(status.toLowerCase())}${grande ? ' carimbo-grande' : ''}">${esc(STATUS[status] ?? status)}</span>`;

export function estrelas(nota) {
    const inteiras = Math.round(Number(nota ?? 0));
    return `<span class="estrelas" aria-label="Nota ${numero(nota, 1)} de 5">${'★'.repeat(inteiras)}${'☆'.repeat(5 - inteiras)}</span>`;
}

export const precoComUnidade = (item) =>
    item.tipoCobranca === 'POR_HORA' ? `${moeda(item.valorBase)}/h` : `${moeda(item.valorBase)} fechado`;

export const CATEGORIAS = [
    'Elétrica', 'Hidráulica', 'Montagem de móveis', 'Pintura', 'Pequenos reparos', 'Marcenaria', 'Alvenaria', 'Jardinagem',
];

// Coordenadas aproximadas de bairros de Maringá e região, usadas no cálculo de distância (sem API externa).
export const BAIRROS = [
    { nome: 'Zona 01 (Centro)', lat: -23.4253, lng: -51.9386 },
    { nome: 'Zona 02', lat: -23.4300, lng: -51.9280 },
    { nome: 'Zona 03', lat: -23.4150, lng: -51.9305 },
    { nome: 'Zona 05', lat: -23.4196, lng: -51.9497 },
    { nome: 'Zona 07', lat: -23.4105, lng: -51.9372 },
    { nome: 'Vila Operária', lat: -23.4165, lng: -51.9235 },
    { nome: 'Vila Morangueira', lat: -23.3960, lng: -51.9180 },
    { nome: 'Jardim Alvorada', lat: -23.3940, lng: -51.9530 },
    { nome: 'Mandacaru', lat: -23.3925, lng: -51.9290 },
    { nome: 'Borba Gato', lat: -23.4270, lng: -51.9150 },
    { nome: 'Jardim Novo Horizonte', lat: -23.4405, lng: -51.9440 },
    { nome: 'Parque das Grevíleas', lat: -23.4480, lng: -51.9600 },
    { nome: 'Sarandi (Centro)', lat: -23.4440, lng: -51.8740 },
];

/** Bairro conhecido mais próximo das coordenadas (para pré-selecionar o campo). */
export function bairroMaisProximo(lat, lng) {
    let melhor = null;
    let menor = Infinity;
    BAIRROS.forEach((b, i) => {
        const d = (b.lat - lat) ** 2 + (b.lng - lng) ** 2;
        if (d < menor) { menor = d; melhor = i; }
    });
    return menor < 0.00005 ? melhor : null; // ~700 m
}

/** Campo de localização: select de bairros + botão de GPS, gravando lat/lng em inputs ocultos. */
export function campoLocalizacao(lat, lng) {
    const indice = lat != null ? bairroMaisProximo(lat, lng) : null;
    const usandoGps = lat != null && indice === null;
    return `
        <div class="campo">
            <label for="bairro">Bairro (para calcular distâncias)</label>
            <div class="com-botao">
                <select id="bairro" name="bairro" required>
                    <option value="">Selecione…</option>
                    ${usandoGps ? '<option value="gps" selected>Minha localização (GPS)</option>' : ''}
                    ${BAIRROS.map((b, i) => `<option value="${i}" ${i === indice ? 'selected' : ''}>${esc(b.nome)}</option>`).join('')}
                </select>
                <button type="button" class="botao botao-secundario" data-acao="gps" title="Usar a localização do aparelho">GPS</button>
            </div>
            <span class="dica" data-dica-local>${usandoGps ? `Coordenadas: ${numero(lat, 4)}, ${numero(lng, 4)}` : 'Escolha o bairro ou use o GPS do aparelho.'}</span>
            <input type="hidden" name="latitude" value="${lat ?? ''}">
            <input type="hidden" name="longitude" value="${lng ?? ''}">
        </div>`;
}

export function ligarCampoLocalizacao(form) {
    const select = form.querySelector('#bairro');
    const dica = form.querySelector('[data-dica-local]');
    const definir = (lat, lng) => {
        form.latitude.value = lat;
        form.longitude.value = lng;
    };
    select.addEventListener('change', () => {
        const b = BAIRROS[select.value];
        if (b) {
            definir(b.lat, b.lng);
            dica.textContent = 'Escolha o bairro ou use o GPS do aparelho.';
        }
    });
    form.querySelector('[data-acao="gps"]').addEventListener('click', () => {
        if (!navigator.geolocation) {
            toast('Seu navegador não oferece geolocalização', 'erro');
            return;
        }
        dica.textContent = 'Obtendo localização…';
        navigator.geolocation.getCurrentPosition((pos) => {
            const { latitude, longitude } = pos.coords;
            definir(latitude.toFixed(6), longitude.toFixed(6));
            let opcao = select.querySelector('option[value="gps"]');
            if (!opcao) {
                opcao = new Option('Minha localização (GPS)', 'gps');
                select.add(opcao, 1);
            }
            select.value = 'gps';
            dica.textContent = `Coordenadas: ${numero(latitude, 4)}, ${numero(longitude, 4)}`;
        }, () => {
            dica.textContent = 'Não foi possível obter a localização. Escolha o bairro.';
        }, { timeout: 10000 });
    });
}

/** Máscara (44) 99999-9999 enquanto digita. */
export function mascaraTelefone(input) {
    input.addEventListener('input', () => {
        const d = input.value.replace(/\D/g, '').slice(0, 11);
        let v = d;
        if (d.length > 2) v = `(${d.slice(0, 2)}) ${d.slice(2)}`;
        if (d.length > 6) v = `(${d.slice(0, 2)}) ${d.slice(2, d.length - 4)}-${d.slice(-4)}`;
        input.value = v;
    });
}

export function dadosForm(form) {
    return Object.fromEntries(new FormData(form).entries());
}

/** Mostra as mensagens do Bean Validation embaixo de cada campo. */
export function mostrarErros(form, erro) {
    limparErros(form);
    const geral = form.querySelector('.erro-geral');
    const campos = erro?.campos ?? {};
    Object.entries(campos).forEach(([nome, mensagem]) => {
        const alvo = form.querySelector(`[name="${nome}"]`);
        const campo = alvo?.closest('.campo, fieldset');
        if (campo) {
            campo.classList.add('invalido');
            campo.insertAdjacentHTML('beforeend', `<span class="erro-campo">${esc(mensagem)}</span>`);
        }
    });
    if (geral) geral.textContent = erro?.message ?? 'Erro inesperado';
    form.querySelector('.invalido input, .invalido select, .invalido textarea')?.focus();
}

export function limparErros(form) {
    form.querySelectorAll('.erro-campo').forEach((e) => e.remove());
    form.querySelectorAll('.invalido').forEach((e) => e.classList.remove('invalido'));
    const geral = form.querySelector('.erro-geral');
    if (geral) geral.textContent = '';
}

/** Desabilita o botão de envio enquanto a requisição está em andamento. */
export async function enviando(form, acao) {
    const botao = form.querySelector('[type="submit"]');
    const texto = botao?.textContent;
    if (botao) { botao.disabled = true; botao.textContent = 'Enviando…'; }
    try {
        return await acao();
    } finally {
        if (botao) { botao.disabled = false; botao.textContent = texto; }
    }
}

let temporizador;
export function toast(mensagem, tipo = 'ok') {
    const el = document.getElementById('toast');
    el.textContent = mensagem;
    el.classList.toggle('erro', tipo === 'erro');
    el.classList.add('visivel');
    clearTimeout(temporizador);
    temporizador = setTimeout(() => el.classList.remove('visivel'), 3500);
}

export const carregando = (texto = 'Carregando…') => `<p class="carregando">${esc(texto)}</p>`;

export function telaErro(app, erro) {
    app.innerHTML = `<div class="folha folha-media vazio"><p>${esc(erro.message)}</p><a class="botao botao-secundario" href="#/">Voltar ao início</a></div>`;
}
