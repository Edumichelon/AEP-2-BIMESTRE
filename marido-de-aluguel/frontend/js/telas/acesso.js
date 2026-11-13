// RF01: login e cadastro de cliente ou prestador.
import { api } from '../api.js';
import { entrar } from '../estado.js';
import {
    campoLocalizacao, dadosForm, enviando, ligarCampoLocalizacao, mascaraTelefone, mostrarErros, toast,
} from '../ui.js';

export const telaInicialDoPerfil = (perfil) => (perfil === 'PRESTADOR' ? '#/servicos' : '#/buscar');

export function telaEntrar(app) {
    app.innerHTML = `
        <section class="folha folha-estreita">
            <div class="folha-titulo"><strong>Acesso</strong><span>OS · login</span></div>
            <h1>Entrar</h1>
            <p>Compare prestadores de pequenos reparos em Maringá e contrate com ordem de serviço registrada.</p>
            <form id="form-login" novalidate>
                <div class="campo">
                    <label for="email">E-mail</label>
                    <input id="email" name="email" type="email" autocomplete="username" required>
                </div>
                <div class="campo">
                    <label for="senha">Senha</label>
                    <input id="senha" name="senha" type="password" autocomplete="current-password" required>
                </div>
                <p class="erro-geral" role="alert"></p>
                <button class="botao botao-bloco" type="submit">Entrar</button>
            </form>
            <p style="margin-top:1rem">Ainda não tem conta? <a href="#/cadastro">Cadastre-se</a></p>
            <p class="aviso"><strong>Contas de demonstração</strong> (senha <code>senha123</code>):<br>
                cliente <code>camila@email.com</code> · prestador <code>douglas@email.com</code></p>
        </section>`;

    const form = app.querySelector('#form-login');
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            const sessao = await enviando(form, () => api('/auth/login', { metodo: 'POST', corpo: dadosForm(form) }));
            entrar(sessao);
            toast(`Olá, ${sessao.usuario.nome.split(' ')[0]}!`);
            location.hash = telaInicialDoPerfil(sessao.usuario.perfil);
        } catch (erro) {
            mostrarErros(form, erro);
        }
    });
    form.email.focus();
}

export function telaCadastro(app) {
    app.innerHTML = `
        <section class="folha folha-media">
            <div class="folha-titulo"><strong>Cadastro</strong><span>OS · novo usuário</span></div>
            <h1>Criar conta</h1>
            <form id="form-cadastro" novalidate>
                <fieldset>
                    <legend class="campo"><span class="legenda">Tipo de perfil</span></legend>
                    <div class="opcoes">
                        <label class="opcao"><input type="radio" name="perfil" value="CLIENTE" checked>
                            <span><strong>Cliente</strong><small>Quero contratar reparos</small></span></label>
                        <label class="opcao"><input type="radio" name="perfil" value="PRESTADOR">
                            <span><strong>Prestador</strong><small>Quero oferecer meus serviços</small></span></label>
                    </div>
                </fieldset>
                <div class="campo">
                    <label for="nome">Nome completo</label>
                    <input id="nome" name="nome" autocomplete="name" maxlength="100" required>
                </div>
                <div class="linha-campos">
                    <div class="campo">
                        <label for="email">E-mail</label>
                        <input id="email" name="email" type="email" autocomplete="email" maxlength="100" required>
                    </div>
                    <div class="campo">
                        <label for="telefone">Telefone (com DDD)</label>
                        <input id="telefone" name="telefone" type="tel" inputmode="tel" autocomplete="tel"
                               placeholder="(44) 99999-9999" required>
                    </div>
                </div>
                <div class="campo">
                    <label for="senha">Senha</label>
                    <input id="senha" name="senha" type="password" autocomplete="new-password" minlength="6" required>
                    <span class="dica">Mínimo de 6 caracteres.</span>
                </div>
                <div class="campo">
                    <label for="endereco">Endereço (rua, número, bairro, cidade)</label>
                    <input id="endereco" name="endereco" autocomplete="street-address" maxlength="150">
                </div>
                ${campoLocalizacao(null, null)}
                <p class="erro-geral" role="alert"></p>
                <button class="botao botao-bloco" type="submit">Criar conta</button>
            </form>
            <p style="margin-top:1rem">Já tem conta? <a href="#/entrar">Entrar</a></p>
        </section>`;

    const form = app.querySelector('#form-cadastro');
    mascaraTelefone(form.telefone);
    ligarCampoLocalizacao(form);
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const dados = dadosForm(form);
        const corpo = {
            nome: dados.nome,
            email: dados.email,
            telefone: dados.telefone,
            senha: dados.senha,
            endereco: dados.endereco,
            perfil: dados.perfil,
            latitude: dados.latitude ? Number(dados.latitude) : null,
            longitude: dados.longitude ? Number(dados.longitude) : null,
        };
        try {
            const sessao = await enviando(form, () => api('/auth/cadastro', { metodo: 'POST', corpo }));
            entrar(sessao);
            toast('Conta criada com sucesso!');
            location.hash = telaInicialDoPerfil(sessao.usuario.perfil);
        } catch (erro) {
            // latitude/longitude são ocultos: mostra o erro no select de bairro
            if (erro.campos?.latitude || erro.campos?.longitude) {
                erro.campos.bairro = erro.campos.latitude ?? erro.campos.longitude;
            }
            mostrarErros(form, erro);
        }
    });
}
