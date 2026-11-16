package com.example.jonas_project.encomendas.dominio;

import com.example.jonas_project.acesso.dominio.PerfilUsuario;
import com.example.jonas_project.acesso.dominio.Usuario;
import com.example.jonas_project.clientes.dominio.Cliente;
import com.example.jonas_project.estoque.dominio.Material;
import com.example.jonas_project.financeiro.dominio.FormaPagamento;
import com.example.jonas_project.financeiro.dominio.Pagamento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncomendaTest {
    private static final Instant CADASTRO = Instant.parse("2026-11-12T12:00:00Z");
    private static final Instant APROVACAO = CADASTRO.plusSeconds(60);
    private static final Instant ALTERACAO = APROVACAO.plusSeconds(60);
    private static final LocalDate PRAZO = LocalDate.of(2026, 11, 30);
    private static final Instant INICIO_PLANEJADO = Instant.parse("2026-11-25T12:00:00Z");
    private static final Instant LIMITE_ALTERACAO = Instant.parse("2026-11-24T12:00:00Z");
    private static final BigDecimal DUAS_HORAS = new BigDecimal("2.00");
    private static final Usuario RESPONSAVEL = new Usuario("Rita", "rita@example.com", "hash-de-teste",
            PerfilUsuario.ADMINISTRADOR);

    @Test
    void iniciaPendenteComUmItemEAceitaOutroItem() {
        Encomenda encomenda = novaEncomenda();
        assertEquals(StatusEncomenda.PENDENTE, encomenda.getStatus());
        assertEquals(1, encomenda.getItens().size());

        encomenda.adicionarItem(new ItemEncomenda("Caneca", 5, "Logotipo azul", List.of()));

        assertEquals(2, encomenda.getItens().size());
        assertEquals("Caneca", encomenda.getItens().getLast().getTipoProduto());
        assertThrows(UnsupportedOperationException.class, () -> encomenda.getItens().clear());
    }

    @Test
    void rejeitaItemComQuantidadeInvalida() {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemEncomenda("Caneca", 0, "Logotipo azul", List.of()));
    }

    @Test
    void exigeJustificativaAntesDeReprovar() {
        Encomenda encomenda = novaEncomenda();

        assertThrows(IllegalArgumentException.class, () -> encomenda.reprovar(" "));
        assertEquals(StatusEncomenda.PENDENTE, encomenda.getStatus());
        assertNull(encomenda.getMotivoReprovacao());
    }

    @Test
    void impedeMudancasDiretasDepoisDaReprovacao() {
        Encomenda encomenda = novaEncomenda();

        encomenda.reprovar("Prazo inviável");

        assertEquals(StatusEncomenda.REPROVADA, encomenda.getStatus());
        assertEquals("Prazo inviável", encomenda.getMotivoReprovacao());
        assertThrows(IllegalStateException.class, () -> encomenda.reprovar("Outro motivo"));
        assertThrows(IllegalStateException.class,
                () -> encomenda.adicionarItem(item("Logotipo azul")));
        assertEquals(1, encomenda.getItens().size());
    }

    @Test
    void aplicaAlteracaoAposConcordanciaRegistraHistoricoEExigeNovaCiencia() {
        Encomenda encomenda = encomendaAprovada();
        ItemEncomenda item = encomenda.getItens().getFirst();
        encomenda.confirmarCiencia(0, RESPONSAVEL, APROVACAO);
        SolicitacaoAlteracao solicitacao = encomenda.solicitarAlteracao(RESPONSAVEL, CanalOrigem.WHATSAPP,
                ALTERACAO, new BigDecimal("120"), null, item, 12, "Nome em azul");
        solicitacao.autorizar(RESPONSAVEL, "Personalização atualizada", ALTERACAO);

        assertThrows(IllegalStateException.class,
                () -> encomenda.aplicarAlteracao(solicitacao, ALTERACAO, BigDecimal.ZERO));
        assertEquals(new BigDecimal("100.00"), encomenda.precoTotal());
        assertEquals("Nome da cliente em dourado", item.getPersonalizacao());
        assertTrue(encomenda.getHistorico().isEmpty());

        solicitacao.confirmarConcordancia(ALTERACAO);
        encomenda.aplicarAlteracao(solicitacao, ALTERACAO, BigDecimal.ZERO);

        assertSame(item, encomenda.getItens().getFirst());
        assertEquals(12, item.getQuantidade());
        assertEquals("Nome em azul", item.getPersonalizacao());
        assertEquals(new BigDecimal("120.00"), encomenda.precoTotal());
        assertEquals(PRAZO, encomenda.getPrazoPrometido());
        assertEquals(StatusSolicitacaoAlteracao.APLICADA, solicitacao.getStatus());
        HistoricoEncomenda registro = encomenda.getHistorico().getFirst();
        assertEquals(TipoEventoEncomenda.ALTERACAO_APROVADA, registro.getOperacao());
        assertSame(RESPONSAVEL, registro.getAutor());
        assertTrue(registro.getDadosAnteriores().contains("100.00"));
        assertTrue(registro.getDadosAnteriores().contains("dourado"));
        assertTrue(registro.getDadosNovos().contains("120.00"));
        assertTrue(registro.getDadosNovos().contains("azul"));

        encomenda.conferirMateriais(RESPONSAVEL, ALTERACAO);
        assertThrows(IllegalStateException.class, () -> encomenda.iniciarProducao(ALTERACAO));
        assertThrows(IllegalStateException.class, () -> encomenda.confirmarCiencia(0, RESPONSAVEL, ALTERACAO));

        encomenda.confirmarCiencia(1, RESPONSAVEL, ALTERACAO);
        encomenda.iniciarProducao(ALTERACAO);
        assertEquals(StatusEncomenda.EM_PRODUCAO, encomenda.getStatus());

        encomenda.marcarPronta();
        assertThrows(IllegalStateException.class, () ->
                encomenda.registrarEntrega(RESPONSAVEL, ALTERACAO, ModalidadeEntrega.RETIRADA, BigDecimal.ONE));
        assertNull(encomenda.getEntregueEm());
        encomenda.registrarEntrega(RESPONSAVEL, ALTERACAO, ModalidadeEntrega.RETIRADA, BigDecimal.ZERO);
        assertEquals(StatusEncomenda.ENTREGUE, encomenda.getStatus());
        assertEquals(ALTERACAO, encomenda.getEntregueEm());
        assertEquals(ModalidadeEntrega.RETIRADA, encomenda.getModalidadeEntrega());
        assertSame(RESPONSAVEL, encomenda.getEntregaRegistradaPor());
    }

    @Test
    void rejeitaLimiteDeAlteracaoPosteriorAoInicioPlanejado() {
        Encomenda encomenda = novaEncomenda();

        assertThrows(IllegalArgumentException.class, () ->
                encomenda.aprovar(new BigDecimal("100"), BigDecimal.ZERO, DUAS_HORAS, PRAZO,
                        INICIO_PLANEJADO, INICIO_PLANEJADO.plusSeconds(1), RESPONSAVEL, APROVACAO));
        assertEquals(StatusEncomenda.PENDENTE, encomenda.getStatus());
        assertNull(encomenda.getAprovadaEm());
    }

    @Test
    void consomeMaterialReservadoSemDescontarDisponibilidadeDuasVezes() {
        Material material = new Material("Caixa", "un");
        material.registrarEntrada(new BigDecimal("10"), RESPONSAVEL, CADASTRO);
        var reserva = material.reservar(novaEncomenda(), new BigDecimal("3"), RESPONSAVEL, APROVACAO);
        assertEquals(new BigDecimal("7.000"), material.saldoDisponivel());

        material.consumir(reserva, new BigDecimal("3"), RESPONSAVEL, ALTERACAO);
        assertEquals(new BigDecimal("7.000"), material.getQuantidadeFisica());
        assertEquals(new BigDecimal("0.000"), material.getQuantidadeReservada());
        assertEquals(new BigDecimal("7.000"), material.saldoDisponivel());
    }

    @Test
    void estornoPreservaORecebimentoOriginalEZeraSeuValorValido() {
        Pagamento pagamento = new Pagamento(novaEncomenda(), "op-1", new BigDecimal("50"), FormaPagamento.PIX,
                RESPONSAVEL, APROVACAO, new BigDecimal("100"));

        pagamento.estornar(RESPONSAVEL, "Lançamento incorreto", ALTERACAO);
        assertEquals(new BigDecimal("50.00"), pagamento.getValor());
        assertEquals(new BigDecimal("0.00"), pagamento.valorValido());
        assertEquals(ALTERACAO, pagamento.getEstornadoEm());
        assertSame(RESPONSAVEL, pagamento.getEstornadoPor());
        assertEquals("Lançamento incorreto", pagamento.getJustificativaEstorno());
        assertThrows(IllegalStateException.class,
                () -> pagamento.estornar(RESPONSAVEL, "Segunda tentativa", ALTERACAO));
    }

    @Test
    void registraRecusaSemMudarEncomenda() {
        Encomenda encomenda = encomendaAprovada();
        SolicitacaoAlteracao solicitacao = encomenda.solicitarAlteracao(RESPONSAVEL, CanalOrigem.TELEFONE,
                ALTERACAO, null, PRAZO.plusDays(1), null, null, null);

        solicitacao.rejeitar(RESPONSAVEL, "Prazo indisponível", ALTERACAO);

        assertEquals(StatusSolicitacaoAlteracao.REJEITADA, solicitacao.getStatus());
        assertEquals("Prazo indisponível", solicitacao.getJustificativa());
        assertSame(RESPONSAVEL, solicitacao.getAnalisadaPor());
        assertEquals(PRAZO, encomenda.getPrazoPrometido());
        assertThrows(IllegalStateException.class,
                () -> encomenda.aplicarAlteracao(solicitacao, ALTERACAO, BigDecimal.ZERO));
    }

    @Test
    void impedeAplicacaoDePedidoDeAlteracaoDesatualizado() {
        Encomenda encomenda = encomendaAprovada();
        ItemEncomenda item = encomenda.getItens().getFirst();
        SolicitacaoAlteracao primeira = encomenda.solicitarAlteracao(RESPONSAVEL, CanalOrigem.TELEFONE, ALTERACAO,
                null, null, item, 11, null);
        SolicitacaoAlteracao segunda = encomenda.solicitarAlteracao(RESPONSAVEL, CanalOrigem.TELEFONE, ALTERACAO,
                null, null, item, 13, null);
        primeira.autorizar(RESPONSAVEL, "Aceita", ALTERACAO);
        segunda.autorizar(RESPONSAVEL, "Aceita", ALTERACAO);
        encomenda.aplicarAlteracao(primeira, ALTERACAO, BigDecimal.ZERO);

        assertThrows(IllegalStateException.class,
                () -> encomenda.aplicarAlteracao(segunda, ALTERACAO, BigDecimal.ZERO));
        assertEquals(11, item.getQuantidade());
        assertEquals(1, encomenda.getHistorico().size());
    }

    private Encomenda encomendaAprovada() {
        Encomenda encomenda = novaEncomenda();
        encomenda.aprovar(new BigDecimal("100"), BigDecimal.ZERO, DUAS_HORAS, PRAZO,
                INICIO_PLANEJADO, LIMITE_ALTERACAO, RESPONSAVEL, APROVACAO);
        return encomenda;
    }

    private Encomenda novaEncomenda() {
        return new Encomenda("ENC-001", new Cliente("Ana", "ana@example.com"), RESPONSAVEL,
                CanalOrigem.WHATSAPP, PRAZO, CADASTRO, item("Nome da cliente em dourado"));
    }

    private ItemEncomenda item(String personalizacao) {
        return new ItemEncomenda("Caixa", 10, personalizacao, List.of());
    }
}
