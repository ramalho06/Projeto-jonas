package com.example.jonas_project.encomenda.entity;

import com.example.jonas_project.common.exception.ValidacaoDominio;
import com.example.jonas_project.encomenda.enums.CanalOrigem;
import com.example.jonas_project.encomenda.enums.ModalidadeEntrega;
import com.example.jonas_project.encomenda.enums.StatusEncomenda;
import com.example.jonas_project.encomenda.enums.StatusSolicitacaoAlteracao;
import com.example.jonas_project.encomenda.enums.TipoEventoEncomenda;
import com.example.jonas_project.usuario.entity.Usuario;
import com.example.jonas_project.cliente.entity.Cliente;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.jonas_project.common.exception.ValidacaoDominio.decimalNaoNegativo;
import static com.example.jonas_project.common.exception.ValidacaoDominio.decimalPositivo;
import static com.example.jonas_project.common.exception.ValidacaoDominio.texto;

@Entity
@Table(name = "encomendas")
public class Encomenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Usuario responsavel;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_origem", nullable = false)
    private CanalOrigem canalOrigem;

    @Column(name = "data_solicitada", nullable = false)
    private LocalDate dataSolicitada;

    @Column(name = "cadastrada_em", nullable = false)
    private Instant cadastradaEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEncomenda status = StatusEncomenda.PENDENTE;

    @Column(name = "motivo_reprovacao", columnDefinition = "text")
    private String motivoReprovacao;

    @Column(name = "motivo_cancelamento", columnDefinition = "text")
    private String motivoCancelamento;

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

    @OneToMany(mappedBy = "encomenda", cascade = CascadeType.PERSIST)
    @OrderBy("ordem ASC")
    private List<ItemEncomenda> itens = new ArrayList<>();

    @Column(name = "numero_alteracoes", nullable = false)
    private int numeroAlteracoes;

    @OneToMany(mappedBy = "encomenda", cascade = CascadeType.PERSIST)
    @OrderBy("ocorridaEm ASC, id ASC")
    private List<HistoricoEncomenda> historico = new ArrayList<>();

    @OneToMany(mappedBy = "encomenda", cascade = CascadeType.PERSIST)
    private List<SolicitacaoAlteracao> solicitacoes = new ArrayList<>();

    @OneToMany(mappedBy = "encomenda", cascade = CascadeType.PERSIST)
    private List<ImpedimentoProducao> impedimentos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "materiais_conferidos_por_id")
    private Usuario materiaisConferidosPor;

    @Column(name = "materiais_conferidos_em")
    private Instant materiaisConferidosEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrega_registrada_por_id")
    private Usuario entregaRegistradaPor;

    @Column(name = "entregue_em")
    private Instant entregueEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidade_entrega")
    private ModalidadeEntrega modalidadeEntrega;

    @Version
    private long revisao;

    protected Encomenda() {
    }

    public Encomenda(String codigo, Cliente cliente, Usuario responsavel, CanalOrigem canalOrigem,
                     LocalDate dataSolicitada, Instant cadastradaEm, ItemEncomenda primeiroItem) {
        this.codigo = texto(codigo, "o código da encomenda");
        this.cliente = Objects.requireNonNull(cliente, "Informe o cliente da encomenda.");
        this.responsavel = Objects.requireNonNull(responsavel, "Informe o responsável pela encomenda.");
        this.canalOrigem = Objects.requireNonNull(canalOrigem, "Informe o canal de origem.");
        this.dataSolicitada = Objects.requireNonNull(dataSolicitada, "Informe a data solicitada.");
        this.cadastradaEm = Objects.requireNonNull(cadastradaEm, "Informe o instante do cadastro.");
        adicionarItem(primeiroItem);
    }

    public void adicionarItem(ItemEncomenda item) {
        exigirStatus(StatusEncomenda.PENDENTE);
        Objects.requireNonNull(item).vincular(this, itens.size());
        itens.add(item);
    }

    public void alterarItem(ItemEncomenda item, ItemEncomenda novosDados) {
        exigirStatus(StatusEncomenda.PENDENTE);
        exigirItemDaEncomenda(Objects.requireNonNull(item));
        item.alterar(Objects.requireNonNull(novosDados));
    }

    public void aprovar(BigDecimal precoBruto, BigDecimal desconto, BigDecimal esforcoHoras,
                                   LocalDate prazoPrometido, Instant inicioPlanejadoEm, Instant limiteAlteracao,
                                   Usuario autor, Instant quando) {
        exigirStatus(StatusEncomenda.PENDENTE);
        Objects.requireNonNull(autor, "Informe quem aprovou a encomenda.");
        Objects.requireNonNull(quando);
        if (quando.isBefore(cadastradaEm)) {
            throw new IllegalArgumentException("A aprovação não pode anteceder o cadastro.");
        }
        definirPlanejamento(precoBruto, desconto, esforcoHoras, prazoPrometido, inicioPlanejadoEm, limiteAlteracao);
        aprovadaPor = autor;
        aprovadaEm = quando;
        status = StatusEncomenda.APROVADA;
    }

    public void reprovar(String justificativa) {
        exigirStatus(StatusEncomenda.PENDENTE);
        motivoReprovacao = texto(justificativa, "a justificativa da reprovação");
        status = StatusEncomenda.REPROVADA;
    }

    public SolicitacaoAlteracao solicitarAlteracao(Usuario autor, CanalOrigem canal, Instant quando,
                                                  BigDecimal precoProposto, LocalDate prazoProposto,
                                                  ItemEncomenda item, Integer quantidadeProposta,
                                                  String personalizacaoProposta) {
        exigirStatus(StatusEncomenda.APROVADA);
        Objects.requireNonNull(quando);
        if (quando.isBefore(aprovadaEm) || quando.isAfter(limiteAlteracao)) {
            throw new IllegalStateException("A solicitação está fora do prazo de alteração.");
        }
        if (item != null) {
            exigirItemDaEncomenda(item);
        }
        SolicitacaoAlteracao solicitacao = new SolicitacaoAlteracao(this, autor, canal, quando,
                precoProposto, prazoProposto, item, quantidadeProposta, personalizacaoProposta);
        solicitacoes.add(solicitacao);
        return solicitacao;
    }

    public void aplicarAlteracao(SolicitacaoAlteracao solicitacao, Instant quando, BigDecimal totalPago) {
        exigirStatus(StatusEncomenda.APROVADA);
        if (!mesmaEncomenda(solicitacao.getEncomenda())) {
            throw new IllegalArgumentException("A solicitação pertence a outra encomenda.");
        }
        solicitacao.exigirAplicavel(quando);
        BigDecimal novoPreco = Objects.requireNonNullElse(solicitacao.getPrecoProposto(), precoTotal());
        if (novoPreco.compareTo(decimalNaoNegativo(totalPago, 2, "O total pago")) < 0) {
            throw new IllegalStateException("Concilie os recebimentos antes de aplicar o novo preço.");
        }

        ItemEncomenda item = solicitacao.getItem();
        String antes = descreverAlteracao(item);
        precoBruto = novoPreco.add(desconto);
        prazoPrometido = Objects.requireNonNullElse(solicitacao.getPrazoProposto(), prazoPrometido);
        if (item != null) {
            item.aplicarAlteracao(solicitacao.getQuantidadeProposta(), solicitacao.getPersonalizacaoProposta());
        }
        aprovadaPor = solicitacao.getAnalisadaPor();
        aprovadaEm = quando;
        numeroAlteracoes++;
        solicitacao.marcarAplicada();
        cienciaPor = null;
        cienciaEm = null;
        materiaisConferidosPor = null;
        materiaisConferidosEm = null;

        historico.add(new HistoricoEncomenda(this, solicitacao.getAnalisadaPor(),
                TipoEventoEncomenda.ALTERACAO_APROVADA, quando, antes, descreverAlteracao(item),
                solicitacao.getJustificativa(), false));
    }

    private String descreverAlteracao(ItemEncomenda item) {
        String dados = "Preço total: " + precoTotal().toPlainString() + "\nPrazo: " + prazoPrometido
                + "\nAprovação: " + descreverAcao(aprovadaPor, aprovadaEm)
                + "\nCiência: " + descreverAcao(cienciaPor, cienciaEm)
                + "\nConferência de materiais: " + descreverAcao(materiaisConferidosPor, materiaisConferidosEm);
        if (item == null) {
            return dados;
        }
        return dados + "\nItem " + (item.getOrdem() + 1) + " (" + item.getTipoProduto() + ")"
                + "\nQuantidade: " + item.getQuantidade() + "\nPersonalização: " + item.getPersonalizacao();
    }

    private String descreverAcao(Usuario autor, Instant quando) {
        if (autor == null) {
            return "Pendente";
        }
        String identificacao = autor.getId() == null ? autor.getNome() : autor.getNome() + " (#" + autor.getId() + ")";
        return identificacao + " em " + quando;
    }

    void registrarMudancaDeSolicitacao(TipoEventoEncomenda evento, Usuario autor, Instant quando,
                                        StatusSolicitacaoAlteracao anterior, SolicitacaoAlteracao solicitacao,
                                        String justificativa) {
        String referencia = solicitacao.getId() == null
                ? "Solicitação de " + solicitacao.getSolicitadaEm() : "Solicitação #" + solicitacao.getId();
        historico.add(new HistoricoEncomenda(this, autor, evento, quando,
                referencia + ": " + anterior, referencia + ": " + solicitacao.getStatus(), justificativa, false));
    }

    private void exigirItemDaEncomenda(ItemEncomenda item) {
        if (!mesmaEncomenda(item.getEncomenda())) {
            throw new IllegalArgumentException("O item não pertence à encomenda.");
        }
    }

    public ImpedimentoProducao registrarImpedimento(String descricao, Usuario responsavel, Instant quando) {
        exigirEmAberto();
        ImpedimentoProducao impedimento = new ImpedimentoProducao(this, descricao, responsavel, quando);
        impedimentos.add(impedimento);
        return impedimento;
    }

    public void conferirMateriais(Usuario funcionario, Instant quando) {
        exigirStatus(StatusEncomenda.APROVADA);
        Objects.requireNonNull(quando);
        if (quando.isBefore(aprovadaEm)) {
            throw new IllegalArgumentException("Confira os materiais após a aprovação da encomenda.");
        }
        materiaisConferidosPor = Objects.requireNonNull(funcionario);
        materiaisConferidosEm = quando;
    }

    public void confirmarCiencia(int alteracoesConsultadas, Usuario funcionario, Instant quando) {
        exigirStatus(StatusEncomenda.APROVADA);
        Objects.requireNonNull(funcionario, "Informe quem confirmou a leitura.");
        Objects.requireNonNull(quando, "Informe quando a leitura foi confirmada.");
        if (alteracoesConsultadas != numeroAlteracoes || quando.isBefore(aprovadaEm) || cienciaEm != null) {
            throw new IllegalStateException("A ciência exige a especificação vigente da encomenda.");
        }
        cienciaPor = funcionario;
        cienciaEm = quando;
    }

    public void iniciarProducao(Instant quando) {
        exigirStatus(StatusEncomenda.APROVADA);
        Objects.requireNonNull(quando);
        if (impedimentos.stream().anyMatch(ImpedimentoProducao::estaAberto)) {
            throw new IllegalStateException("Resolva os impedimentos antes de iniciar a produção.");
        }
        if (solicitacoes.stream().anyMatch(solicitacao -> solicitacao.bloqueiaProducaoEm(quando))) {
            throw new IllegalStateException("Resolva as solicitações de alteração válidas em aberto.");
        }
        if (materiaisConferidosEm == null || quando.isBefore(materiaisConferidosEm)) {
            throw new IllegalStateException("Confira os materiais antes de iniciar a produção.");
        }
        if (cienciaEm == null || quando.isBefore(cienciaEm)) {
            throw new IllegalStateException("Confirme a leitura da especificação vigente antes de iniciar a produção.");
        }
        for (SolicitacaoAlteracao solicitacao : solicitacoes) {
            if (solicitacao.estaEmAberto()) {
                solicitacao.expirar(quando);
            }
        }
        status = StatusEncomenda.EM_PRODUCAO;
    }

    public void marcarPronta() {
        exigirStatus(StatusEncomenda.EM_PRODUCAO);
        status = StatusEncomenda.PRONTA;
    }

    public void registrarEntrega(Usuario autor, Instant quando, ModalidadeEntrega modalidade,
                                     BigDecimal saldoEmAberto) {
        exigirStatus(StatusEncomenda.PRONTA);
        if (decimalNaoNegativo(saldoEmAberto, 2, "O saldo em aberto").signum() > 0) {
            throw new IllegalStateException("Quite o saldo antes de registrar a entrega.");
        }
        Objects.requireNonNull(autor, "Informe quem registrou a entrega.");
        Objects.requireNonNull(modalidade, "Informe a modalidade da entrega.");
        Objects.requireNonNull(quando, "Informe o momento da entrega.");
        entregaRegistradaPor = autor;
        entregueEm = quando;
        modalidadeEntrega = modalidade;
        status = StatusEncomenda.ENTREGUE;
    }

    public void cancelar(Usuario autor, String justificativa, Instant quando) {
        exigirEmAberto();
        Objects.requireNonNull(autor, "Informe quem cancelou a encomenda.");
        Objects.requireNonNull(quando, "Informe quando a encomenda foi cancelada.");
        if (quando.isBefore(cadastradaEm) || (aprovadaEm != null && quando.isBefore(aprovadaEm))
                || solicitacoes.stream().anyMatch(solicitacao -> quando.isBefore(solicitacao.getSolicitadaEm()))) {
            throw new IllegalArgumentException("A data do cancelamento antecede uma operação da encomenda.");
        }
        StatusEncomenda anterior = status;
        motivoCancelamento = texto(justificativa, "a justificativa do cancelamento");
        status = StatusEncomenda.CANCELADA;
        solicitacoes.forEach(SolicitacaoAlteracao::cancelar);
        historico.add(new HistoricoEncomenda(this, autor, TipoEventoEncomenda.CANCELADA, quando,
                anterior.name(), status.name(), motivoCancelamento, false));
    }

    public boolean mesmaEncomenda(Encomenda outra) {
        return outra != null && (this == outra || (id != null && id.equals(outra.getId())));
    }

    public BigDecimal precoTotal() {
        return precoBruto == null ? null : precoBruto.subtract(desconto);
    }

    public int getNumeroAlteracoes() {
        return numeroAlteracoes;
    }

    public List<HistoricoEncomenda> getHistorico() {
        return List.copyOf(historico);
    }

    public Long getId() {
        return id;
    }

    private void definirPlanejamento(BigDecimal precoBruto, BigDecimal desconto, BigDecimal esforcoHoras,
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

    private void exigirStatus(StatusEncomenda esperado) {
        if (status != esperado) {
            throw new IllegalStateException("A operação exige uma encomenda em estado " + esperado + ".");
        }
    }

    private void exigirEmAberto() {
        if (status == StatusEncomenda.REPROVADA || status == StatusEncomenda.CANCELADA
                || status == StatusEncomenda.ENTREGUE) {
            throw new IllegalStateException("A encomenda já foi encerrada.");
        }
    }

    public String getCodigo() {
        return codigo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Usuario getResponsavel() {
        return responsavel;
    }

    public CanalOrigem getCanalOrigem() {
        return canalOrigem;
    }

    public LocalDate getDataSolicitada() {
        return dataSolicitada;
    }

    public Instant getCadastradaEm() {
        return cadastradaEm;
    }

    public StatusEncomenda getStatus() {
        return status;
    }

    public String getMotivoReprovacao() {
        return motivoReprovacao;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
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

    public List<SolicitacaoAlteracao> getSolicitacoes() {
        return List.copyOf(solicitacoes);
    }

    public List<ImpedimentoProducao> getImpedimentos() {
        return List.copyOf(impedimentos);
    }

    public Usuario getMateriaisConferidosPor() {
        return materiaisConferidosPor;
    }

    public Instant getMateriaisConferidosEm() {
        return materiaisConferidosEm;
    }

    public Usuario getEntregaRegistradaPor() {
        return entregaRegistradaPor;
    }

    public Instant getEntregueEm() {
        return entregueEm;
    }

    public ModalidadeEntrega getModalidadeEntrega() {
        return modalidadeEntrega;
    }
}
