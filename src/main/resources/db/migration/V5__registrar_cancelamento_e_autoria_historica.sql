alter table solicitacoes_alteracao
    drop constraint solicitacoes_alteracao_status_check,
    add constraint solicitacoes_alteracao_status_check
        check (status in ('PENDENTE', 'AGUARDANDO_CONCORDANCIA', 'AUTORIZADA',
                          'APLICADA', 'REJEITADA', 'EXPIRADA', 'CANCELADA'));

-- A V4 deixou sem autor alguns eventos de solicitações antigas sem análise.
-- Recupera o autor do registro quando todas as solicitações candidatas apontam para a mesma pessoa.
with autores as (
    select historico.id as historico_id, min(solicitacao.registrada_por_id) as autor_id
    from historicos_encomenda historico
    join solicitacoes_alteracao solicitacao
      on solicitacao.encomenda_id = historico.encomenda_id
     and solicitacao.solicitada_em = historico.ocorrida_em
     and solicitacao.analisada_por_id is null
     and solicitacao.status = 'EXPIRADA'
    where historico.autor_id is null
      and historico.operacao in ('ALTERACAO_SOLICITADA', 'ALTERACAO_EXPIRADA')
      and historico.dados_anteriores like '{"id": %, "itens": %'
      and historico.dados_novos like '{"id": %, "itens": %'
    group by historico.id
    having count(distinct solicitacao.registrada_por_id) = 1
)
update historicos_encomenda historico
set autor_id = autores.autor_id
from autores
where historico.id = autores.historico_id;
