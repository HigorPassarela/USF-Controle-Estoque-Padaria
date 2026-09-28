# USF - Controle de Estoque (Padaria)

Sistema de **controle de estoque e vendas** para padaria, desenvolvido em **Java 21** com **Spring Boot**, banco **PostgreSQL** versionado com **Flyway**, documentação de API com **Swagger (OpenAPI)**, health check com **Actuator**, ambiente local com **Docker Compose** (incluindo **pgAdmin**) e pipeline de **CI** com **GitHub Actions**.

> **Status:** estrutura base do projeto (Sprint 0). Ainda não há entidades, migrations de tabelas nem endpoints. Esses itens estão planejados no board do Jira (projeto **CE**).

---

## Início rápido

```bash
git clone https://github.com/HigorPassarela/USF-Controle-Estoque-Padaria.git
cd USF-Controle-Estoque-Padaria

cp .env.example .env
docker compose -f docker/docker-compose.yml --env-file .env up -d

mvn spring-boot:run
```

| Recurso      | URL                                    |
|--------------|----------------------------------------|
| API          | http://localhost:8080                  |
| Swagger UI   | http://localhost:8080/swagger-ui.html  |
| Health check | http://localhost:8080/actuator/health  |
| pgAdmin      | http://localhost:5050                  |

---

## Sumário

