package com.example.jonas_project.encomenda.entity;

import com.example.jonas_project.common.exception.ValidacaoDominio;
import com.example.jonas_project.encomenda.enums.CanalOrigem;
import com.example.jonas_project.encomenda.enums.StatusEncomenda;
import com.example.jonas_project.encomenda.enums.StatusSolicitacaoAlteracao;
import com.example.jonas_project.encomenda.enums.TipoEventoEncomenda;
import com.example.jonas_project.usuario.entity.Usuario;

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
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

import static com.example.jonas_project.common.exception.ValidacaoDominio.decimalNaoNegativo;
import static com.example.jonas_project.common.exception.ValidacaoDominio.texto;

@Entity
@Table(name = "solicitacoes_alteracao")
public class SolicitacaoAlteracao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @Column(name = "alteracoes_na_solicitacao", nullable = false)
    private int alteracoesNaSolicitacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrada_por_id", nullable = false)
    private Usuario registradaPor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalOrigem canal;

    @Column(name = "solicitada_em", nullable = false)
    private Instant solicitadaEm;

    @Column(name = "preco_proposto", precision = 15, scale = 2)
    private BigDecimal precoProposto;

    @Column(name = "prazo_proposto")
    private LocalDate prazoProposto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private ItemEncomenda item;

    @Column(name = "quantidade_proposta")
    private Integer quantidadeProposta;

    @Column(name = "personalizacao_proposta", columnDefinition = "text")
    private String personalizacaoProposta;

    @Version
    private long revisao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacaoAlteracao status = StatusSolicitacaoAlteracao.PENDENTE;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "analisada_por_id")
    private Usuario analisadaPor;

    @Column(name = "analisada_em")
    private Instant analisadaEm;

    @Column(columnDefinition = "text")
    private String justificativa;

    @Column(name = "concordancia_em")
    private Instant concordanciaEm;

    protected SolicitacaoAlteracao() {
    }

    SolicitacaoAlteracao(Encomenda encomenda, Usuario registradaPor, CanalOrigem canal, Instant solicitadaEm,
                         BigDecimal precoProposto, LocalDate prazoProposto, ItemEncomenda item,
                         Integer quantidadeProposta, String personalizacaoProposta) {
        this.encomenda = encomenda;
        this.alteracoesNaSolicitacao = encomenda.getNumeroAlteracoes();
        this.registradaPor = Objects.requireNonNull(registradaPor);
        this.canal = Objects.requireNonNull(canal);
        this.solicitadaEm = Objects.requireNonNull(solicitadaEm);
        boolean alteraItem = quantidadeProposta != null || personalizacaoProposta != null;
        if ((item != null) != alteraItem) {
            throw new IllegalArgumentException("Informe o item e a quantidade ou personalização proposta.");
        }
        if (quantidadeProposta != null && quantidadeProposta <= 0) {
            throw new IllegalArgumentException("A quantidade proposta deve ser positiva.");
        }

        this.precoProposto = precoProposto == null ? null : decimalNaoNegativo(precoProposto, 2, "O preço proposto");
        this.prazoProposto = prazoProposto;
        this.item = item;
        this.quantidadeProposta = quantidadeProposta;
        this.personalizacaoProposta = personalizacaoProposta == null ? null : texto(personalizacaoProposta, "a personalização");
        boolean mudouQuantidade = quantidadeProposta != null && quantidadeProposta != item.getQuantidade();
        boolean mudouPersonalizacao = this.personalizacaoProposta != null
                && !this.personalizacaoProposta.equals(item.getPersonalizacao());
        if (!alteraCondicoesComerciais() && !mudouQuantidade && !mudouPersonalizacao) {
            throw new IllegalArgumentException("Informe pelo menos uma alteração.");
        }
    }

    public void autorizar(Usuario analista, String motivo, Instant quando) {
        exigirEspecificacaoAtual();
        registrarAnalise(analista, motivo, quando);
        status = alteraCondicoesComerciais() ? StatusSolicitacaoAlteracao.AGUARDANDO_CONCORDANCIA
                : StatusSolicitacaoAlteracao.AUTORIZADA;
    }

    public void rejeitar(Usuario analista, String motivo, Instant quando) {
        registrarAnalise(analista, motivo, quando);
        StatusSolicitacaoAlteracao anterior = status;
        status = StatusSolicitacaoAlteracao.REJEITADA;
        encomenda.registrarMudancaDeSolicitacao(TipoEventoEncomenda.ALTERACAO_REJEITADA,
                analista, quando, anterior, this, justificativa);
    }

    public void confirmarConcordancia(Instant quando) {
        exigirEspecificacaoAtual();
        exigirPrazo(Objects.requireNonNull(quando));
        if (status != StatusSolicitacaoAlteracao.AGUARDANDO_CONCORDANCIA || quando.isBefore(analisadaEm)) {
            throw new IllegalStateException("A solicitação não pode receber concordância nesse momento.");
        }
        concordanciaEm = quando;
        status = StatusSolicitacaoAlteracao.AUTORIZADA;
    }

    public void expirar(Instant quando) {
        Objects.requireNonNull(quando);
        if (!estaEmAberto() || quando.isBefore(solicitadaEm)
                || (alteracoesNaSolicitacao == encomenda.getNumeroAlteracoes()
                && !quando.isAfter(encomenda.getLimiteAlteracao()))) {
            throw new IllegalStateException("A solicitação ainda não pode expirar.");
        }
        StatusSolicitacaoAlteracao anterior = status;
        String motivo = alteracoesNaSolicitacao == encomenda.getNumeroAlteracoes()
                ? "O prazo para alterações terminou."
                : "A solicitação foi baseada em uma especificação anterior.";
        status = StatusSolicitacaoAlteracao.EXPIRADA;
        encomenda.registrarMudancaDeSolicitacao(TipoEventoEncomenda.ALTERACAO_EXPIRADA,
                null, quando, anterior, this, motivo);
    }

    boolean bloqueiaProducaoEm(Instant quando) {
        return estaEmAberto() && (quando.isBefore(solicitadaEm)
                || (alteracoesNaSolicitacao == encomenda.getNumeroAlteracoes()
                && !quando.isAfter(encomenda.getLimiteAlteracao())));
    }

    public boolean estaEmAberto() {
        return switch (status) {
            case PENDENTE, AGUARDANDO_CONCORDANCIA, AUTORIZADA -> true;
            case APLICADA, REJEITADA, EXPIRADA, CANCELADA -> false;
        };
    }

    void cancelar() {
        if (estaEmAberto()) {
            status = StatusSolicitacaoAlteracao.CANCELADA;
        }
    }

    void exigirAplicavel(Instant quando) {
        exigirEspecificacaoAtual();
        exigirPrazo(Objects.requireNonNull(quando));
        Instant autorizadaEm = concordanciaEm == null ? analisadaEm : concordanciaEm;
        if (status != StatusSolicitacaoAlteracao.AUTORIZADA || quando.isBefore(autorizadaEm)) {
            throw new IllegalStateException("A solicitação precisa estar autorizada.");
        }
    }

    void marcarAplicada() {
        status = StatusSolicitacaoAlteracao.APLICADA;
    }

    private void registrarAnalise(Usuario analista, String motivo, Instant quando) {
        Objects.requireNonNull(quando);
        exigirPrazo(quando);
        if (status != StatusSolicitacaoAlteracao.PENDENTE || quando.isBefore(solicitadaEm)) {
            throw new IllegalStateException("A solicitação não está pendente de análise.");
        }
        Objects.requireNonNull(analista);
        justificativa = texto(motivo, "a justificativa da decisão");
        analisadaPor = analista;
        analisadaEm = quando;
    }

    private void exigirPrazo(Instant quando) {
        if (quando.isAfter(encomenda.getLimiteAlteracao())) {
            throw new IllegalStateException("O limite para alterações foi atingido.");
        }
    }

    public Long getId() {
        return id;
    }

    public Encomenda getEncomenda() {
        return encomenda;
    }

    private boolean alteraCondicoesComerciais() {
        return (precoProposto != null && precoProposto.compareTo(encomenda.precoTotal()) != 0)
                || (prazoProposto != null && !prazoProposto.equals(encomenda.getPrazoPrometido()));
    }

    private void exigirEspecificacaoAtual() {
        if (encomenda.getStatus() != StatusEncomenda.APROVADA
                || alteracoesNaSolicitacao != encomenda.getNumeroAlteracoes()) {
            throw new IllegalStateException("A solicitação não corresponde à especificação vigente.");
        }
    }

    public Usuario getRegistradaPor() {
        return registradaPor;
    }

    public CanalOrigem getCanal() {
        return canal;
    }

    public Instant getSolicitadaEm() {
        return solicitadaEm;
    }

    public BigDecimal getPrecoProposto() {
        return precoProposto;
    }

    public LocalDate getPrazoProposto() {
        return prazoProposto;
    }

    public ItemEncomenda getItem() {
        return item;
    }

    public Integer getQuantidadeProposta() {
        return quantidadeProposta;
    }

    public String getPersonalizacaoProposta() {
        return personalizacaoProposta;
    }

    public StatusSolicitacaoAlteracao getStatus() {
        return status;
    }

    public Usuario getAnalisadaPor() {
        return analisadaPor;
    }

    public Instant getAnalisadaEm() {
        return analisadaEm;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public Instant getConcordanciaEm() {
        return concordanciaEm;
    }
}
