package com.example.jonas_project.encomendas.dominio;

import com.example.jonas_project.acesso.dominio.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.jonas_project.compartilhado.dominio.ValidacaoDominio.decimalNaoNegativo;
import static com.example.jonas_project.compartilhado.dominio.ValidacaoDominio.decimalPositivo;

@Entity
@Table(name = "versoes_encomenda", uniqueConstraints = @UniqueConstraint(columnNames = {"encomenda_id", "numero"}))
public class VersaoEncomenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    private Integer numero;

    @Column(name = "preco_bruto", precision = 15, scale = 2)
    private BigDecimal precoBruto;

    @Column(precision = 15, scale = 2)
    private BigDecimal desconto;

    @Column(name = "esforco_horas", precision = 8, scale = 2)
    private BigDecimal esforcoHoras;

    @Column(name = "prazo_prometido")
    private LocalDate prazoPrometido;

    @Column(name = "inicio_planejado_em")
    private Instant inicioPlanejadoEm;

    @Column(name = "limite_alteracao")
    private Instant limiteAlteracao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprovada_por_id")
    private Usuario aprovadaPor;

    @Column(name = "aprovada_em")
    private Instant aprovadaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciencia_por_id")
    private Usuario cienciaPor;

    @Column(name = "ciencia_em")
    private Instant cienciaEm;

    @OneToMany(mappedBy = "versao", cascade = CascadeType.PERSIST)
    @OrderBy("ordem ASC")
    private List<ItemEncomenda> itens = new ArrayList<>();

    protected VersaoEncomenda() {
    }

    VersaoEncomenda(Encomenda encomenda, List<ItemEncomenda> itens) {
        this.encomenda = encomenda;
        if (itens.isEmpty()) {
            throw new IllegalArgumentException("Uma versão deve ter pelo menos um item.");
        }
        itens.forEach(this::adicionarItem);
    }

    void adicionarItem(ItemEncomenda item) {
        itens.add(item.copiarPara(this, itens.size()));
    }

    void alterarItem(ItemEncomenda item, ItemEncomenda novosDados) {
        if (!itens.contains(item)) {
            throw new IllegalArgumentException("O item não pertence à versão.");
        }
        item.alterar(novosDados);
    }

    void definirPlanejamento(BigDecimal precoBruto, BigDecimal desconto, BigDecimal esforcoHoras,
                             LocalDate prazoPrometido, Instant inicioPlanejadoEm, Instant limiteAlteracao) {
        BigDecimal precoValidado = decimalNaoNegativo(precoBruto, 2, "O preço bruto");
        BigDecimal descontoValidado = decimalNaoNegativo(desconto, 2, "O desconto");
        BigDecimal esforcoValidado = decimalPositivo(esforcoHoras, 2, "O esforço");
        Objects.requireNonNull(prazoPrometido, "Informe o prazo prometido.");
        Objects.requireNonNull(inicioPlanejadoEm, "Informe o início planejado.");
        Objects.requireNonNull(limiteAlteracao, "Informe o limite de alteração.");
        if (descontoValidado.compareTo(precoValidado) > 0) {
            throw new IllegalArgumentException("O desconto não pode superar o preço bruto.");
        }
        if (limiteAlteracao.isAfter(inicioPlanejadoEm)) {
            throw new IllegalArgumentException("O limite de alteração não pode passar do início planejado.");
        }
        this.precoBruto = precoValidado;
        this.desconto = descontoValidado;
        this.esforcoHoras = esforcoValidado;
        this.prazoPrometido = prazoPrometido;
        this.inicioPlanejadoEm = inicioPlanejadoEm;
        this.limiteAlteracao = limiteAlteracao;
    }

    void aprovar(int numero, Usuario autor, Instant quando) {
        this.numero = numero;
        this.aprovadaPor = autor;
        this.aprovadaEm = quando;
    }

    void confirmarCiencia(Usuario funcionario, Instant quando) {
        Objects.requireNonNull(funcionario, "Informe quem confirmou a versão.");
        Objects.requireNonNull(quando, "Informe quando a versão foi confirmada.");
        if (aprovadaEm == null || quando.isBefore(aprovadaEm) || cienciaEm != null) {
            throw new IllegalStateException("A versão não pode receber confirmação nesse momento.");
        }
        cienciaPor = funcionario;
        cienciaEm = quando;
    }

    public BigDecimal precoTotal() {
        return precoBruto == null ? null : precoBruto.subtract(desconto);
    }

    public boolean mesmaVersao(VersaoEncomenda outra) {
        return outra != null && (this == outra || (id != null && id.equals(outra.getId())));
    }

    public Long getId() {
        return id;
    }

    public Encomenda getEncomenda() {
        return encomenda;
    }

    public Integer getNumero() {
        return numero;
    }

    public BigDecimal getPrecoBruto() {
        return precoBruto;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public BigDecimal getEsforcoHoras() {
        return esforcoHoras;
    }

    public LocalDate getPrazoPrometido() {
        return prazoPrometido;
    }

    public Instant getInicioPlanejadoEm() {
        return inicioPlanejadoEm;
    }

    public Instant getLimiteAlteracao() {
        return limiteAlteracao;
    }

    public Usuario getAprovadaPor() {
        return aprovadaPor;
    }

    public Instant getAprovadaEm() {
        return aprovadaEm;
    }

    public Usuario getCienciaPor() {
        return cienciaPor;
    }

    public Instant getCienciaEm() {
        return cienciaEm;
    }

    public List<ItemEncomenda> getItens() {
        return List.copyOf(itens);
    }
}
