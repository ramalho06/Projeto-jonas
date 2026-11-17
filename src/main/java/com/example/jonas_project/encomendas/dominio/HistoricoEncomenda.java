package com.example.jonas_project.encomendas.dominio;

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

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "historicos_encomenda")
public class HistoricoEncomenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEventoEncomenda operacao;

    @Column(name = "ocorrida_em", nullable = false)
    private Instant ocorridaEm;

    @Column(name = "dados_anteriores", columnDefinition = "text")
    private String dadosAnteriores;

    @Column(name = "dados_novos", columnDefinition = "text")
    private String dadosNovos;

    @Column(columnDefinition = "text")
    private String justificativa;

    @Column(name = "visivel_ao_cliente", nullable = false)
    private boolean visivelAoCliente;

    protected HistoricoEncomenda() {
    }

    public HistoricoEncomenda(Encomenda encomenda, Usuario autor, TipoEventoEncomenda operacao,
                              Instant ocorridaEm, String dadosAnteriores, String dadosNovos,
                              String justificativa, boolean visivelAoCliente) {
        this.encomenda = Objects.requireNonNull(encomenda);
        this.autor = autor;
        this.operacao = Objects.requireNonNull(operacao);
        this.ocorridaEm = Objects.requireNonNull(ocorridaEm);
        this.dadosAnteriores = dadosAnteriores;
        this.dadosNovos = dadosNovos;
        this.justificativa = justificativa;
        this.visivelAoCliente = visivelAoCliente;
    }

    public Long getId() {
        return id;
    }

    public Encomenda getEncomenda() {
        return encomenda;
    }

    public Usuario getAutor() {
        return autor;
    }

    public TipoEventoEncomenda getOperacao() {
        return operacao;
    }

    public Instant getOcorridaEm() {
        return ocorridaEm;
    }

    public String getDadosAnteriores() {
        return dadosAnteriores;
    }

    public String getDadosNovos() {
        return dadosNovos;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public boolean isVisivelAoCliente() {
        return visivelAoCliente;
    }
}
