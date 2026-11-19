package com.example.jonas_project.pagamento.entity;

import com.example.jonas_project.common.exception.ValidacaoDominio;
import com.example.jonas_project.pagamento.enums.FormaPagamento;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import static com.example.jonas_project.common.exception.ValidacaoDominio.texto;
import static com.example.jonas_project.common.exception.ValidacaoDominio.decimalNaoNegativo;
import static com.example.jonas_project.common.exception.ValidacaoDominio.decimalPositivo;

@Entity
@Table(name = "pagamentos", uniqueConstraints = @UniqueConstraint(columnNames = {"encomenda_id", "chave_operacao"}))
public class Pagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @Column(name = "chave_operacao", nullable = false)
    private String chaveOperacao;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaPagamento forma;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Usuario responsavel;

    @Column(name = "recebido_em", nullable = false)
    private Instant recebidoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estornado_por_id")
    private Usuario estornadoPor;

    @Column(name = "estornado_em")
    private Instant estornadoEm;

    @Column(name = "justificativa_estorno", columnDefinition = "text")
    private String justificativaEstorno;

    @Version
    private long revisao;

    protected Pagamento() {
    }

    public Pagamento(Encomenda encomenda, String chaveOperacao, BigDecimal valor, FormaPagamento forma,
                     Usuario responsavel, Instant recebidoEm, BigDecimal saldoEmAberto) {
        this.encomenda = Objects.requireNonNull(encomenda);
        this.chaveOperacao = texto(chaveOperacao, "a chave da operação");
        this.valor = decimalPositivo(valor, 2, "O valor do recebimento");
        this.forma = Objects.requireNonNull(forma);
        this.responsavel = Objects.requireNonNull(responsavel);
        this.recebidoEm = Objects.requireNonNull(recebidoEm);
        if (this.valor.compareTo(decimalNaoNegativo(saldoEmAberto, 2, "O saldo em aberto")) > 0) {
            throw new IllegalStateException("O pagamento não pode superar o saldo em aberto.");
        }
    }

    public void estornar(Usuario autor, String justificativa, Instant quando) {
        if (estornadoEm != null) {
            throw new IllegalStateException("O pagamento já foi estornado.");
        }
        Objects.requireNonNull(quando);
        if (quando.isBefore(recebidoEm)) {
            throw new IllegalArgumentException("O estorno não pode anteceder o recebimento.");
        }
        Objects.requireNonNull(autor, "Informe quem registrou o estorno.");
        justificativaEstorno = texto(justificativa, "a justificativa do estorno");
        estornadoPor = autor;
        estornadoEm = quando;
    }

    public BigDecimal valorValido() {
        return estornadoEm == null ? valor : BigDecimal.ZERO.setScale(2);
    }

    public Long getId() {
        return id;
    }

    public Encomenda getEncomenda() {
        return encomenda;
    }

    public String getChaveOperacao() {
        return chaveOperacao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public FormaPagamento getForma() {
        return forma;
    }

    public Usuario getResponsavel() {
        return responsavel;
    }

    public Instant getRecebidoEm() {
        return recebidoEm;
    }

    public Usuario getEstornadoPor() {
        return estornadoPor;
    }

    public Instant getEstornadoEm() {
        return estornadoEm;
    }

    public String getJustificativaEstorno() {
        return justificativaEstorno;
    }
}
