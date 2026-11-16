# Diagrama de classes do sistema de encomendas

O diagrama está organizado em quatro vistas. Cada classe tem uma vista com seus atributos e métodos de domínio. Nas outras vistas, ela pode aparecer somente pelo nome para mostrar seus relacionamentos.

## Cadastro e encomenda

```mermaid
classDiagram
    direction TB

    class Encomenda {
        <<entity>>
        -Long id
        -String codigo
        -Cliente cliente
        -Usuario responsavel
        -CanalOrigem canalOrigem
        -LocalDate dataSolicitada
        -Instant cadastradaEm
        -StatusEncomenda status
        -String motivoReprovacao
        -String motivoCancelamento
        -BigDecimal precoBruto
        -BigDecimal desconto
        -BigDecimal esforcoHoras
        -LocalDate prazoPrometido
        -Instant inicioPlanejadoEm
        -Instant limiteAlteracao
        -Usuario aprovadaPor
        -Instant aprovadaEm
        -Usuario cienciaPor
        -Instant cienciaEm
        -List~ItemEncomenda~ itens
        -int numeroAlteracoes
        -List~HistoricoEncomenda~ historico
        -List~SolicitacaoAlteracao~ solicitacoes
        -List~ImpedimentoProducao~ impedimentos
        -Usuario materiaisConferidosPor
        -Instant materiaisConferidosEm
        -Usuario entregaRegistradaPor
        -Instant entregueEm
        -ModalidadeEntrega modalidadeEntrega
        -long revisao
        +adicionarItem(ItemEncomenda item) void
        +alterarItem(ItemEncomenda item, ItemEncomenda novosDados) void
        +aprovar(BigDecimal precoBruto, BigDecimal desconto, BigDecimal esforcoHoras, LocalDate prazoPrometido, Instant inicioPlanejadoEm, Instant limiteAlteracao, Usuario autor, Instant quando) void
        +reprovar(String justificativa) void
        +solicitarAlteracao(Usuario autor, CanalOrigem canal, Instant quando, BigDecimal precoProposto, LocalDate prazoProposto, ItemEncomenda item, Integer quantidadeProposta, String personalizacaoProposta) SolicitacaoAlteracao
        +aplicarAlteracao(SolicitacaoAlteracao solicitacao, Instant quando, BigDecimal totalPago) void
        +registrarImpedimento(String descricao, Usuario responsavel, Instant quando) ImpedimentoProducao
        +conferirMateriais(Usuario funcionario, Instant quando) void
        +confirmarCiencia(int alteracoesConsultadas, Usuario funcionario, Instant quando) void
        +iniciarProducao(Instant quando) void
        +marcarPronta() void
        +registrarEntrega(Usuario autor, Instant quando, ModalidadeEntrega modalidade, BigDecimal saldoEmAberto) void
        +cancelar(String justificativa) void
        +precoTotal() BigDecimal
    }

    class Cliente {
        <<entity>>
        -Long id
        -String nome
        -String contato
        -Usuario usuario
        +atualizarContato(String novoContato) void
        +vincularConta(Usuario conta) void
    }

    class Usuario {
        <<entity>>
        -Long id
        -String nome
        -String email
        -String senhaCodificada
        -PerfilUsuario perfil
        -boolean ativo
        -Set~FuncaoFuncionario~ funcoes
        +atualizarFuncoes(Set~FuncaoFuncionario~ novasFuncoes) void
        +alterarSenhaCodificada(String novaSenhaCodificada) void
        +ativar() void
        +desativar() void
    }

    class ItemEncomenda {
        <<entity>>
        -Long id
        -Encomenda encomenda
        -int ordem
        -String tipoProduto
        -int quantidade
        -String personalizacao
        -List~MaterialNecessario~ materiais
        ~alterar(ItemEncomenda novosDados) void
        ~aplicarAlteracao(Integer novaQuantidade, String novaPersonalizacao) void
    }

    class PerfilUsuario {
        <<enumeration>>
        ADMINISTRADOR
        FUNCIONARIO
        CLIENTE
    }

    class FuncaoFuncionario {
        <<enumeration>>
        ATENDIMENTO
        PRODUCAO
        FINANCEIRO
        ENTREGA
    }

    class CanalOrigem {
        <<enumeration>>
        WHATSAPP
        INSTAGRAM
        PRESENCIAL
        TELEFONE
        PORTAL
    }

    class StatusEncomenda {
        <<enumeration>>
        PENDENTE
        APROVADA
        REPROVADA
        EM_PRODUCAO
        PRONTA
        ENTREGUE
        CANCELADA
    }

    Encomenda "1" *-- "1..*" ItemEncomenda : itens do pedido
    Encomenda "0..*" --> "1" Cliente : pertence a
    Encomenda "0..*" --> "1" Usuario : responsável
    Encomenda "0..*" --> "0..1" Usuario : aprovação
    Cliente "0..1" --> "0..1" Usuario : conta do portal

    Encomenda ..> StatusEncomenda
    Encomenda ..> CanalOrigem
    Usuario ..> PerfilUsuario
    Usuario ..> FuncaoFuncionario

```

