package br.unicesumar.maridodealuguel.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CLIENTE")
public class Cliente extends Usuario {

    protected Cliente() {
        // exigido pelo JPA
    }

    public Cliente(String nome, String email, String telefone, String senhaHash,
                   String endereco, Double latitude, Double longitude) {
        super(nome, email, telefone, senhaHash, endereco, latitude, longitude);
    }

    @Override
    public PerfilUsuario getPerfil() {
        return PerfilUsuario.CLIENTE;
    }
}
