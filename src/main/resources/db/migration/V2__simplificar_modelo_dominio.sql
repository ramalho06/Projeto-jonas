-- A mesma tabela de versões passa a guardar rascunhos e propostas sem número.
alter table versoes_encomenda
    alter column numero drop not null,
    alter column preco_bruto drop not null,
    alter column desconto drop not null,
    alter column esforco_horas drop not null,
    alter column prazo_prometido drop not null,
    alter column inicio_planejado_em drop not null,
    alter column limite_alteracao drop not null,
    alter column aprovada_por_id drop not null,
    alter column aprovada_em drop not null,
    add column ciencia_por_id bigint references usuarios (id),
    add column ciencia_em timestamp(6) with time zone;

insert into versoes_encomenda (encomenda_id)
select id from encomendas;

alter table itens_encomenda
    add column versao_id bigint references versoes_encomenda (id),
    add column ordem integer;

with itens_ordenados as (
    select id, encomenda_id, row_number() over (partition by encomenda_id order by id) - 1 as ordem
    from itens_encomenda
)
update itens_encomenda item
set versao_id = rascunho.id, ordem = origem.ordem
from itens_ordenados origem
join versoes_encomenda rascunho on rascunho.encomenda_id = origem.encomenda_id and rascunho.numero is null
where item.id = origem.id;

insert into itens_encomenda (encomenda_id, versao_id, ordem, tipo_produto, quantidade, personalizacao)
select versao.encomenda_id, item.versao_id, item.ordem, item.tipo_produto, item.quantidade, item.personalizacao
from itens_versao_encomenda item
join versoes_encomenda versao on versao.id = item.versao_id;

insert into materiais_item_encomenda (item_id, material_id, quantidade)
select destino.id, material.material_id, material.quantidade
from materiais_item_versao material
join itens_versao_encomenda origem on origem.id = material.item_id
join itens_encomenda destino on destino.versao_id = origem.versao_id and destino.ordem = origem.ordem;

alter table solicitacoes_alteracao add column versao_proposta_id bigint;

-- Nas alterações já aplicadas, a proposta é a versão aprovada seguinte à base.
update solicitacoes_alteracao solicitacao
set versao_proposta_id = aprovada.id
from versoes_encomenda base
join versoes_encomenda aprovada on aprovada.encomenda_id = base.encomenda_id
                               and aprovada.numero = base.numero + 1
where base.id = solicitacao.versao_base_id and solicitacao.status = 'APLICADA';

-- A coluna temporária associa cada proposta copiada à sua solicitação original.
alter table versoes_encomenda add column solicitacao_origem_id bigint;

insert into versoes_encomenda (encomenda_id, preco_bruto, desconto, esforco_horas,
                              prazo_prometido, inicio_planejado_em, limite_alteracao, solicitacao_origem_id)
select encomenda_id, preco_bruto, desconto, esforco_horas,
       prazo_prometido, inicio_planejado_em, limite_alteracao, id
from solicitacoes_alteracao
where versao_proposta_id is null;

update solicitacoes_alteracao solicitacao
set versao_proposta_id = proposta.id
from versoes_encomenda proposta
where proposta.solicitacao_origem_id = solicitacao.id;

insert into itens_encomenda (encomenda_id, versao_id, ordem, tipo_produto, quantidade, personalizacao)
select solicitacao.encomenda_id, solicitacao.versao_proposta_id, item.ordem,
       item.tipo_produto, item.quantidade, item.personalizacao
from itens_propostos item
join solicitacoes_alteracao solicitacao on solicitacao.id = item.solicitacao_id
join versoes_encomenda proposta on proposta.solicitacao_origem_id = solicitacao.id;

insert into materiais_item_encomenda (item_id, material_id, quantidade)
select destino.id, material.material_id, material.quantidade
from materiais_item_proposto material
join itens_propostos origem on origem.id = material.item_id
join solicitacoes_alteracao solicitacao on solicitacao.id = origem.solicitacao_id
join versoes_encomenda proposta on proposta.solicitacao_origem_id = solicitacao.id
join itens_encomenda destino on destino.versao_id = solicitacao.versao_proposta_id and destino.ordem = origem.ordem;

alter table versoes_encomenda drop column solicitacao_origem_id;

alter table solicitacoes_alteracao
    alter column versao_proposta_id set not null,
    add foreign key (versao_proposta_id) references versoes_encomenda (id),
    add unique (versao_proposta_id),
    drop column preco_bruto,
    drop column desconto,
    drop column esforco_horas,
    drop column prazo_prometido,
    drop column inicio_planejado_em,
    drop column limite_alteracao;

alter table itens_encomenda
    alter column versao_id set not null,
    alter column ordem set not null,
    add check (ordem >= 0),
    drop column encomenda_id;

with primeira_ciencia as (
    select distinct on (versao_id) id, versao_id, funcionario_id, confirmada_em
    from ciencias_producao
    order by versao_id, confirmada_em, id
)
update versoes_encomenda versao
set ciencia_por_id = ciencia.funcionario_id, ciencia_em = ciencia.confirmada_em
from primeira_ciencia ciencia
where versao.id = ciencia.versao_id;

-- Confirmações posteriores continuam consultáveis no histórico.
insert into historicos_encomenda (visivel_ao_cliente, autor_id, encomenda_id, ocorrida_em, dados_novos, operacao)
select false, ciencia.funcionario_id, versao.encomenda_id, ciencia.confirmada_em,
       'Versão ' || versao.numero, 'CIENCIA_REGISTRADA'
from ciencias_producao ciencia
join versoes_encomenda versao on versao.id = ciencia.versao_id
where exists (
    select 1 from ciencias_producao anterior
    where anterior.versao_id = ciencia.versao_id
      and (anterior.confirmada_em, anterior.id) < (ciencia.confirmada_em, ciencia.id)
);

alter table encomendas
    add column entrega_registrada_por_id bigint references usuarios (id),
    add column entregue_em timestamp(6) with time zone,
    add column modalidade_entrega varchar(255) check (modalidade_entrega in ('ENTREGA', 'RETIRADA'));

update encomendas encomenda
set entrega_registrada_por_id = entrega.registrada_por_id,
    entregue_em = entrega.registrada_em, modalidade_entrega = entrega.modalidade
from entregas entrega
where encomenda.id = entrega.encomenda_id;

alter table pagamentos
    add column estornado_por_id bigint references usuarios (id),
    add column estornado_em timestamp(6) with time zone,
    add column justificativa_estorno text,
    add column revisao bigint not null default 0;

update pagamentos pagamento
set estornado_por_id = estorno.autor_id, estornado_em = estorno.registrado_em,
    justificativa_estorno = estorno.justificativa
from estornos_pagamento estorno
where pagamento.id = estorno.pagamento_id;

drop table materiais_item_proposto;
drop table materiais_item_versao;
drop table itens_propostos;
drop table itens_versao_encomenda;
drop table ciencias_producao;
drop table entregas;
drop table estornos_pagamento;
