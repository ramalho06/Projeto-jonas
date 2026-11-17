package com.example.jonas_project.acesso.dominio;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import static com.example.jonas_project.compartilhado.dominio.ValidacaoDominio.texto;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "senha_codificada", nullable = false)
    private String senhaCodificada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerfilUsuario perfil;

    @Column(nullable = false)
    private boolean ativo;

    @ElementCollection
    @CollectionTable(name = "funcoes_usuario", joinColumns = @JoinColumn(name = "usuario_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "funcao", nullable = false)
    private Set<FuncaoFuncionario> funcoes = new HashSet<>();

    protected Usuario() {
    }

    public Usuario(String nome, String email, String senhaCodificada, PerfilUsuario perfil) {
        this.nome = texto(nome, "o nome do usuário");
        this.email = texto(email, "o e-mail").toLowerCase(Locale.ROOT);
        this.senhaCodificada = texto(senhaCodificada, "a senha codificada");
        this.perfil = Objects.requireNonNull(perfil, "Informe o perfil.");
        this.ativo = true;
    }

    public void atualizarFuncoes(Set<FuncaoFuncionario> novasFuncoes) {
        if (perfil != PerfilUsuario.FUNCIONARIO) {
            throw new IllegalStateException("Somente funcionários podem ter funções operacionais.");
        }
        Set<FuncaoFuncionario> funcoesValidadas = Set.copyOf(novasFuncoes);
        funcoes.clear();
        funcoes.addAll(funcoesValidadas);
    }

    public void alterarSenhaCodificada(String novaSenhaCodificada) {
        senhaCodificada = texto(novaSenhaCodificada, "a senha codificada");
    }

    public void ativar() {
        ativo = true;
    }

    public void desativar() {
        ativo = false;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaCodificada() {
        return senhaCodificada;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Set<FuncaoFuncionario> getFuncoes() {
        return Set.copyOf(funcoes);
    }
}
