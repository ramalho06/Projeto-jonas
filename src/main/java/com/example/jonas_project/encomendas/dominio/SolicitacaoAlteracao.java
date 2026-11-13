package com.example.jonas_project.encomendas.dominio;

import com.example.jonas_project.acesso.dominio.Usuario;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

import static com.example.jonas_project.compartilhado.dominio.ValidacaoDominio.texto;

@Entity
@Table(name = "solicitacoes_alteracao")
public class SolicitacaoAlteracao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "versao_base_id", nullable = false)
    private VersaoEncomenda versaoBase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrada_por_id", nullable = false)
    private Usuario registradaPor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalOrigem canal;

    @Column(name = "solicitada_em", nullable = false)
    private Instant solicitadaEm;

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "versao_proposta_id", nullable = false, unique = true)
    private VersaoEncomenda versaoProposta;

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

    SolicitacaoAlteracao(Encomenda encomenda, VersaoEncomenda versaoBase, Usuario registradaPor,
                         CanalOrigem canal, Instant solicitadaEm, VersaoEncomenda versaoProposta) {
        this.encomenda = encomenda;
        this.versaoBase = versaoBase;
        this.registradaPor = Objects.requireNonNull(registradaPor);
        this.canal = Objects.requireNonNull(canal);
        this.solicitadaEm = Objects.requireNonNull(solicitadaEm);
        this.versaoProposta = versaoProposta;
    }

    public void autorizar(Usuario analista, String motivo, Instant quando) {
        registrarAnalise(analista, motivo, quando);
        boolean exigeConcordancia = versaoBase.precoTotal().compareTo(versaoProposta.precoTotal()) != 0
                || !versaoBase.getPrazoPrometido().equals(versaoProposta.getPrazoPrometido());
        status = exigeConcordancia ? StatusSolicitacaoAlteracao.AGUARDANDO_CONCORDANCIA
                : StatusSolicitacaoAlteracao.AUTORIZADA;
    }

    public void rejeitar(Usuario analista, String motivo, Instant quando) {
        registrarAnalise(analista, motivo, quando);
        status = StatusSolicitacaoAlteracao.REJEITADA;
    }

    public void confirmarConcordancia(Instant quando) {
        exigirPrazo(Objects.requireNonNull(quando));
        if (status != StatusSolicitacaoAlteracao.AGUARDANDO_CONCORDANCIA || quando.isBefore(analisadaEm)) {
            throw new IllegalStateException("A solicitação não pode receber concordância nesse momento.");
        }
        concordanciaEm = quando;
        status = StatusSolicitacaoAlteracao.AUTORIZADA;
    }

    public void expirar(Instant quando) {
        Objects.requireNonNull(quando);
        if (!estaEmAberto() || !quando.isAfter(versaoBase.getLimiteAlteracao())) {
            throw new IllegalStateException("A solicitação ainda não pode expirar.");
        }
        status = StatusSolicitacaoAlteracao.EXPIRADA;
    }

    public boolean estaEmAberto() {
        return switch (status) {
            case PENDENTE, AGUARDANDO_CONCORDANCIA, AUTORIZADA -> true;
            case APLICADA, REJEITADA, EXPIRADA -> false;
        };
    }

    void exigirAplicavel(Instant quando) {
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
        if (quando.isAfter(versaoBase.getLimiteAlteracao())) {
            throw new IllegalStateException("O limite para alterações foi atingido.");
        }
    }

    public Long getId() {
        return id;
    }

    public Encomenda getEncomenda() {
        return encomenda;
    }

    public VersaoEncomenda getVersaoBase() {
        return versaoBase;
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

    public VersaoEncomenda getVersaoProposta() {
        return versaoProposta;
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
