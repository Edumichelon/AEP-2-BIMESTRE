package br.unicesumar.maridodealuguel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

/**
 * Superclasse abstrata da herança Usuario -> Cliente / Prestador.
 * Mapeada em tabela única (usuarios), com tipo_usuario como discriminador, como no DER.
 */
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(length = 150)
    private String endereco;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    protected Usuario() {
        // exigido pelo JPA
    }

    protected Usuario(String nome, String email, String telefone, String senhaHash,
                      String endereco, Double latitude, Double longitude) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.senhaHash = senhaHash;
        this.endereco = endereco;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Papel de segurança do usuário. Cada subclasse responde com o seu próprio perfil (@Override),
     * e é esse valor que vai para o token JWT e para as checagens de acesso de cada endpoint.
     */
    public abstract PerfilUsuario getPerfil();

    /** Atualização dos dados de perfil (o e-mail e a senha não mudam por aqui). */
    public void atualizarPerfil(String nome, String telefone, String endereco, Double latitude, Double longitude) {
        this.nome = nome;
        this.telefone = telefone;
        this.endereco = endereco;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public String getEndereco() {
        return endereco;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
