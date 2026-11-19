package com.example.jonas_project.encomenda.entity;

import com.example.jonas_project.encomenda.enums.CanalOrigem;
import com.example.jonas_project.encomenda.enums.ModalidadeEntrega;
import com.example.jonas_project.encomenda.enums.StatusEncomenda;
import com.example.jonas_project.encomenda.enums.StatusSolicitacaoAlteracao;
import com.example.jonas_project.encomenda.enums.TipoEventoEncomenda;
import com.example.jonas_project.usuario.entity.PerfilUsuario;
import com.example.jonas_project.usuario.entity.Usuario;
import com.example.jonas_project.cliente.entity.Cliente;
import com.example.jonas_project.material.entity.Material;
import com.example.jonas_project.pagamento.enums.FormaPagamento;
import com.example.jonas_project.pagamento.entity.Pagamento;
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

        ItemEncomenda caneca = new ItemEncomenda("Caneca", 5, "Logotipo azul", List.of());
        encomenda.adicionarItem(caneca);

        assertEquals(2, encomenda.getItens().size());
        assertEquals("Caneca", encomenda.getItens().getLast().getTipoProduto());
        assertSame(caneca, encomenda.getItens().getLast());
        encomenda.alterarItem(caneca, new ItemEncomenda("Caneca", 6, "Logotipo azul", List.of()));
        assertEquals(6, caneca.getQuantidade());
        assertThrows(IllegalStateException.class, () -> novaEncomenda().adicionarItem(caneca));
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
        encomenda.conferirMateriais(RESPONSAVEL, APROVACAO);
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
        assertTrue(registro.getDadosAnteriores().contains("Aprovação: " + RESPONSAVEL.getNome() + " em " + APROVACAO));
        assertTrue(registro.getDadosAnteriores().contains("Ciência: " + RESPONSAVEL.getNome() + " em " + APROVACAO));
        assertTrue(registro.getDadosAnteriores().contains("Conferência de materiais: " + RESPONSAVEL.getNome() + " em " + APROVACAO));
        assertTrue(registro.getDadosNovos().contains("120.00"));
        assertTrue(registro.getDadosNovos().contains("azul"));
        assertTrue(registro.getDadosNovos().contains("Ciência: Pendente"));

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
        HistoricoEncomenda registro = encomenda.getHistorico().getFirst();
        assertEquals(TipoEventoEncomenda.ALTERACAO_REJEITADA, registro.getOperacao());
        assertEquals("Prazo indisponível", registro.getJustificativa());
        assertSame(RESPONSAVEL, registro.getAutor());
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

        encomenda.conferirMateriais(RESPONSAVEL, ALTERACAO);
        encomenda.confirmarCiencia(1, RESPONSAVEL, ALTERACAO);
        encomenda.iniciarProducao(ALTERACAO);

        assertEquals(StatusEncomenda.EM_PRODUCAO, encomenda.getStatus());
        assertEquals(StatusSolicitacaoAlteracao.EXPIRADA, segunda.getStatus());
        assertEquals(TipoEventoEncomenda.ALTERACAO_EXPIRADA, encomenda.getHistorico().getLast().getOperacao());
    }

    @Test
    void prazoVigenteBloqueiaProducaoAteAExpiracao() {
        Encomenda encomenda = encomendaAprovada();
        SolicitacaoAlteracao solicitacao = encomenda.solicitarAlteracao(RESPONSAVEL, CanalOrigem.TELEFONE,
                ALTERACAO, null, PRAZO.plusDays(1), null, null, null);
        encomenda.conferirMateriais(RESPONSAVEL, ALTERACAO);
        encomenda.confirmarCiencia(0, RESPONSAVEL, ALTERACAO);

        assertThrows(IllegalStateException.class, () -> encomenda.iniciarProducao(LIMITE_ALTERACAO));
        assertTrue(encomenda.getHistorico().isEmpty());
        encomenda.iniciarProducao(LIMITE_ALTERACAO.plusSeconds(1));
        assertEquals(StatusSolicitacaoAlteracao.EXPIRADA, solicitacao.getStatus());
        assertEquals(TipoEventoEncomenda.ALTERACAO_EXPIRADA, encomenda.getHistorico().getFirst().getOperacao());
    }

    @Test
    void cancelamentoRegistraAutorDataEEncerraPendencias() {
        Encomenda encomenda = encomendaAprovada();
        SolicitacaoAlteracao solicitacao = encomenda.solicitarAlteracao(RESPONSAVEL, CanalOrigem.TELEFONE,
                ALTERACAO, null, PRAZO.plusDays(1), null, null, null);
        ImpedimentoProducao impedimento = encomenda.registrarImpedimento("Falta material", RESPONSAVEL, ALTERACAO);

        encomenda.cancelar(RESPONSAVEL, "Desistência do cliente", ALTERACAO);

        assertEquals(StatusEncomenda.CANCELADA, encomenda.getStatus());
        assertEquals(StatusSolicitacaoAlteracao.CANCELADA, solicitacao.getStatus());
        assertTrue(!impedimento.estaAberto());
        assertNull(impedimento.getResolvidoEm());
        HistoricoEncomenda registro = encomenda.getHistorico().getFirst();
        assertEquals(TipoEventoEncomenda.CANCELADA, registro.getOperacao());
        assertSame(RESPONSAVEL, registro.getAutor());
        assertEquals(ALTERACAO, registro.getOcorridaEm());
    }

    @Test
    void casasDecimaisExcedentesSaoErrosDeValidacao() {
        Encomenda encomenda = novaEncomenda();
        IllegalArgumentException preco = assertThrows(IllegalArgumentException.class, () ->
                encomenda.aprovar(new BigDecimal("100.005"), BigDecimal.ZERO, DUAS_HORAS, PRAZO,
                        INICIO_PLANEJADO, LIMITE_ALTERACAO, RESPONSAVEL, APROVACAO));
        assertTrue(preco.getMessage().contains("preço bruto"));

        Material fita = new Material("Fita", "m");
        IllegalArgumentException estoque = assertThrows(IllegalArgumentException.class,
                () -> fita.registrarEntrada(new BigDecimal("0.0005"), RESPONSAVEL, ALTERACAO));
        assertTrue(estoque.getMessage().contains("quantidade de entrada"));
        fita.registrarEntrada(new BigDecimal("1.5000"), RESPONSAVEL, ALTERACAO);
        assertEquals(new BigDecimal("1.500"), fita.getQuantidadeFisica());
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
