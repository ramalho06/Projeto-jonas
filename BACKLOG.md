# Backlog do sistema de encomendas

Plano individual de 14 dias com prioridade para o backend Spring Boot. Usa os RFs e RNFs originais como referência, com os ajustes de escopo abaixo. As datas são metas para o backend e sua verificação. A implementação das telas fica no card 19, com data a definir após o recebimento do Figma.

## Ajustes de escopo

- O RF04 passa a cobrir aprovação com prazo prometido, início planejado e esforço estimado. O RN01 de capacidade diária sai do escopo. A equipe avalia a agenda manualmente.
- O RF09 de notificações sai do escopo. A consulta dos acontecimentos fica no histórico do pedido, conforme a visibilidade de cada perfil.
- O RF11 mantém os recebimentos, inclusive parciais, e o saldo em aberto. O cadastro de parcelas futuras e seus vencimentos sai do escopo.

- A encomenda guarda a especificação vigente. Cada solicitação propõe um novo preço, prazo ou alteração de quantidade e personalização de um item. O antes e o depois ficam no histórico.

## Como usar o quadro

- Crie um card por seção e mantenha as subtasks como checklist. Use as colunas Pendente, Em andamento, Em verificação e Concluído.
- Um card de backend está pronto quando passa no critério de aceite e funciona pela API. Documente as requisições, as respostas e os erros de negócio para integrar as telas depois.
- Comece cada fluxo pela regra que pode invalidar a operação. Teste com PostgreSQL onde a garantia depende de restrição, transação ou concorrência.

## Cronograma

| Janela | Foco | Marco verificável |
| --- | --- | --- |
| Dias 1–4 | Base, acesso, pedidos e aprovação | Criar, consultar e aprovar um pedido com planejamento no PostgreSQL. |
| Dias 5–9 | Alterações, produção, pagamentos e entrega | Alterar um pedido, confirmar a especificação, produzir, quitar e entregar. |
| Dias 10–12 | Portal, materiais, compras, indicadores e custos | Exercitar todos os módulos pela API, do pedido ao resultado estimado. |
| Dias 13–14 | Verificação, documentação e correções | Repetir a demonstração e os testes a partir da configuração descrita no README. |

## 01 Preparar aplicação e banco

**Dia 1 · RNF08**

- [ ] Rodar o Maven Wrapper e a suíte com Java 21.
- [ ] Configurar PostgreSQL por variáveis de ambiente e executar as migrações com Flyway em um banco vazio.
- [ ] Definir como rodar testes de integração com PostgreSQL e como inicializar os dados de demonstração.
- [ ] Documentar a execução local e definir o formato das respostas de erro da API.

**Aceite.** O README permite clonar, configurar, migrar o banco e iniciar a aplicação.

## 02 Definir regras e decisões de negócio

**Dia 1 · RN02–RN08**

- [ ] Registrar as decisões validadas e as hipóteses ainda pendentes sobre início da produção, prazo para solicitação e expiração, ciência da especificação, sinal, entrega e exceções.
- [ ] Confirmar como serão registrados o esforço em horas, a concordância do cliente, as correções de pagamento e as perdas no cancelamento.
- [ ] Combinar a matriz de permissões e quais dados o cliente pode ver. Confirmar se fotos ou artes são necessárias à fabricação.
- [ ] Combinar os campos e estados compartilhados de pedido, solicitação, pagamento, material e reserva de estoque, além do fuso para interpretar prazos operacionais.

**Aceite.** As regras registradas descrevem as transições, quem pode executá-las e o que acontece quando uma operação é bloqueada. Cada decisão pendente tem uma hipótese registrada para orientar a implementação.

## 03 Autenticar e autorizar usuários

**Dia 2 · RF01 e RNF01 · Depende de 01 e 02**

- [ ] Persistir usuários ativos e inativos, credenciais com hash e perfis de administrador, funcionário e cliente. Permitir que um funcionário acumule funções.
- [ ] Implementar login, logout e administração de contas e permissões.
- [ ] Aplicar autorização no servidor por função e, nas operações de cliente, por vínculo com o pedido. Reaproveitar a verificação nos cards seguintes.
- [ ] Testar conta desativada, função sem permissão e acesso a pedido de outro cliente depois da integração com o card 04.

**Aceite.** Uma conta desativada não consegue acessar o sistema. Um cliente não consegue acessar pedido de outro cliente pelo código, e ações de gestão exigem a função apropriada.

## 04 Cadastrar clientes e pedidos e consultar a ficha

