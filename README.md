# Sistema de Gestão de Encomendas Personalizadas

Aplicação web para centralizar o gerenciamento de encomendas
personalizadas, integrando atendimento, produção, financeiro e
administração em um único ambiente.

O projeto tem como objetivo garantir que todos os envolvidos trabalhem
com informações atualizadas sobre cada pedido, reduzindo retrabalho,
falhas de comunicação, atrasos e problemas no acompanhamento de
pagamentos.

## Arquitetura

O projeto adota um **monólito modular em Spring Boot com modelagem orientada
ao domínio (DDD)**. O código é organizado por área do negócio em `acesso`,
`clientes`, `encomendas`, `financeiro` e `estoque`. Os impedimentos de produção
integram o domínio de encomendas.
Cada área reúne as entidades, os tipos e as regras no seu pacote `dominio`.

Um único processo e um banco PostgreSQL simplificam a execução e a manutenção
para uma equipe pequena. Essa estrutura permite coordenar em uma transação
operações que precisam acontecer juntas, como aplicar uma alteração e registrar
seu histórico. A separação por área torna as regras mais fáceis de
localizar e testar.

O modelo expõe operações como `aprovar`, `aplicarAlteracao` e
`iniciarProducao`. Esses métodos validam as condições da operação antes de
alterar o estado das entidades. Consulte o [diagrama de classes](docs/uml-dominio.md)
para ver os atributos, os métodos e as multiplicidades.

## Dependências

O projeto utiliza **Java 21** e **Maven**. As dependências são gerenciadas pelo Maven no arquivo [`pom.xml`](pom.xml).

### Aplicação

- **Spring Boot Data JPA** (`spring-boot-starter-data-jpa`) — persistência e acesso a dados com JPA.
- **Spring Boot Flyway** (`spring-boot-starter-flyway`) — gerenciamento de migrações do banco de dados.
- **Spring Boot Security** (`spring-boot-starter-security`) — autenticação e segurança.
- **Spring Boot Web MVC** (`spring-boot-starter-webmvc`) — criação da aplicação web e APIs HTTP.
- **Flyway Database PostgreSQL** (`flyway-database-postgresql`) — suporte do Flyway ao PostgreSQL.
- **PostgreSQL JDBC Driver** (`postgresql`) — conexão com o banco PostgreSQL (runtime).

### Desenvolvimento

- **Spring Boot DevTools** (`spring-boot-devtools`) — recursos de desenvolvimento e reinicialização automática (runtime, opcional).
- **Lombok** (`lombok`) — redução de código repetitivo, como getters e construtores (opcional).

### Testes

- `spring-boot-starter-data-jpa-test`
- `spring-boot-starter-flyway-test`
- `spring-boot-starter-security-test`
- `spring-boot-starter-webmvc-test`

As dependências de teste são utilizadas apenas durante a execução dos testes.

## Executar o projeto

Instale o JDK 21 e tenha uma instância PostgreSQL acessível. Crie um banco
vazio chamado `encomendas` e use um usuário com permissão para criar tabelas
nesse banco. O Flyway aplica as migrações e o Hibernate valida o
esquema na inicialização.

No Linux ou macOS:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/encomendas
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD='sua-senha-local'
./mvnw spring-boot:run
```

No PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:postgresql://localhost:5432/encomendas'
$env:SPRING_DATASOURCE_USERNAME = 'postgres'
$env:SPRING_DATASOURCE_PASSWORD = 'sua-senha-local'
.\mvnw.cmd spring-boot:run
```

Com o banco configurado, rode `./mvnw test` ou `.\mvnw.cmd test` para executar a
suíte. Para executar somente os testes do domínio no PowerShell, use
`.\mvnw.cmd '-Dtest=EncomendaTest' test`.
