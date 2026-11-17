package com.example.jonas_project.clientes.dominio;

import com.example.jonas_project.acesso.dominio.PerfilUsuario;
import com.example.jonas_project.acesso.dominio.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String contato;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    protected Cliente() {
    }

    public Cliente(String nome, String contato) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Informe o nome do cliente.");
        }

        this.nome = nome.strip();
        atualizarContato(contato);
    }

    public void atualizarContato(String novoContato) {
        if (novoContato == null || novoContato.isBlank()) {
            throw new IllegalArgumentException("Informe um contato para o cliente.");
        }

        this.contato = novoContato.strip();
    }

    public void vincularConta(Usuario conta) {
        if (conta == null || conta.getPerfil() != PerfilUsuario.CLIENTE) {
            throw new IllegalArgumentException("A conta vinculada deve ter perfil de cliente.");
        }
        if (usuario != null) {
            throw new IllegalStateException("O cliente já tem uma conta vinculada.");
        }
        usuario = conta;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getContato() {
        return contato;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
