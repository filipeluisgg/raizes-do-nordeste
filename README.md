# API Raízes do Nordeste

Esta é a API Backend do projeto multidisciplinar **Raízes do Nordeste**, construída com **Java 21, Spring Boot 3.4.x, PostgreSQL e JWT**. A aplicação é responsável por gerenciar estoques de forma isolada, processar pedidos multicanal e tratar bonificações de fidelidade com consentimento LGPD.

## Pré-Requisitos

Para rodar este projeto localmente, você precisará de:
1. **Docker e Docker Compose** (para levantar o banco de dados PostgreSQL sem instalação local)
2. **Java 21 JDK** 
3. **Maven 3.9+**

## 1. Obtendo o Código-Fonte (.zip)

1. Na página do repositório no GitHub, clique no botão verde `Code` e selecione a opção `Download ZIP`.
2. Extraia o conteúdo do arquivo `.zip` baixado, no diretório de sua preferência.
3. Abra o seu terminal e navegue diretamente para a pasta `raizes-do-nordeste-main` que foi extraída.

## 2. Configurando o Ambiente (.env)

O banco de dados e os tokens de segurança dependem de variáveis de ambiente. Siga estes passos:

1. No diretório raiz (pasta "raizes-do-nordeste-main") do projeto, faça uma cópia do arquivo `.env.example` nomeando-o somente  para `.env`.
2. Neste arquivo `.env`, certifique-se de que a variável `JWT_SECRET` tenha no mínimo 32 caracteres (ela é o *salt* usado pelo Spring Security).

## 3. Rodando a Infraestrutura (PostgreSQL)

Com o terminal na pasta raiz da aplicação, levante o contêiner do banco de dados:
```bash
docker compose up -d
```
Verifique se a base subiu com `docker ps`. Ela usará a porta `5432` da sua máquina host.

## 4. Rodando a Aplicação (API)

Ainda na pasta raiz "raizes-do-nordeste-main", execute o seguinte comando do Maven:
```bash
mvn spring-boot:run
```
O Spring Boot inicializará. O **Flyway** rodará as *migrations* e o seed populando o BD com os estados iniciais automaticamente. A porta mapeada para a aplicação é a **8080**.

## 5. Documentação interativa (Swagger)

A API possui documentação auto-gerada pela biblioteca `springdoc-openapi`. Com a aplicação em execução, acesse:
**[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

## 6. Rodando os Testes (E2E)

A aplicação conta com uma cobertura de testes simulando cenários reais. Para rodar a suíte inteira e validar a coerência, execute o seguinte comando no diretório da API:
```bash
mvn test
```

## 7. Coleção Postman/Insomnia

No diretório raiz da implementação (pasta `/raizes-do-nordeste-main`), encontra-se o arquivo `postman_collection.json`. 
Pode-se importar esse arquivo no Postman ou no Insomnia para ter as requisições prontas (Headers e Body) do fluxo completo de ponta-a-ponta testando no seu ambiente executável.
