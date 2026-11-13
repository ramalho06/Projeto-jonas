package com.example.jonas_project.producao.dominio;

import com.example.jonas_project.acesso.dominio.Usuario;
import com.example.jonas_project.encomendas.dominio.Encomenda;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

import static com.example.jonas_project.compartilhado.dominio.ValidacaoDominio.texto;

@Entity
@Table(name = "impedimentos_producao")
public class ImpedimentoProducao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Usuario responsavel;

    @Column(name = "registrado_em", nullable = false)
    private Instant registradoEm;

    @Column(name = "resolvido_em")
    private Instant resolvidoEm;

    protected ImpedimentoProducao() {
    }

    public ImpedimentoProducao(Encomenda encomenda, String descricao, Usuario responsavel, Instant registradoEm) {
        this.encomenda = Objects.requireNonNull(encomenda);
        this.descricao = texto(descricao, "a descrição do impedimento");
        this.responsavel = Objects.requireNonNull(responsavel);
        this.registradoEm = Objects.requireNonNull(registradoEm);
    }

    public boolean estaAberto() {
        return resolvidoEm == null;
    }

    public void resolver(Instant quando) {
        Objects.requireNonNull(quando);
        if (!estaAberto() || quando.isBefore(registradoEm)) {
            throw new IllegalStateException("O impedimento não pode ser resolvido nesse momento.");
        }
        resolvidoEm = quando;
    }

    public Long getId() {
        return id;
    }

    public Encomenda getEncomenda() {
        return encomenda;
    }

    public String getDescricao() {
        return descricao;
    }

    public Usuario getResponsavel() {
        return responsavel;
    }

    public Instant getRegistradoEm() {
        return registradoEm;
    }

    public Instant getResolvidoEm() {
        return resolvidoEm;
    }
}
