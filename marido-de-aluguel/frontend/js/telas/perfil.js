// Manutenção do próprio perfil e perfil público do prestador.
import { api } from '../api.js';
import { atualizarUsuario, ehPrestador, estado } from '../estado.js';
import {
    campoLocalizacao, carregando, COBRANCA, dadosForm, data, enviando, esc, estrelas, ligarCampoLocalizacao,
    mascaraTelefone, moeda, mostrarErros, numero, telaErro, toast,
} from '../ui.js';

export async function telaPerfil(app) {
    app.innerHTML = carregando();
    let usuario;
    try {
        usuario = await api('/usuarios/me');
    } catch (erro) {
        telaErro(app, erro);
        return;
    }
    app.innerHTML = `
        <section class="folha folha-media">
            <div class="folha-titulo"><strong>Meu perfil</strong><span>${usuario.perfil === 'PRESTADOR' ? 'prestador' : 'cliente'}</span></div>
            ${ehPrestador() ? `<p class="aviso">Nota média: <strong>${numero(usuario.notaMedia, 2)}</strong>
                (${usuario.quantidadeAvaliacoes} avaliações) · <a href="#/prestadores/${usuario.id}">ver meu perfil público</a></p>` : ''}
            <form id="form-perfil" novalidate>
                <div class="campo">
                    <label for="email">E-mail</label>
                    <input id="email" value="${esc(usuario.email)}" disabled>
                </div>
                <div class="campo">
                    <label for="nome">Nome completo</label>
                    <input id="nome" name="nome" maxlength="100" required value="${esc(usuario.nome)}">
                </div>
                <div class="campo">
                    <label for="telefone">Telefone</label>
                    <input id="telefone" name="telefone" type="tel" required value="${esc(usuario.telefone)}">
                </div>
                <div class="campo">
                    <label for="endereco">Endereço</label>
                    <input id="endereco" name="endereco" maxlength="150" value="${esc(usuario.endereco)}">
                </div>
                ${campoLocalizacao(usuario.latitude, usuario.longitude)}
                <p class="erro-geral" role="alert"></p>
                <button class="botao" type="submit">Salvar</button>
            </form>
        </section>`;

    const form = app.querySelector('#form-perfil');
    mascaraTelefone(form.telefone);
    ligarCampoLocalizacao(form);
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const dados = dadosForm(form);
        try {
            const atualizado = await enviando(form, () => api('/usuarios/me', {
                metodo: 'PUT',
                corpo: {
                    nome: dados.nome, telefone: dados.telefone, endereco: dados.endereco,
                    latitude: dados.latitude ? Number(dados.latitude) : null,
                    longitude: dados.longitude ? Number(dados.longitude) : null,
                },
            }));
            atualizarUsuario(atualizado);
            estado.busca = null; // distâncias mudam com o novo endereço
            toast('Perfil atualizado');
        } catch (erro) {
            mostrarErros(form, erro);
        }
    });
}

export async function telaPrestador(app, { id }) {
    app.innerHTML = carregando();
    let p;
    try {
        p = await api(`/prestadores/${id}`);
    } catch (erro) {
        telaErro(app, erro);
        return;
    }
    app.innerHTML = `
        <p><button type="button" class="botao botao-secundario botao-pequeno" onclick="history.back()">← Voltar</button></p>
        <section class="folha folha-media">
            <div class="folha-titulo"><strong>Perfil do prestador</strong><span>reputação verificada</span></div>
            <h1>${esc(p.nome)}</h1>
            <p>${estrelas(p.notaMedia)} <strong>${numero(p.notaMedia, 2)}</strong> · ${p.quantidadeAvaliacoes} avaliação(ões)
                · ${p.servicosConcluidos} serviço(s) concluído(s)</p>
            <p class="sub">Telefone: <span class="mono">${esc(p.telefone)}</span></p>
            <p class="aviso">Todas as avaliações vêm de ordens de serviço concluídas pela plataforma.</p>
        </section>
        <section class="folha folha-media">
            <div class="folha-titulo"><strong>Serviços oferecidos</strong><span>${p.servicos.length}</span></div>
            ${p.servicos.length ? `<div class="lista">${p.servicos.map((o) => `
                <div class="ficha" style="box-shadow:none">
                    <div><h3>${esc(o.descricao)}</h3><p class="sub"><span class="etiqueta">${esc(o.categoria)}</span>
                        · atende até ${o.raioAtendimentoKm} km</p></div>
                    <div class="preco"><strong>${moeda(o.valorBase)}</strong><small>${esc(COBRANCA[o.tipoCobranca])}</small></div>
                </div>`).join('')}</div>` : '<p>Nenhum serviço cadastrado.</p>'}
        </section>
        <section class="folha folha-media">
            <div class="folha-titulo"><strong>Avaliações</strong><span>${p.avaliacoes.length}</span></div>
            ${p.avaliacoes.length ? p.avaliacoes.map((a) => `
                <div style="border-bottom:1px solid var(--linha);padding:.6rem 0">
                    <p style="margin:0">${estrelas(a.nota)} <strong>${a.nota}/5</strong> · ${esc(a.clienteNome)} · ${data(a.dataAvaliacao)}</p>
                    ${a.comentario ? `<p style="margin:.25rem 0 0">“${esc(a.comentario)}”</p>` : ''}
                </div>`).join('') : '<p>Ainda sem avaliações.</p>'}
        </section>`;
}
