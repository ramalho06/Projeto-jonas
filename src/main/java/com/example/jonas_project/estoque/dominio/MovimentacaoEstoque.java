package com.example.jonas_project.estoque.dominio;

import com.example.jonas_project.acesso.dominio.Usuario;

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

@Entity
@Table(name = "movimentacoes_estoque")
public class MovimentacaoEstoque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "reserva_id")
    private ReservaMaterial reserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacaoEstoque tipo;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(name = "ocorrida_em", nullable = false)
    private Instant ocorridaEm;

    @Column(columnDefinition = "text")
    private String motivo;

    protected MovimentacaoEstoque() {
    }

    MovimentacaoEstoque(Material material, ReservaMaterial reserva, TipoMovimentacaoEstoque tipo,
                        BigDecimal quantidade, Usuario autor, Instant ocorridaEm, String motivo) {
        this.material = material;
        this.reserva = reserva;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.autor = Objects.requireNonNull(autor);
        this.ocorridaEm = Objects.requireNonNull(ocorridaEm);
        this.motivo = motivo;
    }

    public Long getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public ReservaMaterial getReserva() {
        return reserva;
    }

    public TipoMovimentacaoEstoque getTipo() {
        return tipo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public Usuario getAutor() {
        return autor;
    }

    public Instant getOcorridaEm() {
        return ocorridaEm;
    }

    public String getMotivo() {
        return motivo;
    }
}