**Dia 3 · RF02 e RF03 · Depende de 03**

- [ ] Migrar e persistir cliente, pedido e itens. Incluir canal, responsável, personalização, quantidade, materiais necessários e dados de prazo e valores conforme a etapa do pedido.
- [ ] Gerar código único, começar como pendente e validar cliente, itens, quantidade positiva e personalização.
- [ ] Expor na API o cadastro, a ficha e a consulta com filtros por código, cliente, status, prazo e responsável.
- [ ] Destacar pedidos ativos com prazo prometido vencido e exibir impedimentos na ficha e na lista quando o card 06 estiver integrado.

**Aceite.** O atendimento cadastra um pedido com itens, encontra a ficha pelo código e recebe erro de campo para um cadastro inválido. Atendimento e produção consultam a mesma ficha atualizada conforme suas permissões.

## 05 Planejar e aprovar pedidos

**Dia 4 · RF04 revisado · Depende de 02 e 04**

- [ ] Registrar o prazo prometido, o início planejado, o esforço estimado e o limite para alterações.
- [ ] Exigir preço e planejamento válido para aprovar. O limite de alteração pode ir até o início planejado da produção.
- [ ] Expor a aprovação na API e testar as validações e o registro da especificação aprovada.

**Aceite.** Um pedido com preço e planejamento válidos é aprovado. Um limite de alteração posterior ao início planejado impede a aprovação.

## 06 Controlar status e impedimentos da produção

**Dia 5 · RF05 e RN05 · Depende de 04 e 05 · Integrar com 07–09**

- [ ] Implementar as transições permitidas e seus responsáveis. Integrar reprovação, cancelamento e entrega às operações correspondentes.
- [ ] Registrar e resolver impedimentos, com motivo e responsável. Exibir o bloqueio na ficha.
- [ ] Registrar a conferência manual de materiais e, ao iniciar produção, verificar status aprovado, impedimentos, solicitações em aberto e ciência da especificação vigente.
- [ ] Expor as ações de produção na API. Testar transição inválida e início com impedimento ou sem conferência.

**Aceite.** Um pedido impedido exibe o motivo e fica bloqueado para início da produção. Depois de resolver o impedimento, a produção só começa com as demais verificações satisfeitas.

## 07 Solicitar alterações e controlar o prazo

**Dias 5–6 · RF07 e RN02–RN04 · Depende de 03–05**

- [ ] Persistir a solicitação separadamente da especificação aprovada, com autor, canal, dados propostos e situação.
- [ ] Permitir que o atendimento registre a solicitação em nome do cliente. Reaproveitar essa operação no portal com verificação de titularidade.
- [ ] Aplicar a regra de prazo e expiração decidida no card 02. Uma solicitação aberta bloqueia o início da produção enquanto estiver válida.
- [ ] Expor a criação e a consulta de solicitações na API. Impedir alterações diretas de campos protegidos de pedidos aprovados.

**Aceite.** Uma solicitação válida fica pendente e preserva a especificação vigente. Uma tentativa fora do prazo retorna o motivo do bloqueio. Uma solicitação expirada preserva os dados vigentes e tem sua expiração registrada no histórico.

## 08 Aprovar alterações e registrar histórico

**Dias 6–7 · RF06, RF08, RN04, RN06, RN07, RNF02 e RNF03 · Depende de 05 e 07 · Integrar saldo com 10**

- [ ] Implementar análise de impacto em esforço, materiais, preço e prazo, com decisão justificada. Registrar concordância do cliente antes de aplicar novo preço ou prazo.
- [ ] Validar se a solicitação está atualizada e conferir o total pago na aplicação. Se o novo preço ficar abaixo do valor recebido, exigir conciliação conforme a regra validada.
- [ ] Atualizar a encomenda e gravar o antes e o depois de forma atômica. Detectar solicitações abertas antes de outra alteração aplicada.
- [ ] Registrar autor, data, operação e valores anterior e novo nos eventos do pedido. Exibir o histórico na ficha, sem ações de edição ou exclusão, e filtrar os eventos visíveis ao cliente.
- [ ] Expor as ações de análise na API e testar aprovação, rejeição, solicitação desatualizada e falha com reversão da operação.

**Aceite.** A encomenda só muda quando as condições são satisfeitas. Se a validação falhar, os dados vigentes são preservados e o histórico não registra a alteração como aplicada.

## 09 Confirmar ciência da especificação vigente

**Dia 7 · RF10 e RN05 · Depende de 06 e 08**