1. [Visão geral do projeto](#1-visão-geral-do-projeto)
2. [Stack](#2-stack)
3. [Pré-requisitos](#3-pré-requisitos)
4. [Estrutura de pastas](#4-estrutura-de-pastas)
5. [Arquitetura](#5-arquitetura)
6. [Configuração do ambiente](#6-configuração-do-ambiente)
7. [Subindo os containers](#7-subindo-os-containers)
8. [Acessando o pgAdmin](#8-acessando-o-pgadmin)
9. [Rodando a aplicação](#9-rodando-a-aplicação)
10. [Imagem Docker da aplicação](#10-imagem-docker-da-aplicação)
11. [Documentação da API (Swagger)](#11-documentação-da-api-swagger)
12. [Health check (Actuator)](#12-health-check-actuator)
13. [Banco de dados e migrations (Flyway)](#13-banco-de-dados-e-migrations-flyway)
14. [CI (GitHub Actions)](#14-ci-github-actions)
15. [Comandos úteis do Docker](#15-comandos-úteis-do-docker)
16. [Problemas comuns](#16-problemas-comuns)
17. [Roadmap](#17-roadmap)
18. [Convenções](#18-convenções)

---

## 1. Visão geral do projeto

O sistema gerencia o ciclo de **cadastros, vendas e estoque** da padaria. O escopo planejado é:

**Cadastros**
- Clientes e Tipos de Cliente
- Fornecedores
- Produtos, Tipos de Produto, Categorias de Produto e Unidades de Medida
- Produto x Fornecedor (custo unitário por fornecedor)
- Métodos de Pagamento
- Localização: Estados, Cidades e Bairros

**Vendas**
- Pedidos de Venda, Itens do Pedido e Pagamentos

**Estoque**
- Entradas de estoque e Lotes
- Estoque (saldo atual por lote)
- Movimentações de estoque e Tipos de Movimentação (entrada/saída)

---

## 2. Stack

| Tecnologia        | Uso                                             |
|-------------------|-------------------------------------------------|
| Java 21           | Linguagem                                       |
| Spring Boot 3.5   | Framework (Web, Data JPA, Validation)           |
| PostgreSQL 16     | Banco de dados relacional                       |
| Flyway            | Versionamento e execução das migrations         |
| springdoc-openapi | Documentação da API (Swagger UI / OpenAPI 3)    |
| Spring Actuator   | Health check da aplicação e do banco            |
| Maven             | Build e gerenciamento de dependências           |
| Docker            | Imagem da aplicação (Dockerfile multi-stage)    |
| Docker Compose    | Ambiente local (PostgreSQL + pgAdmin)           |
| pgAdmin 4         | Interface web para visualizar o banco           |
| GitHub Actions    | Pipeline de CI (build da imagem Docker)         |

---

## 3. Pré-requisitos

- [JDK 21](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/) (ou a integração Maven da sua IDE)
- [Docker](https://docs.docker.com/get-docker/) e Docker Compose v2
- Git

Confira as versões instaladas:

```bash
java -version
mvn -version
docker --version
docker compose version
```

---

## 4. Estrutura de pastas

```
USF-Controle-Estoque-Padaria/
├── .github/
│   └── workflows/
│       └── ci.yml                  # pipeline de CI (gatilho manual)
├── docker/
│   ├── docker-compose.yml          # PostgreSQL + pgAdmin
│   └── pgadmin/
│       └── servers.json            # servidor pré-cadastrado no pgAdmin
├── .dockerignore
├── .env.example                    # modelo das variáveis de ambiente
├── .gitignore
├── Dockerfile                      # imagem da aplicação (multi-stage)
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/br/com/controleestoque/
    │   │   ├── ControleEstoqueApplication.java   # classe main
    │   │   ├── config/                           # configurações (OpenApiConfig)
    │   │   ├── controller/                       # controllers REST
    │   │   ├── dto/                              # DTOs de request/response
    │   │   ├── entity/                           # entidades JPA
    │   │   ├── exception/                        # exceções e handler global
    │   │   ├── repository/                       # repositories Spring Data JPA
    │   │   └── service/                          # regras de negócio
    │   └── resources/
    │       ├── application.yaml                  # configuração única da aplicação
    │       └── db/migration/
    │           └── V1__baseline.sql              # migrations Flyway
    └── test/
        └── java/br/com/controleestoque/          # testes automatizados
```

---

## 5. Arquitetura

O projeto segue uma **arquitetura em camadas**, com responsabilidades bem separadas:

```
Cliente HTTP
     │
     ▼
┌─────────────┐   recebe/valida requests, devolve responses (DTOs)
│ Controller  │   sem regra de negócio (controllers "finos")
└──────┬──────┘
       ▼
┌─────────────┐   regras de negócio, validações de integridade,
│  Service    │   controle de transações
└──────┬──────┘
       ▼
┌─────────────┐   acesso a dados via Spring Data JPA
│ Repository  │
└──────┬──────┘
       ▼
┌─────────────┐   mapeamento objeto-relacional
│   Entity    │
└──────┬──────┘
       ▼
  PostgreSQL  ◄── schema criado e versionado pelo Flyway
```

### Responsabilidade de cada pacote

| Pacote        | Responsabilidade                                                                 |
|---------------|----------------------------------------------------------------------------------|
| `controller`  | Endpoints REST, códigos HTTP coerentes e integração com os Services              |
| `service`     | Regras de negócio, validações de existência/integridade e transações             |
| `repository`  | Interfaces Spring Data JPA e consultas                                           |
| `entity`      | Entidades JPA alinhadas com as migrations Flyway                                 |
| `dto`         | Objetos de request/response. **Entidades nunca são expostas diretamente na API** |
| `exception`   | Exceções de domínio e handler global com resposta JSON padronizada               |
| `config`      | Beans e configurações da aplicação (ex.: `OpenApiConfig`)                        |

### Decisões técnicas

- **O schema é controlado só pelo Flyway.** O Hibernate roda com `ddl-auto: validate`: ele apenas confere se as entidades batem com o banco, sem criar ou alterar tabelas.
- **Entidades JPA não vão para a API.** A comunicação usa DTOs, com Bean Validation nos requests.
- **`open-in-view` desabilitado**, para evitar consultas preguiçosas fora da camada de serviço.
- **Datas em UTC** no Hibernate.
- **API documentada com OpenAPI 3**, gerada automaticamente a partir dos controllers e DTOs.
- **Health check via Actuator**, expondo apenas os endpoints `health` e `info`.
- **Dockerfile multi-stage**: o Maven compila dentro do próprio build da imagem, então não é preciso ter nada instalado além do Docker para gerar a imagem.

---

## 6. Configuração do ambiente

### 6.1. Variáveis de ambiente

Copie o arquivo de exemplo e ajuste se necessário:

```bash
cp .env.example .env
```

| Variável           | Padrão            | Descrição                                 |
|--------------------|-------------------|-------------------------------------------|
| `DB_HOST`          | `localhost`       | Host do PostgreSQL (usado pela aplicação) |
| `DB_PORT`          | `5432`            | Porta do PostgreSQL                       |
| `DB_NAME`          | `ce_db`           | Nome do banco                             |
| `DB_USER`          | `ce_user`         | Usuário do banco                          |
| `DB_PASSWORD`      | `ce_password`     | Senha do banco                            |
| `PGADMIN_EMAIL`    | `admin@admin.com` | Login do pgAdmin                          |
| `PGADMIN_PASSWORD` | `admin`           | Senha do pgAdmin                          |
| `PGADMIN_PORT`     | `5050`            | Porta local do pgAdmin                    |

> O arquivo `.env` **não vai para o Git** (está no `.gitignore`). Nunca faça commit de credenciais reais.

### 6.2. Como a aplicação lê essas variáveis

O `application.yaml` usa valores padrão iguais aos do `.env.example`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:ce_db}
    username: ${DB_USER:ce_user}
    password: ${DB_PASSWORD:ce_password}
```

Ou seja: **sem alterar nada, tudo funciona com os valores padrão**.

O Spring Boot **não lê o arquivo `.env` sozinho**. Se você mudar as credenciais no `.env`, precisa também informar esses valores para a aplicação, exportando as variáveis no terminal ou configurando-as na sua IDE (Run Configuration > Environment variables).

Exemplo no terminal (Linux/macOS):

```bash
export DB_PASSWORD=minha_senha
mvn spring-boot:run
```

Se você alterar `DB_NAME` ou `DB_USER`, atualize também o `MaintenanceDB` e o `Username` no arquivo `docker/pgadmin/servers.json`.

---

## 7. Subindo os containers

O `docker-compose.yml` fica dentro da pasta `docker/`, então rode os comandos **a partir da raiz do projeto**.

### Subir tudo (PostgreSQL + pgAdmin)

```bash
docker compose -f docker/docker-compose.yml --env-file .env up -d
```

Sem `.env`, remova o `--env-file .env` e os valores padrão do compose serão usados.

### Verificar se está tudo rodando

```bash
docker compose -f docker/docker-compose.yml ps
```

O `ce-postgres` deve aparecer como `healthy`.

### Parar os containers (mantendo os dados)

```bash
docker compose -f docker/docker-compose.yml down
```

### Parar e apagar todos os dados

```bash
docker compose -f docker/docker-compose.yml down -v
```

> Atenção: o `-v` remove os volumes, apagando o banco e as configurações do pgAdmin.

### O que o compose cria

| Serviço    | Container     | Imagem                | Porta local |
|------------|---------------|-----------------------|-------------|
| `postgres` | `ce-postgres` | `postgres:16-alpine`  | `5432`      |
| `pgadmin`  | `ce-pgadmin`  | `dpage/pgadmin4:9`    | `5050`      |

| Volume          | Conteúdo                              |
|-----------------|---------------------------------------|
| `postgres_data` | Dados do PostgreSQL (persistente)     |
| `pgadmin_data`  | Configurações e sessão do pgAdmin     |

O pgAdmin só inicia depois que o PostgreSQL estiver saudável (`healthcheck` com `pg_isready`).

---

## 8. Acessando o pgAdmin

1. Abra no navegador: **http://localhost:5050**
2. Faça login:
    - **E-mail:** `admin@admin.com`
    - **Senha:** `admin`
3. No painel à esquerda, em **Servers**, já estará cadastrado o servidor **Controle Estoque (local)**.
4. Ao expandir o servidor, informe a senha do banco: **`ce_password`**.
5. Navegue até: `Databases > ce_db > Schemas > public > Tables`.

O servidor vem pré-configurado pelo arquivo `docker/pgadmin/servers.json`.

### Cadastrar o servidor manualmente (se necessário)

Clique com o botão direito em **Servers > Register > Server** e preencha:

| Campo                | Valor         |
|----------------------|---------------|
| Name                 | qualquer nome |
| Host name/address    | `postgres`    |
| Port                 | `5432`        |
| Maintenance database | `ce_db`       |
| Username             | `ce_user`     |
| Password             | `ce_password` |

> O host é `postgres` (nome do serviço no compose), e não `localhost`, porque o pgAdmin roda dentro da rede do Docker.

### Conectando com outro cliente (DBeaver, IntelliJ, etc.)

De fora do Docker, use `localhost`:

```
Host:     localhost
Porta:    5432
Banco:    ce_db
Usuário:  ce_user
Senha:    ce_password
```

---

## 9. Rodando a aplicação

Com os containers no ar, na raiz do projeto:

```bash
mvn spring-boot:run
```

Ou rode a classe `ControleEstoqueApplication` pela IDE.

A aplicação sobe em **http://localhost:8080**.

Na inicialização, o Flyway conecta no PostgreSQL e executa as migrations pendentes. Você verá no log algo como:

```
Successfully applied 1 migration to schema "public"
```

### Gerar o JAR

```bash
mvn clean package
java -jar target/controle-estoque-0.0.1-SNAPSHOT.jar
```

### Endereços da aplicação

| Recurso        | URL                                      |
|----------------|------------------------------------------|
| API            | http://localhost:8080                    |
| Swagger UI     | http://localhost:8080/swagger-ui.html    |
| OpenAPI (JSON) | http://localhost:8080/v3/api-docs        |
| Health check   | http://localhost:8080/actuator/health    |
| pgAdmin        | http://localhost:5050                    |

---

## 10. Imagem Docker da aplicação

O `Dockerfile` fica na **raiz** do projeto e é **multi-stage**:

1. **Build:** usa `maven:3.9-eclipse-temurin-21`, baixa as dependências (camada em cache) e gera o JAR.
2. **Runtime:** usa `eclipse-temurin:21-jre-alpine`, copia só o JAR, roda com usuário sem privilégios e define um `HEALTHCHECK` no `/actuator/health`.

### Gerar a imagem

```bash
docker build -t controle-estoque .
```

### Gerar a imagem executando os testes

Os testes são **opcionais** e desligados por padrão. Para executá-los durante o build:

```bash
docker build --build-arg RUN_TESTS=true -t controle-estoque .
```

### Rodar a imagem

Com o compose no ar, conecte o container na mesma rede do banco:

```bash
docker run --rm -p 8080:8080 \
  --network docker_default \
  -e DB_HOST=postgres \
  controle-estoque
```

- A rede `docker_default` vem do nome da pasta onde está o compose. Se a sua for diferente, confira com `docker network ls`.
- Dentro da rede do Docker, o host do banco é `postgres`, e não `localhost`.
- Para passar opções da JVM, use `-e JAVA_OPTS="-Xmx512m"`.

---

## 11. Documentação da API (Swagger)

A documentação é gerada com o **springdoc-openapi** e fica disponível com a aplicação no ar:

| Recurso        | URL                                      |
|----------------|------------------------------------------|
| Swagger UI     | http://localhost:8080/swagger-ui.html    |
| OpenAPI (JSON) | http://localhost:8080/v3/api-docs        |

No Swagger UI é possível visualizar todos os endpoints, os modelos de request/response, os códigos HTTP e **testar as chamadas direto pelo navegador**.

> Enquanto não houver controllers, o Swagger UI abre normalmente, mas mostra a mensagem "No operations defined in spec". Os endpoints aparecem automaticamente conforme os controllers forem criados.

### Configuração

Os caminhos ficam no `application.yaml`:

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method
    tags-sorter: alpha
```

O título, a descrição e a versão exibidos no topo do Swagger UI ficam na classe `OpenApiConfig`, no pacote `config`:

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI controleEstoqueOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Controle de Estoque API")
                        .description("API de controle de estoque e vendas: cadastros, pedidos, lotes e movimentações.")
                        .version("v0.0.1"));
    }
}
```

### Como os endpoints são documentados

A documentação é gerada automaticamente a partir dos controllers e DTOs. Para enriquecer, use as anotações do `io.swagger.v3.oas.annotations`:

```java
@Tag(name = "Clientes", description = "CRUD de clientes")
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Operation(summary = "Busca um cliente pelo id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @GetMapping("/{id}")
    public ClienteResponse buscar(@PathVariable Long id) {
        // ...
    }
}
```

Os DTOs com Bean Validation (`@NotBlank`, `@Size`, etc.) já têm essas restrições refletidas no schema automaticamente.

> Os endpoints do Actuator não aparecem no Swagger por padrão. Para exibi-los, use `springdoc.show-actuator: true`.
>
> Em produção, considere desabilitar o Swagger com `springdoc.swagger-ui.enabled=false` e `springdoc.api-docs.enabled=false`.

---

## 12. Health check (Actuator)

O **Spring Boot Actuator** expõe endpoints para monitorar a aplicação:

| Recurso | URL                                     |
|---------|-----------------------------------------|
| Health  | http://localhost:8080/actuator/health   |
| Info    | http://localhost:8080/actuator/info     |

O endpoint `health` retorna `UP` quando a aplicação e o banco estão funcionando:

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" }
  }
}
```

Se o PostgreSQL estiver fora do ar, o status muda para `DOWN`, o que ajuda a diagnosticar problemas de conexão.

### Configuração

No `application.yaml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

Só `health` e `info` estão expostos. O `show-details: always` mostra o status de cada componente (como o banco) e é útil em desenvolvimento. Em produção, altere para `when-authorized` ou `never`, para não revelar detalhes da infraestrutura.

---

## 13. Banco de dados e migrations (Flyway)

As migrations ficam em `src/main/resources/db/migration/` e rodam automaticamente ao iniciar a aplicação.

### Padrão de nomes

```
V<versão>__<descricao_em_snake_case>.sql
```

Exemplos:

```
V1__baseline.sql
V2__criar_tabelas_de_localizacao.sql
V3__criar_tabelas_de_cadastro.sql
```

### Regras

- **Nunca edite uma migration que já foi executada.** Crie uma nova versão com a alteração.
- Use sempre **dois underscores** (`__`) depois da versão.
- Versões devem ser crescentes e únicas.
- Cada migration deve rodar sem erros em um banco PostgreSQL vazio.
- As entidades JPA precisam estar alinhadas com as migrations, porque o Hibernate só valida o schema (`ddl-auto: validate`).

### Consultar o histórico de migrations

No pgAdmin ou em qualquer cliente SQL:

```sql
SELECT * FROM flyway_schema_history;
```

---

## 14. CI (GitHub Actions)

A pipeline fica em `.github/workflows/ci.yml` e tem **gatilho manual**: nada roda sozinho em push ou Pull Request.

### O que ela faz

1. **Checkout** do código.
2. **Build image:** executa o `docker build`. O próprio Dockerfile compila o projeto com Maven e gera a imagem. Se a compilação (ou os testes, quando ativados) falhar, a pipeline fica vermelha.

### Testes opcionais

Ao disparar o workflow, existe a opção **Run tests**, desmarcada por padrão:

- **Desmarcada:** compila e monta a imagem, sem executar testes.
- **Marcada:** executa `mvn test` antes de empacotar (via `--build-arg RUN_TESTS=true`).

### Como executar

Pela interface:

1. Abra a aba **Actions** do repositório.
2. Selecione o workflow **CI**.
3. Clique em **Run workflow**, escolha a branch e marque (ou não) **Run tests**.

Pelo terminal, com o [GitHub CLI](https://cli.github.com/):

```bash
# sem testes
gh workflow run ci.yml

# com testes
gh workflow run ci.yml -f run_tests=true
```

> O botão **Run workflow** só aparece depois que o `ci.yml` estiver na branch padrão (`main`).

### Workflow

```yaml
name: CI

run-name: "CI • ${{ github.ref_name }} • tests: ${{ inputs.run_tests }}"

on:
  workflow_dispatch:
    inputs:
      run_tests:
        description: "Run tests"
        type: boolean
        default: false

permissions:
  contents: read

jobs:
  build:
    name: Build
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Build image
        run: |
          docker build \
            --build-arg RUN_TESTS=${{ inputs.run_tests }} \
            -t controle-estoque:${{ github.sha }} .
```

> O badge no topo do README mostra o resultado da última execução do workflow.

---

## 15. Comandos úteis do Docker

```bash
# Ver logs do PostgreSQL
docker logs -f ce-postgres

# Ver logs do pgAdmin
docker logs -f ce-pgadmin

# Abrir o psql dentro do container
docker exec -it ce-postgres psql -U ce_user -d ce_db

# Reiniciar um serviço
docker compose -f docker/docker-compose.yml restart postgres

# Listar volumes
docker volume ls

# Listar redes
docker network ls
```

---

## 16. Problemas comuns

**A aplicação não conecta no banco**
- Confirme se o container está rodando e saudável: `docker compose -f docker/docker-compose.yml ps`.
- Verifique se as variáveis da aplicação batem com as do `.env` (lembre que o Spring não lê o `.env` sozinho).
- Consulte `http://localhost:8080/actuator/health` para ver o status do componente `db`.

**Porta 5432 ou 5050 já em uso**
- Altere `DB_PORT` ou `PGADMIN_PORT` no `.env` e suba os containers de novo.
- Se mudar `DB_PORT`, informe o mesmo valor para a aplicação.

**O servidor não aparece no pgAdmin**
- O pgAdmin só importa o `servers.json` na **primeira** inicialização, com o volume vazio.
- Solução: `docker compose -f docker/docker-compose.yml down -v` e suba novamente (isso apaga os dados do banco).
- Ou cadastre o servidor manualmente (seção 8).

**Não consigo fazer login no pgAdmin**
- O e-mail precisa ser válido. Domínios como `.local` são rejeitados.
- As credenciais de login só são aplicadas na primeira inicialização. Para trocá-las depois, recrie os volumes com `down -v`.

**pgAdmin: "password authentication failed for user ce_user"**
- Confirme que a senha digitada é a mesma do `DB_PASSWORD` no `.env`.
- O `POSTGRES_PASSWORD` só é aplicado na **primeira** criação do volume `postgres_data`. Se o volume foi criado antes com outra senha, redefina-a sem perder dados:
```bash
  docker exec -it ce-postgres psql -U ce_user -d ce_db -c "ALTER USER ce_user WITH PASSWORD 'ce_password';"
```
- Ou recrie tudo com `down -v` (apaga os dados) e suba de novo.

**Erro de validação do schema no Hibernate**
- Significa que uma entidade não bate com o banco. Ajuste a entidade ou crie uma nova migration.

**Erro de checksum do Flyway**
- Uma migration já executada foi alterada. Restaure o arquivo original e crie uma nova migration.
- Em ambiente local, você pode recriar o banco com `down -v`.

**Swagger UI não abre (erro 404)**
- Confirme que a dependência `springdoc-openapi-starter-webmvc-ui` está no `pom.xml` e que a aplicação foi reiniciada.
- Use a URL `http://localhost:8080/swagger-ui.html`, que redireciona para a interface.
- Se tiver alterado o caminho em `springdoc.swagger-ui.path`, use o novo valor.

**`/actuator/health` retorna 404**
- Confirme que a dependência `spring-boot-starter-actuator` está no `pom.xml`.
- Verifique se o bloco `management` está no `application.yaml` com `health` na lista de `include`.

**O container da aplicação não conecta no banco**
- Dentro do Docker, `DB_HOST` deve ser `postgres`, e não `localhost`.
- Confirme que o container está na mesma rede do compose (`--network`). Veja o nome com `docker network ls`.

**O botão "Run workflow" não aparece no GitHub**
- O `ci.yml` precisa estar na branch padrão (`main`).
- Confirme que o arquivo está em `.github/workflows/`.

---

## 17. Roadmap

Acompanhado no board do Jira (projeto **CE**). Ordem sugerida:

1. **Base do projeto**: estrutura Java 21 + Spring Boot, pacotes e convenções *(entregue)*
2. **Ambiente local**: Docker Compose com PostgreSQL e pgAdmin *(entregue)*
3. **Imagem Docker e CI**: Dockerfile e pipeline manual no GitHub Actions *(entregue)*
4. **Modelagem do banco** e estratégia de migrations com Flyway
5. **Migrations iniciais** com todas as tabelas, PKs, FKs, constraints e índices
6. **Entities JPA e Repositories**
7. **DTOs, validações e mapeamento** da API
8. **Services** e regras de negócio (cadastros, estoque e vendas)
9. **Controllers REST** e tratamento global de exceções
10. **CRUDs** de todos os cadastros, pedidos, lotes e movimentações
11. **Documentação da API** com OpenAPI/Swagger *(dependência, configuração e `OpenApiConfig` entregues; as anotações nos controllers ficam para a implementação dos endpoints)*
12. **Testes automatizados** (unitários e de integração)

---

## 18. Convenções

- **Pacotes:** `br.com.controleestoque.<camada>`.
- **Tabelas e colunas:** `snake_case`, em português, seguindo o modelo do projeto (ex.: `id_cliente`, `data_pedido`, `valor_total`).
- **Valores monetários:** `NUMERIC` no banco e `BigDecimal` no Java, nunca `double`.
- **API:** Request/Response DTOs, Bean Validation, status HTTP coerentes e erros em JSON padronizado.
- **Controllers finos:** regras de negócio ficam nos Services.
- **Operações críticas** (pedidos, entradas de lote e movimentações de estoque) rodam em **transações**.
- **Segredos:** nunca versionar o `.env`.