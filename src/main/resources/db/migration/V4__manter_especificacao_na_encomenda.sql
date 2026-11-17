alter table encomendas
    add column preco_bruto numeric(15,2),
    add column desconto numeric(15,2),
    add column esforco_horas numeric(8,2),
    add column prazo_prometido date,
    add column inicio_planejado_em timestamp(6) with time zone,
    add column limite_alteracao timestamp(6) with time zone,
    add column aprovada_por_id bigint references usuarios (id),
    add column aprovada_em timestamp(6) with time zone,
    add column ciencia_por_id bigint references usuarios (id),
    add column ciencia_em timestamp(6) with time zone,
    add column numero_alteracoes integer not null default 0;

-- Cada encomenda mantém os itens em uso. Pedidos sem aprovação mantêm o primeiro rascunho.
create temporary table versoes_vigentes on commit drop as
select distinct on (encomenda.id) encomenda.id as encomenda_id, versao.id as versao_id
from encomendas encomenda
join versoes_encomenda versao on versao.encomenda_id = encomenda.id
where versao.numero = encomenda.numero_versao_vigente
   or (encomenda.numero_versao_vigente = 0 and versao.numero is null)
order by encomenda.id, versao.id;

do $$
begin
    if exists (
        select 1 from encomendas encomenda
        where not exists (
            select 1 from versoes_vigentes vigente where vigente.encomenda_id = encomenda.id
        )
    ) then
        raise exception 'Existem encomendas sem especificação vigente ou rascunho.';
    end if;
end $$;

update encomendas encomenda
set preco_bruto = versao.preco_bruto,
    desconto = versao.desconto,
    esforco_horas = versao.esforco_horas,
    prazo_prometido = versao.prazo_prometido,
    inicio_planejado_em = versao.inicio_planejado_em,
    limite_alteracao = versao.limite_alteracao,
    aprovada_por_id = versao.aprovada_por_id,
    aprovada_em = versao.aprovada_em,
    ciencia_por_id = versao.ciencia_por_id,
    ciencia_em = versao.ciencia_em,
    numero_alteracoes = greatest(encomenda.numero_versao_vigente - 1, 0)
from versoes_vigentes vigente
join versoes_encomenda versao on versao.id = vigente.versao_id
where encomenda.id = vigente.encomenda_id;

-- Preserva as especificações antigas como texto de histórico antes de retirar as tabelas de versões.
create temporary table especificacoes_anteriores on commit drop as
select versao.id,
       (to_jsonb(versao) || jsonb_build_object('itens', coalesce((
           select jsonb_agg(to_jsonb(item) || jsonb_build_object('materiais', coalesce((
               select jsonb_agg(jsonb_build_object('material_id', material.material_id, 'quantidade', material.quantidade)
                                order by material.material_id, material.quantidade)
               from materiais_item_encomenda material
               where material.item_id = item.id
           ), '[]'::jsonb)) order by item.ordem, item.id)
           from itens_encomenda item
           where item.versao_id = versao.id
       ), '[]'::jsonb)))::text as dados
from versoes_encomenda versao;

insert into historicos_encomenda
    (encomenda_id, autor_id, operacao, ocorrida_em, dados_anteriores, dados_novos, justificativa, visivel_ao_cliente)
select solicitacao.encomenda_id, solicitacao.analisada_por_id,
       case solicitacao.status
           when 'APLICADA' then 'ALTERACAO_APROVADA'
           when 'REJEITADA' then 'ALTERACAO_REJEITADA'
           when 'EXPIRADA' then 'ALTERACAO_EXPIRADA'
           else 'ALTERACAO_SOLICITADA'
       end,
       coalesce(versao.aprovada_em, solicitacao.analisada_em, solicitacao.solicitada_em),
       base.dados, proposta.dados, solicitacao.justificativa, false
from solicitacoes_alteracao solicitacao
join versoes_encomenda versao on versao.id = solicitacao.versao_proposta_id
join especificacoes_anteriores base on base.id = solicitacao.versao_base_id
join especificacoes_anteriores proposta on proposta.id = solicitacao.versao_proposta_id;

-- Solicitações antigas podem substituir vários itens e precisam ser reavaliadas no novo formato.
insert into historicos_encomenda
    (encomenda_id, operacao, ocorrida_em, justificativa, visivel_ao_cliente)
select encomenda_id, 'ALTERACAO_EXPIRADA', greatest(now(), solicitada_em),
       'Solicitação antiga arquivada. Cadastre novamente as mudanças que ainda precisam ser avaliadas.', false
from solicitacoes_alteracao
where status in ('PENDENTE', 'AGUARDANDO_CONCORDANCIA', 'AUTORIZADA');

alter table solicitacoes_alteracao
    add column alteracoes_na_solicitacao integer not null default 0,
    add column preco_proposto numeric(15,2),
    add column prazo_proposto date,
    add column item_id bigint references itens_encomenda (id),
    add column quantidade_proposta integer check (quantidade_proposta > 0),
    add column personalizacao_proposta text,
    add column revisao bigint not null default 0;

update solicitacoes_alteracao solicitacao
set alteracoes_na_solicitacao = greatest(base.numero - 1, 0),
    preco_proposto = proposta.preco_bruto - proposta.desconto,
    prazo_proposto = proposta.prazo_prometido,
    status = case when solicitacao.status in ('PENDENTE', 'AGUARDANDO_CONCORDANCIA', 'AUTORIZADA')
                  then 'EXPIRADA' else solicitacao.status end
from versoes_encomenda base, versoes_encomenda proposta
where base.id = solicitacao.versao_base_id and proposta.id = solicitacao.versao_proposta_id;

alter table solicitacoes_alteracao
    drop column versao_base_id,
    drop column versao_proposta_id;

alter table itens_encomenda add column encomenda_id bigint references encomendas (id);

update itens_encomenda item
set encomenda_id = vigente.encomenda_id
from versoes_vigentes vigente
where item.versao_id = vigente.versao_id;

delete from materiais_item_encomenda material
using itens_encomenda item
where material.item_id = item.id and item.encomenda_id is null;

delete from itens_encomenda where encomenda_id is null;

alter table itens_encomenda
    alter column encomenda_id set not null,
    drop column versao_id;

alter table encomendas drop column numero_versao_vigente;
drop table versoes_encomenda;