- [ ] Registrar ciência com funcionário, contador de alterações consultado e data.
- [ ] Integrar a validação à transição para produção. Exigir nova confirmação depois de aplicar uma alteração.
- [ ] Expor os dados aprovados e o contador de alterações na API. Testar o bloqueio quando o funcionário consultou dados anteriores.

**Aceite.** Após aplicar uma alteração, a ciência é reiniciada. A produção só começa após a confirmação da especificação atual.

## 10 Registrar recebimentos e calcular saldo

**Dia 8 · RF11, RN06 e RNF02 · Depende de 03 e 04**

- [ ] Persistir pagamentos com valor monetário decimal, data, forma e responsável. Exibir os recebimentos e o saldo em aberto na ficha.
- [ ] Calcular saldo pelo preço vigente e pelos recebimentos válidos. Bloquear lançamento acima do saldo, inclusive em gravações simultâneas.
- [ ] Impedir duplicidade no reenvio da mesma operação. Disponibilizar o cálculo para alteração, entrega, portal e indicadores.
- [ ] Expor o registro e o histórico de pagamentos na API. Testar pagamento parcial, excesso, reenvio e concorrência.

**Aceite.** Um pedido de R$ 300 com recebimentos de R$ 100 e R$ 50 mostra R$ 150 pagos e R$ 150 de saldo. Reenviar o lançamento de R$ 50 não altera esse total.

## 11 Confirmar entrega, cancelar e estornar

**Dia 9 · RF12, RF13 e RN06–RN08 · Depende de 05, 06 e 10**

- [ ] Confirmar entrega ou retirada com data, funcionário e modalidade. Exigir pedido pronto e verificar a quitação na mesma operação que registra a entrega.
- [ ] Registrar cancelamento autorizado com motivo e liberar as reservas de material ainda abertas após integrar o card 14. Manter o histórico e registrar perdas e pendências financeiras quando aplicável.
- [ ] Registrar estorno com motivo e vínculo ao recebimento original. Sinalizar valores já pagos em pedido cancelado para conciliação, sem presumir que foram devolvidos.
- [ ] Expor as ações na API e testar bloqueio de entrega, quitação, cancelamento e estorno.

**Aceite.** Pedido pronto com saldo pendente fica bloqueado para entrega. Após quitar, é possível registrá-la. Um cancelamento libera as reservas aplicáveis e preserva os lançamentos originais.

## 12 Implementar a API do portal do cliente

**Dia 10 · Complementa RF01, RF03, RF06–RF08 e RF11 · Depende de 03, 04, 07, 08 e 10**

- [ ] Expor lista e ficha com pedidos próprios, status, saldo, decisões e histórico permitido.
- [ ] Reaproveitar a solicitação de alteração e registrar concordância com novo preço ou prazo pelo canal definido no card 02.
- [ ] Testar que dados de outro cliente, custos internos e observações administrativas ficam fora da resposta do servidor.

**Aceite.** Pela API, um cliente autenticado acompanha um pedido próprio e solicita uma mudança. As ações e os dados disponíveis correspondem às permissões dele.

## 14 Controlar materiais e estoque

**Dias 10–11 · RF15 e RNF02 · Depende de 04 · Integrar com 06 e 11**

- [ ] Cadastrar materiais com unidade de medida e associar quantidades necessárias aos itens.
- [ ] Persistir entradas, saídas, reservas, consumo e perdas. Calcular estoque físico e disponível, com validações transacionais.
- [ ] Integrar reserva e consumo ao pedido e liberar reservas remanescentes no cancelamento. Preservar os movimentos já realizados.
- [ ] Expor cadastro e extrato na API. Testar reserva, consumo, perda e cancelamento.

**Aceite.** Com 10 unidades físicas e 3 reservadas, o saldo disponível é 7. Consumir as 3 deixa 7 unidades físicas e 7 disponíveis, sem descontar duas vezes.

## 15 Listar necessidades de compra

**Dias 11–12 · RF16 · Depende de 05 e 14**

- [ ] Somar as necessidades ainda não cobertas dos pedidos aprovados por material. Considerar as reservas existentes e evitar contar a mesma disponibilidade duas vezes.
- [ ] Mostrar material, unidade e quantidade faltante em uma lista consultável.
- [ ] Testar pedidos que disputam o mesmo material, reposição e cancelamento.

**Aceite.** Se a demanda ainda descoberta é de 8 unidades e há 5 disponíveis, a lista mostra falta de 3. Registrar a entrada dessas 3 unidades elimina a falta.

