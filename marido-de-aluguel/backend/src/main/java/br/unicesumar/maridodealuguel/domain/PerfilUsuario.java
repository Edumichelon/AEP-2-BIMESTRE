package br.unicesumar.maridodealuguel.domain;

/**
 * Papel de segurança de cada tipo de usuário (RF01 / RNF02).
 */
public enum PerfilUsuario {
    CLIENTE,
    PRESTADOR;

    /** Nome da autoridade no padrão do Spring Security (ROLE_*). */
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
