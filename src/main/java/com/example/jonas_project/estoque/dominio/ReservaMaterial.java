package com.example.jonas_project.estoque.dominio;

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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "reservas_material")
public class ReservaMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "saldo_reservado", nullable = false, precision = 15, scale = 3)
    private BigDecimal saldoReservado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    protected ReservaMaterial() {
    }

    ReservaMaterial(Material material, Encomenda encomenda, BigDecimal quantidade, Usuario autor, Instant criadaEm) {
        this.material = Objects.requireNonNull(material);
        this.encomenda = Objects.requireNonNull(encomenda);
        this.quantidade = quantidade;
        this.saldoReservado = quantidade;
        this.autor = Objects.requireNonNull(autor);
        this.criadaEm = Objects.requireNonNull(criadaEm);
    }

    void exigirMaterial(Material esperado) {
        if (material != esperado && (esperado.getId() == null || !esperado.getId().equals(material.getId()))) {
            throw new IllegalArgumentException("A reserva pertence a outro material.");
        }
    }

    void retirarSaldo(BigDecimal retirada) {
        if (retirada.compareTo(saldoReservado) > 0) {
            throw new IllegalStateException("A quantidade supera o saldo da reserva.");
        }
        saldoReservado = saldoReservado.subtract(retirada);
    }

    public Long getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public Encomenda getEncomenda() {
        return encomenda;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public BigDecimal getSaldoReservado() {
        return saldoReservado;
    }

    public Usuario getAutor() {
        return autor;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }
}