## Alterações e histórico

```mermaid
classDiagram
    direction TB

    class SolicitacaoAlteracao {
        <<entity>>
        -Long id
        -Encomenda encomenda
        -int alteracoesNaSolicitacao
        -Usuario registradaPor
        -CanalOrigem canal
        -Instant solicitadaEm
        -BigDecimal precoProposto
        -LocalDate prazoProposto
        -ItemEncomenda item
        -Integer quantidadeProposta
        -String personalizacaoProposta
        -long revisao
        -StatusSolicitacaoAlteracao status
        -Usuario analisadaPor
        -Instant analisadaEm
        -String justificativa
        -Instant concordanciaEm
        +autorizar(Usuario analista, String motivo, Instant quando) void
        +rejeitar(Usuario analista, String motivo, Instant quando) void
        +confirmarConcordancia(Instant quando) void
        +expirar(Instant quando) void
        +estaEmAberto() boolean
        ~marcarAplicada() void
    }

    class StatusSolicitacaoAlteracao {
        <<enumeration>>
        PENDENTE
        AGUARDANDO_CONCORDANCIA
        AUTORIZADA
        APLICADA
        REJEITADA
        EXPIRADA
    }

    class HistoricoEncomenda {
        <<entity>>
        -Long id
        -Encomenda encomenda
        -Usuario autor
        -TipoEventoEncomenda operacao
        -Instant ocorridaEm
        -String dadosAnteriores
        -String dadosNovos
        -String justificativa
        -boolean visivelAoCliente
    }

    class TipoEventoEncomenda {
        <<enumeration>>
        CADASTRADA
        STATUS_ALTERADO
        ALTERACAO_SOLICITADA
        ALTERACAO_APROVADA
        ALTERACAO_REJEITADA
        ALTERACAO_EXPIRADA
        IMPEDIMENTO_REGISTRADO
        IMPEDIMENTO_RESOLVIDO
        CIENCIA_REGISTRADA
        PAGAMENTO_REGISTRADO
        PAGAMENTO_ESTORNADO
        CANCELADA
        ENTREGUE
    }

    Encomenda "1" *-- "0..*" SolicitacaoAlteracao : solicitações

    SolicitacaoAlteracao "0..*" --> "1" Usuario : registro
    SolicitacaoAlteracao "0..*" --> "0..1" Usuario : análise
    Encomenda "1" *-- "0..*" HistoricoEncomenda : eventos
    SolicitacaoAlteracao "0..*" --> "0..1" ItemEncomenda : item da mudança
    HistoricoEncomenda "0..*" --> "0..1" Usuario : autor
    SolicitacaoAlteracao ..> StatusSolicitacaoAlteracao
    SolicitacaoAlteracao ..> CanalOrigem
    HistoricoEncomenda ..> TipoEventoEncomenda
```

## Produção e entrega

```mermaid
classDiagram
    direction TB

    class ImpedimentoProducao {
        <<entity>>
        -Long id
        -Encomenda encomenda
        -String descricao
        -Usuario responsavel
        -Instant registradoEm
        -Instant resolvidoEm
        +estaAberto() boolean
        +resolver(Instant quando) void
    }

    class ModalidadeEntrega {
        <<enumeration>>
        ENTREGA
        RETIRADA
    }


    Encomenda "1" *-- "0..*" ImpedimentoProducao : impedimentos
    ImpedimentoProducao "0..*" --> "1" Usuario : responsável
    Encomenda "0..*" --> "0..1" Usuario : conferência de materiais
    Encomenda "0..*" --> "0..1" Usuario : registro da entrega
    Encomenda "0..*" --> "0..1" Usuario : ciência da produção
    Encomenda ..> ModalidadeEntrega

```

## Financeiro e estoque

