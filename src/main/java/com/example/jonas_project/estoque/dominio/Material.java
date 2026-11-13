package com.example.jonas_project.estoque.dominio;

import com.example.jonas_project.acesso.dominio.Usuario;
import com.example.jonas_project.encomendas.dominio.Encomenda;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

import static com.example.jonas_project.compartilhado.dominio.ValidacaoDominio.texto;
import static com.example.jonas_project.compartilhado.dominio.ValidacaoDominio.decimalPositivo;

@Entity
@Table(name = "materiais")
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "unidade_medida", nullable = false)
    private String unidadeMedida;

    @Column(name = "quantidade_fisica", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidadeFisica = BigDecimal.ZERO.setScale(3);

    @Column(name = "quantidade_reservada", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidadeReservada = BigDecimal.ZERO.setScale(3);

    @Version
    private long revisao;

    protected Material() {
    }

    public Material(String nome, String unidadeMedida) {
        this.nome = texto(nome, "o nome do material");
        this.unidadeMedida = texto(unidadeMedida, "a unidade de medida");
    }

    public BigDecimal saldoDisponivel() {
        return quantidadeFisica.subtract(quantidadeReservada);
    }

    public MovimentacaoEstoque registrarEntrada(BigDecimal quantidade, Usuario autor, Instant quando) {
        BigDecimal entrada = decimalPositivo(quantidade, 3, "A quantidade de entrada");
        MovimentacaoEstoque movimento = new MovimentacaoEstoque(this, null, TipoMovimentacaoEstoque.ENTRADA,
                entrada, autor, quando, null);
        quantidadeFisica = quantidadeFisica.add(entrada);
        return movimento;
    }

    public MovimentacaoEstoque registrarSaida(BigDecimal quantidade, Usuario autor, Instant quando) {
        return retirarDisponivel(quantidade, autor, quando, TipoMovimentacaoEstoque.SAIDA, null);
    }

    public MovimentacaoEstoque registrarPerda(BigDecimal quantidade, Usuario autor, Instant quando, String motivo) {
        return retirarDisponivel(quantidade, autor, quando, TipoMovimentacaoEstoque.PERDA,
                texto(motivo, "o motivo da perda"));
    }

    public ReservaMaterial reservar(Encomenda encomenda, BigDecimal quantidade, Usuario autor, Instant quando) {
        BigDecimal solicitada = decimalPositivo(quantidade, 3, "A quantidade da reserva");
        exigirDisponibilidade(solicitada);
        ReservaMaterial reserva = new ReservaMaterial(this, encomenda, solicitada, autor, quando);
        quantidadeReservada = quantidadeReservada.add(solicitada);
        return reserva;
    }

    public MovimentacaoEstoque consumir(ReservaMaterial reserva, BigDecimal quantidade, Usuario autor, Instant quando) {
        reserva.exigirMaterial(this);
        BigDecimal consumo = decimalPositivo(quantidade, 3, "A quantidade consumida");
        MovimentacaoEstoque movimento = new MovimentacaoEstoque(this, reserva, TipoMovimentacaoEstoque.CONSUMO,
                consumo, autor, quando, null);
        reserva.retirarSaldo(consumo);
        quantidadeReservada = quantidadeReservada.subtract(consumo);
        quantidadeFisica = quantidadeFisica.subtract(consumo);
        return movimento;
    }

    public void liberar(ReservaMaterial reserva) {
        reserva.exigirMaterial(this);
        BigDecimal saldo = reserva.getSaldoReservado();
        reserva.retirarSaldo(saldo);
        quantidadeReservada = quantidadeReservada.subtract(saldo);
    }

    private MovimentacaoEstoque retirarDisponivel(BigDecimal quantidade, Usuario autor, Instant quando,
                                                  TipoMovimentacaoEstoque tipo, String motivo) {
        BigDecimal retirada = decimalPositivo(quantidade, 3, "A quantidade da saída");
        exigirDisponibilidade(retirada);
        MovimentacaoEstoque movimento = new MovimentacaoEstoque(this, null, tipo, retirada, autor, quando, motivo);
        quantidadeFisica = quantidadeFisica.subtract(retirada);
        return movimento;
    }

    private void exigirDisponibilidade(BigDecimal quantidade) {
        if (quantidade.compareTo(saldoDisponivel()) > 0) {
            throw new IllegalStateException("Estoque disponível insuficiente.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public BigDecimal getQuantidadeFisica() {
        return quantidadeFisica;
    }

    public BigDecimal getQuantidadeReservada() {
        return quantidadeReservada;
    }
}