## 16 Exibir indicadores operacionais

**Dia 12 · RF14 · Depende de 04, 06, 10 e 11**

- [ ] Agregar quantidades por status, pedidos atrasados e saldo a receber por período de criação do pedido.
- [ ] Excluir pedidos cancelados do saldo operacional e reutilizar a definição de atraso da ficha.
- [ ] Expor os indicadores com filtro por período e testar os resultados com dados conhecidos.

**Aceite.** As quantidades e o saldo correspondem aos pedidos do período e o saldo operacional ignora pedidos cancelados.

## 17 Registrar custos e calcular resultado estimado

**Dia 12 · RF17–RF18 · Depende de 04, 10 e 11 · Integrar com 14**

- [ ] Registrar custo por pedido, categoria, valor e responsável. Cobrir material consumido, mão de obra, entrega, retrabalho e perdas.
- [ ] Exibir receita líquida, descontos, custos registrados e resultado estimado por pedido e período. Indicar custos ausentes e mostrar recebimentos em separado.
- [ ] Expor lançamento de custos e consulta do relatório na API. Testar descontos, custos parciais e diferença entre valor da venda e valor recebido.

**Aceite.** Um pedido com receita líquida de R$ 280 e R$ 100 em custos registrados mostra R$ 180 de resultado estimado, mesmo que o cliente tenha pago apenas R$ 150. O relatório sinaliza custos ausentes.

## 18 Verificar segurança, desempenho e recuperação

**Dias 13–14 · Preparar ambiente e dados durante a implementação · RNF01–RNF04, RNF07 e RNF08**

- [ ] Revisar autorização no servidor e armazenamento de senhas. Configurar e verificar HTTPS no ambiente publicado.
- [ ] Rodar a suíte de integração para alterações, pagamentos, autorização, estoque e cancelamento. Conferir rollback, concorrência e reenvio de operações.
- [ ] Carregar 1.000 pedidos e medir busca e abertura de pedido com 10 acessos simultâneos. Registrar p95 e comparar à meta de até 2 segundos no ambiente de demonstração.
- [ ] Configurar backup diário no ambiente hospedado. Demonstrar restauração da última cópia em banco separado e registrar o tempo em relação à meta de até 4 horas.
- [ ] Documentar a API, configuração, migrações, testes e procedimento de restauração.

**Aceite.** Cada RNF de backend tem um resultado registrado com o comando, teste ou roteiro usado. Os resultados atendem aos critérios acordados.

## 19 Implementar as telas a partir do Figma

**Aguardando o Figma · Data a definir · RNF05 e RNF06**

- [ ] Mapear as telas e os componentes do Figma aos fluxos, perfis e endpoints do backend.
- [ ] Implementar o frontend e integrá-lo à API, com atenção aos estados de carregamento, erro e confirmação.
- [ ] Conferir cadastro, alteração e consulta de saldo com um usuário de teste. Verificar português, mensagens, confirmação de ações irreversíveis, teclado e larguras de 360 px e 1366 px.

**Aceite.** As telas previstas no Figma executam os fluxos com a API real, e as verificações de usabilidade e compatibilidade passam.

## 20 Ensaiar e fechar a entrega

**Dias 13–14 para a verificação pela API · Repetir o roteiro pelas telas após o card 19**

- [ ] Preparar dados e contas de demonstração para administrador, atendimento, produção, financeiro e cliente.
- [ ] Executar o roteiro completo com a mesma encomenda sempre que possível. Cobrir pedido, aprovação, alteração, ciência, impedimento, estoque, compra, pagamentos, entrega, custos e resultado.
- [ ] Ensaiar os bloqueios de maior valor para a apresentação. Usar uma solicitação desatualizada, prazo de alteração inválido, acesso de outro cliente e tentativa de entrega com saldo.
- [ ] Conferir os requisitos vigentes no quadro e repetir a execução partindo de banco vazio conforme o README.

**Aceite.** O README permite rodar o sistema e repetir a demonstração pela API. A entrega completa inclui a repetição pelas telas e o aceite dos RFs e RNFs vigentes.

## Instrumentar casos de uso com Spring AOP

**Etapa de implementação dos serviços**

- [ ] Aplicar um `@Aspect` às operações de aplicação escolhidas para registrar duração e resultado.
- [ ] Verificar que o aspecto preserva o retorno e as exceções dos casos de uso.

**Aceite.** Uma chamada de sucesso e uma chamada com erro geram registros de duração sem alterar o comportamento do serviço.