```mermaid
classDiagram
    direction TB

    class Pagamento {
        <<entity>>
        -Long id
        -Encomenda encomenda
        -String chaveOperacao
        -BigDecimal valor
        -FormaPagamento forma
        -Usuario responsavel
        -Instant recebidoEm
        -Usuario estornadoPor
        -Instant estornadoEm
        -String justificativaEstorno
        -long revisao
        +estornar(Usuario autor, String justificativa, Instant quando) void
        +valorValido() BigDecimal
    }

    class CustoEncomenda {
        <<entity>>
        -Long id
        -Encomenda encomenda
        -CategoriaCusto categoria
        -BigDecimal valor
        -String descricao
        -Usuario responsavel
        -Instant registradoEm
    }

    class FormaPagamento {
        <<enumeration>>
        PIX
        DINHEIRO
        CARTAO
        TRANSFERENCIA
        OUTRA
    }

    class CategoriaCusto {
        <<enumeration>>
        MATERIAL
        MAO_DE_OBRA
        ENTREGA
        RETRABALHO
        PERDA
    }

    class Material {
        <<entity>>
        -Long id
        -String nome
        -String unidadeMedida
        -BigDecimal quantidadeFisica
        -BigDecimal quantidadeReservada
        -long revisao
        +saldoDisponivel() BigDecimal
        +registrarEntrada(BigDecimal quantidade, Usuario autor, Instant quando) MovimentacaoEstoque
        +registrarSaida(BigDecimal quantidade, Usuario autor, Instant quando) MovimentacaoEstoque
        +registrarPerda(BigDecimal quantidade, Usuario autor, Instant quando, String motivo) MovimentacaoEstoque
        +reservar(Encomenda encomenda, BigDecimal quantidade, Usuario autor, Instant quando) ReservaMaterial
        +consumir(ReservaMaterial reserva, BigDecimal quantidade, Usuario autor, Instant quando) MovimentacaoEstoque
        +liberar(ReservaMaterial reserva) void
    }

    class MaterialNecessario {
        <<valueObject>>
        -Material material
        -BigDecimal quantidade
    }

    class ReservaMaterial {
        <<entity>>
        -Long id
        -Material material
        -Encomenda encomenda
        -BigDecimal quantidade
        -BigDecimal saldoReservado
        -Usuario autor
        -Instant criadaEm
        ~retirarSaldo(BigDecimal retirada) void
    }

    class MovimentacaoEstoque {
        <<entity>>
        -Long id
        -Material material
        -ReservaMaterial reserva
        -TipoMovimentacaoEstoque tipo
        -BigDecimal quantidade
        -Usuario autor
        -Instant ocorridaEm
        -String motivo
    }

    class TipoMovimentacaoEstoque {
        <<enumeration>>
        ENTRADA
        SAIDA
        CONSUMO
        PERDA
    }

    Pagamento "0..*" --> "1" Encomenda : recebimento
    Pagamento "0..*" --> "1" Usuario : responsável
    Pagamento "0..*" --> "0..1" Usuario : registrou o estorno

    CustoEncomenda "0..*" --> "1" Encomenda : custo registrado
    CustoEncomenda "0..*" --> "1" Usuario : responsável
    CustoEncomenda ..> CategoriaCusto
    Pagamento ..> FormaPagamento
    ItemEncomenda "1" *-- "0..*" MaterialNecessario : materiais do item

    MaterialNecessario "0..*" --> "1" Material : material especificado
    ReservaMaterial "0..*" --> "1" Material : material reservado
    ReservaMaterial "0..*" --> "1" Encomenda : encomenda reservada
    ReservaMaterial "0..*" --> "1" Usuario : autor
    MovimentacaoEstoque "0..*" --> "1" Material : material movimentado
    MovimentacaoEstoque "0..*" --> "0..1" ReservaMaterial : reserva consumida
    MovimentacaoEstoque "0..*" --> "1" Usuario : autor
    MovimentacaoEstoque ..> TipoMovimentacaoEstoque
```

## Decisões de modelagem

- A `Encomenda` guarda os itens, o preço e o prazo vigentes. A solicitação guarda somente o preço, o prazo ou a mudança de quantidade e personalização de um item existente.
- Aplicar uma solicitação autorizada atualiza a encomenda e registra os dados anteriores e novos no histórico. O contador de alterações identifica solicitações e confirmações desatualizadas.
- A encomenda guarda quem confirmou a ciência e quando, além da modalidade, do funcionário e da data da entrega.
- O cliente pode existir sem conta no portal. Os funcionários podem acumular funções operacionais. O histórico admite eventos automáticos sem autor humano.
- Cada pagamento admite um estorno integral, com responsável, data e justificativa no próprio pagamento. A disponibilidade de material deriva dos saldos físico e reservado.
- A lista de compras, os indicadores e o resultado estimado são consultas sobre os dados persistidos. Arquivos de arte ainda dependem de validação com o proprietário.
