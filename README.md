# API Raízes do Nordeste

Esta é a API Backend do projeto multidisciplinar **Raízes do Nordeste**, construída com **Java 21, Spring Boot 3.4.x, PostgreSQL e JWT**. A aplicação é responsável por gerenciar estoques de forma isolada, processar pedidos multicanal e tratar bonificações de fidelidade com consentimento LGPD.

## Pré-Requisitos

Para rodar este projeto localmente, você precisará de:
1. **Docker e Docker Compose** (para levantar o banco de dados PostgreSQL sem instalação local)
2. **Java 21 JDK** 
3. **Maven 3.9+** (opcional, o projeto possui o *wrapper* `./mvnw`)

## 1. Configurando o Ambiente (.env)

O banco de dados e os tokens de segurança dependem de variáveis de ambiente. Siga estes passos:

1. Na pasta `fonte/api` do projeto, faça uma cópia do arquivo `.env.example` nomeando-o para `.env`.
   - Windows: `copy .env.example .env`
   - Linux/Mac: `cp .env.example .env`
2. No arquivo `.env`, certifique-se de que a variável `JWT_SECRET` tenha no mínimo 32 caracteres (ela é o *salt* usado pelo Spring Security).

## 2. Rodando a Infraestrutura (PostgreSQL)

Abra o terminal na pasta raiz da aplicação (`fonte/api`) e levante o contêiner do banco de dados:
```bash
docker-compose up -d
```
Verifique se a base subiu com `docker ps`. Ela usará a porta `5432` da sua máquina host.

## 3. Rodando a Aplicação (API)

Ainda na pasta raiz `fonte/api`, execute o seguinte comando do Maven Wrapper:
```bash
# No Windows
mvnw.cmd spring-boot:run

# No Linux/Mac
./mvnw spring-boot:run
```
O Spring Boot inicializará. O **Flyway** rodará as *migrations* e o seed populando o BD com os estados iniciais automaticamente. A porta mapeada para a aplicação é a **8080**.

## 4. Documentação Viva (Swagger)

A API possui documentação auto-gerada pela biblioteca `springdoc-openapi`. Com o serviço em execução, acesse:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

## 5. Rodando os Testes (E2E)

A aplicação conta com uma sólida cobertura end-to-end simulando cenários complexos reais. Os testes de integração (como `FluxoClienteE2ETest`) não usam o banco docker local, pois o código sobe um container descartável via `Testcontainers` para total isolamento.
Para rodar a suíte inteira e validar a coerência, execute:
```bash
# No Windows
mvnw.cmd test
```

## 6. Coleção Postman

Na raiz da pasta `/fonte/api`, você encontra o arquivo `postman_collection.json`. 
Você pode importar esse arquivo no Postman ou no Insomnia para ter as requisições prontas (Headers e Body) do fluxo completo de ponta-a-ponta testando no seu ambiente executável.
