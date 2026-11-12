# Sistema de Gestão de Encomendas Personalizadas

Aplicação web para centralizar o gerenciamento de encomendas
personalizadas, integrando atendimento, produção, financeiro e
administração em um único ambiente.

O projeto tem como objetivo garantir que todos os envolvidos trabalhem
com informações atualizadas sobre cada pedido, reduzindo retrabalho,
falhas de comunicação, atrasos e problemas no acompanhamento de
pagamentos.

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

Com o Maven Wrapper incluído no repositório, execute:

```bash
./mvnw spring-boot:run
```

No Windows, use:

```bat
mvnw.cmd spring-boot:run
```
