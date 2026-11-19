package com.example.jonas_project.custo.entity;

import com.example.jonas_project.common.exception.ValidacaoDominio;
import com.example.jonas_project.custo.enums.CategoriaCusto;
import com.example.jonas_project.usuario.entity.Usuario;
import com.example.jonas_project.encomenda.entity.Encomenda;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import static com.example.jonas_project.common.exception.ValidacaoDominio.texto;
import static com.example.jonas_project.common.exception.ValidacaoDominio.decimalNaoNegativo;

@Entity
@Table(name = "custos_encomenda")
public class CustoEncomenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaCusto categoria;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Usuario responsavel;

    @Column(name = "registrado_em", nullable = false)
    private Instant registradoEm;

    protected CustoEncomenda() {
    }

    public CustoEncomenda(Encomenda encomenda, CategoriaCusto categoria, BigDecimal valor,
                          String descricao, Usuario responsavel, Instant registradoEm) {
        this.encomenda = Objects.requireNonNull(encomenda);
        this.categoria = Objects.requireNonNull(categoria);
        this.valor = decimalNaoNegativo(valor, 2, "O valor do custo");
        this.descricao = texto(descricao, "a descrição do custo");
        this.responsavel = Objects.requireNonNull(responsavel);
        this.registradoEm = Objects.requireNonNull(registradoEm);
    }

    public Long getId() {
        return id;
    }

    public Encomenda getEncomenda() {
        return encomenda;
    }

    public CategoriaCusto getCategoria() {
        return categoria;
    }

    public BigDecimal getValor() {
        return valor;
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
}
