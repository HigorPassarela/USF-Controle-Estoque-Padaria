# Como rodar o projeto (passo a passo)

Este guia parte do zero: computador sem nada instalado. Siga na ordem e não pule passos. Os comandos são para **Windows** (PowerShell); no fim há uma nota para Mac e Linux.

O projeto tem três partes, e **as três sobem com um único comando** pelo Docker:

```
+-------------------------+          +---------------------------+
|  Aplicação              |  ----->  |  Banco de dados           |
|  interface web + API    |          |  ce-postgres, porta 5432  |
|  ce-app, porta 8080     |          +---------------------------+
+-------------------------+          |  pgAdmin (ver o banco)    |
                                     |  ce-pgadmin, porta 5050   |
                                     +---------------------------+
```

Você **não precisa instalar Java nem Maven**: o Docker cuida de tudo. Só quem vai programar no projeto precisa do IntelliJ (veja a seção [Para quem vai programar](#para-quem-vai-programar-intellij), no fim).

---

## Passo 1. Instalar o Git

O Git baixa o código do projeto.

1. Acesse https://git-scm.com/download/win e baixe o instalador.
2. Abra o instalador e clique em **Next** em todas as telas, sem mudar nada, até **Install**.
3. Para conferir, abra o **PowerShell**: aperte a tecla Windows, digite `PowerShell` e aperte Enter.
4. Digite o comando abaixo e aperte Enter:

```powershell
git --version
```

Se aparecer algo como `git version 2.47.0`, deu certo. Se aparecer "não é reconhecido", feche o PowerShell, abra de novo e repita.

## Passo 2. Instalar o Docker Desktop

O Docker roda o banco, o pgAdmin e a aplicação sem você instalar nada disso no Windows.

1. Acesse https://www.docker.com/products/docker-desktop/ e baixe a versão para Windows.
2. Instale com as opções padrão. Se ele pedir para ativar o **WSL 2**, aceite.
3. **Reinicie o computador** se o instalador pedir.
4. Abra o **Docker Desktop** pelo menu Iniciar e espere. No canto inferior esquerdo da janela, ele precisa mostrar **Engine running** (bolinha verde). Isso pode levar um ou dois minutos.
5. No PowerShell, confira:

```powershell
docker --version
docker compose version
```

As duas linhas precisam mostrar um número de versão.

> **Importante:** o Docker Desktop precisa estar **aberto** toda vez que você for rodar o projeto. Se ele estiver fechado, nada funciona.

## Passo 3. Baixar o projeto

1. No PowerShell, vá para a pasta onde quer guardar o projeto. Por exemplo, a pasta Documentos:

```powershell
cd $HOME\Documents
```

2. Baixe o código:

```powershell
git clone https://github.com/HigorPassarela/USF-Controle-Estoque-Padaria.git
```

3. Entre na pasta do projeto:

```powershell
cd USF-Controle-Estoque-Padaria
```

**A partir daqui, todos os comandos devem ser digitados dentro desta pasta.** Se fechar o PowerShell, abra de novo e repita os comandos `cd` acima.

## Passo 4. Criar o arquivo de configuração `.env`

### O que é o `.env`

É um arquivo de texto, na raiz do projeto, com as configurações: nome, usuário e senha do banco, e as portas de cada serviço. Ele **não vem pronto** quando você baixa o projeto, porque guarda senhas e por isso não é enviado ao GitHub (está no `.gitignore`). O que vem é um **modelo**, o arquivo `.env.example`, que você copia.

### Criar o arquivo

Dentro da pasta do projeto, rode:

```powershell
Copy-Item .env.example .env
```

Para conferir se ele foi criado:

```powershell
Get-Content .env
```

Deve aparecer o conteúdo abaixo:

```
# Banco de dados
DB_HOST=localhost
DB_PORT=5432
DB_NAME=ce_db
DB_USER=ce_user
DB_PASSWORD=ce_password

# pgAdmin
PGADMIN_EMAIL=admin@admin.com
PGADMIN_PASSWORD=admin
PGADMIN_PORT=5050

# Aplicação
APP_PORT=8080
```

> Se o Windows estiver escondendo arquivos que começam com ponto e você não enxergar o `.env` no Explorer, tudo bem: o comando acima mostra que ele existe.

### O que significa cada linha

| Variável | Valor padrão | Para que serve |
|----------|--------------|----------------|
| `DB_HOST` | `localhost` | Endereço do banco. Só é usado quando a aplicação roda fora do Docker (IntelliJ) |
| `DB_PORT` | `5432` | Porta do banco no seu computador |
| `DB_NAME` | `ce_db` | Nome do banco de dados |
| `DB_USER` | `ce_user` | Usuário do banco |
| `DB_PASSWORD` | `ce_password` | Senha do banco |
| `PGADMIN_EMAIL` | `admin@admin.com` | E-mail para entrar no pgAdmin |
| `PGADMIN_PASSWORD` | `admin` | Senha para entrar no pgAdmin |
| `PGADMIN_PORT` | `5050` | Porta do pgAdmin no seu computador |
| `APP_PORT` | `8080` | Porta da aplicação no seu computador |

### Preciso mudar alguma coisa?

**Não.** Com os valores padrão, tudo funciona. Só mude se tiver um motivo, por exemplo uma porta já estar ocupada por outro programa (veja "Problemas comuns" no fim).

Quem lê o `.env` é o **Docker Compose**, por causa do `--env-file .env` no comando do próximo passo. Ele repassa nome do banco, usuário e senha para a aplicação automaticamente.

> **Cuidado com a senha:** o banco grava usuário e senha só na **primeira vez** que sobe. Se você trocar `DB_PASSWORD` depois, o banco continua com a senha antiga e a aplicação não conecta. Para valer a nova, apague os dados e comece do zero (seção "Apagar todos os dados" no fim). O mesmo vale para o e-mail e a senha do pgAdmin.

## Passo 5. Subir tudo

Este é o comando que liga o projeto inteiro (banco, pgAdmin e aplicação):

```powershell
docker compose -f docker/docker-compose.yml --env-file .env up -d --build
```

O que cada parte faz, se tiver curiosidade:
- `-f docker/docker-compose.yml`: o arquivo que descreve os três serviços fica na pasta `docker`.
- `--env-file .env`: usa as configurações do Passo 4.
- `up -d`: liga tudo em segundo plano (o PowerShell fica livre).
- `--build`: gera a imagem da aplicação a partir do código. Se o código não mudou, ele reaproveita a imagem e é rápido.

**Na primeira vez demora bastante (5 a 10 minutos):** ele baixa o PostgreSQL, o pgAdmin e tudo o que o Java precisa, e compila a aplicação. Nas próximas vezes leva segundos. No final aparece algo assim:

```
 Container ce-postgres  Healthy
 Container ce-pgadmin   Started
 Container ce-app       Started
```

A aplicação só começa a subir depois que o banco estiver pronto, e leva mais uns 20 a 60 segundos para ficar disponível. Para acompanhar:

```powershell
docker compose -f docker/docker-compose.yml logs -f app
```

Quando aparecer uma linha com `Started ControleEstoqueApplication`, está pronta. Aperte **Ctrl + C** para sair do log (a aplicação continua rodando).

Para ver o estado dos três serviços:

```powershell
docker compose -f docker/docker-compose.yml ps
```

Os três (`ce-app`, `ce-pgadmin` e `ce-postgres`) precisam aparecer com status `Up`. O `ce-postgres` deve mostrar `(healthy)`, e o `ce-app` também fica `(healthy)` depois que termina de subir.

## Passo 6. Abrir a interface

Abra o navegador (Chrome, Edge ou Firefox) e acesse:

### http://localhost:8080

Vai abrir a tela **Visão geral** do sistema, com o menu à esquerda (no celular, o menu fica no botão ☰ no canto de cima). Se a página não abrir, a aplicação ainda está subindo: espere um pouco e aperte **F5**.

> Confira também, no rodapé do menu, a bolinha **verde** com o texto "API e banco conectados". Se ela estiver vermelha, veja "Problemas comuns" no fim.

### Seu primeiro produto

Com o sistema vazio, o painel mostra um convite para cadastrar o primeiro produto. Antes dele, o produto precisa de três cadastros simples. Faça nesta ordem:

1. No menu, em **Produtos**, clique em **Categorias** > **Nova categoria**. Digite `Pães` e clique em **Cadastrar categoria**.
2. Em **Tipos de produto** > **Novo tipo de produto**, digite `Produto acabado` e cadastre.
3. Em **Unidades de medida** > **Nova unidade de medida**, digite `Unidade` na descrição e `un` na sigla, e cadastre.
4. Em **Produtos** > **Novo produto**, preencha:
   - Nome: `Pão francês`
   - Preço de venda: `0,75`
   - Unidade de medida, Categoria e Tipo de produto: escolha os que você acabou de criar
5. Clique em **Cadastrar produto**. Ele aparece na lista com saldo 0.

### Sua primeira entrada de estoque

1. No menu, clique em **Saldos** (grupo Estoque) e depois em **Registrar entrada**.
2. Escolha o produto `Pão francês`, quantidade `120` e tipo **Compra de fornecedor**. Se quiser, informe uma **validade**.
3. Clique em **Registrar entrada**. Aparece um aviso com o número do lote criado e o novo saldo.
4. Volte ao **Painel**: os indicadores e o gráfico já mostram o produto.

### Sua primeira saída

1. Em **Saldos**, clique em **Registrar saída**.
2. Escolha o produto e digite uma quantidade. Antes de confirmar, o quadro **De onde vai sair** mostra de quais lotes a quantidade vai sair. Primeiro sai o lote que vence antes, e lotes vencidos ficam de fora.
3. Clique em **Registrar saída**. Uma janela confirma o que saiu de cada lote e o novo saldo.

> Lote vencido não sai pela saída comum. Para tirá-lo do estoque, vá em **Lotes**, marque **Só vencidos** e clique em **Descartar** no lote.

### Outras coisas para conhecer

- **Tema escuro:** botão no rodapé do menu. A escolha fica salva.
- **Movimentações:** histórico de tudo o que entrou e saiu, com filtros por produto, lote e período.
- **Explorador da API** (grupo Ferramentas): lista as 70 rotas da API e permite chamar qualquer uma pela própria tela.
- **Swagger:** a documentação técnica da API continua em http://localhost:8080/swagger-ui.html.

A lista completa de rotas, com parâmetros e respostas, está em [ROTAS.md](ROTAS.md).

## Passo 7. Ver o banco de dados (opcional)

1. Abra http://localhost:5050
2. Entre com e-mail `admin@admin.com` e senha `admin`.
3. No painel da esquerda, abra **Servers > Controle Estoque**.
4. Se pedir senha, digite `ce_password`.
5. Abra **Databases > ce_db > Schemas > public > Tables**. Para ver o conteúdo de uma tabela, clique nela com o botão direito e escolha **View/Edit Data > All Rows**.

---

## Desligar, religar e atualizar

**Desligar tudo** (os dados ficam guardados):

```powershell
docker compose -f docker/docker-compose.yml stop
```

**Ligar de novo outro dia:**

1. Abra o Docker Desktop e espere o **Engine running**.
2. No PowerShell, dentro da pasta do projeto, rode o mesmo comando do Passo 5:

```powershell
docker compose -f docker/docker-compose.yml --env-file .env up -d --build
```

**Baixou uma versão nova do código** (`git pull`) **ou mudou o `.env`:** rode o mesmo comando do Passo 5. O `--build` gera a aplicação de novo com o código atualizado, e o compose recria o que mudou.

## Apagar todos os dados e começar do zero

Isso apaga **tudo** o que foi cadastrado. Não tem volta.

```powershell
docker compose -f docker/docker-compose.yml down -v
```

Depois, volte ao Passo 5.

---

## Para quem vai programar (IntelliJ)

Para programar, é mais prático rodar a aplicação pelo IntelliJ (dá para usar o debug e ver as mudanças sem gerar imagem Docker). Nesse caso, o Docker roda **só o banco e o pgAdmin**.

### 1. Subir só o banco e o pgAdmin

```powershell
docker compose -f docker/docker-compose.yml --env-file .env up -d postgres pgadmin
```

Se a aplicação do Docker já estiver rodando, desligue-a para liberar a porta 8080:

```powershell
docker compose -f docker/docker-compose.yml stop app
```

### 2. Instalar o IntelliJ IDEA

1. Acesse https://www.jetbrains.com/idea/download/ e baixe o **IntelliJ IDEA Community Edition** (gratuito).
2. Instale com as opções padrão.

### 3. Abrir o projeto

1. Abra o IntelliJ.
2. Clique em **Open** e escolha a pasta `USF-Controle-Estoque-Padaria` que você baixou no Passo 3.
3. Se aparecer a pergunta **Trust project?**, clique em **Trust Project**.
4. Espere. No canto inferior direito aparece uma barra de progresso enquanto ele baixa as dependências (Maven). Na primeira vez leva alguns minutos.

### 4. Configurar o Java 21

O projeto precisa do **Java 21**. Nem mais velho, nem outro.

1. Menu **File > Project Structure** (ou `Ctrl + Alt + Shift + S`).
2. Em **Project > SDK**, abra a lista:
   - se já existir uma opção com **21** no nome, escolha ela;
   - se não existir, clique em **Download JDK...**, escolha a **Version 21**, qualquer fornecedor (por exemplo, *Eclipse Temurin*), e clique em **Download**.
3. Em **Language level**, deixe **21**.
4. Clique em **OK**.

### 5. Rodar

1. No painel da esquerda, abra `src > main > java > br.com.controleestoque` e dê dois cliques em **`ControleEstoqueApplication`**.
2. Ao lado da linha `public class ControleEstoqueApplication`, clique no triângulo verde e escolha **Run 'ControleEstoqueApplication'**.
3. Embaixo abre o console. Quando aparecer `Started ControleEstoqueApplication`, está pronta. Abra http://localhost:8080 como no Passo 6.

Para desligar, clique no quadrado vermelho (Stop) no console.

### 6. Só se você mudou o `.env`

Se você deixou o `.env` com os valores padrão, **pule este passo**.

O IntelliJ **não lê o `.env`**: a aplicação usa os valores padrão gravados nela, que são iguais aos do `.env.example`. Se você mudou algum valor do banco no `.env` (porta, senha...), informe o mesmo valor ao IntelliJ:

1. No alto da tela, ao lado do triângulo verde, clique em **ControleEstoqueApplication > Edit Configurations...**
2. Procure o campo **Environment variables**. Se não aparecer, clique em **Modify options > Environment variables**.
3. Digite só as variáveis que você mudou, separadas por ponto e vírgula. Por exemplo, se mudou a porta para 5433:

```
DB_PORT=5433
```

   Se mudou porta e senha:

```
DB_PORT=5433;DB_PASSWORD=minhaSenhaNova
```

4. Clique em **OK** e rode de novo.

Aqui o `DB_HOST` continua `localhost`: a aplicação roda no Windows e enxerga o banco pela porta exposta no seu computador.

---

## Deu errado? Problemas comuns

**"docker: command not found" ou "não é reconhecido"**
O Docker não está instalado ou o PowerShell foi aberto antes da instalação. Feche e abra o PowerShell. Se continuar, reinstale o Docker Desktop (Passo 2).

**"error during connect" ou "Cannot connect to the Docker daemon"**
O Docker Desktop está fechado. Abra ele e espere aparecer **Engine running**.

**"port is already allocated" ou "address already in use" (5432, 5050 ou 8080)**
Outro programa está usando a porta. Troque a porta no `.env` e rode o Passo 5 de novo:
- **5432** (`DB_PORT`): provavelmente há um PostgreSQL instalado direto no Windows. Pare o serviço dele (tecla Windows, digite `Serviços`, procure *postgresql*, clique com o botão direito e escolha **Parar**) ou troque para `DB_PORT=5433`. Se usa o IntelliJ, informe a porta nova a ele (passo 6 da seção do IntelliJ).
- **8080** (`APP_PORT`): a aplicação já está rodando em outro lugar, por exemplo no IntelliJ e no Docker ao mesmo tempo. Pare uma delas, ou troque para `APP_PORT=8081` e acesse http://localhost:8081.
- **5050** (`PGADMIN_PORT`): troque para `PGADMIN_PORT=5051` e acesse http://localhost:5051.

**A aplicação não abre no navegador logo depois do Passo 5**
Ela ainda está subindo. Acompanhe com `docker compose -f docker/docker-compose.yml logs -f app` e espere a linha `Started ControleEstoqueApplication`.

**O `ce-app` fica reiniciando ou o log mostra erro de conexão/senha com o banco**
- Confira se o `ce-postgres` está `Up (healthy)` com `docker compose -f docker/docker-compose.yml ps`.
- Se você trocou `DB_PASSWORD` no `.env` depois que o banco já tinha sido criado, a senha nova não vale. Volte o valor antigo ou apague os dados e comece do zero.

**`/actuator/health` mostra `DOWN`**
O banco não está acessível. Faça as mesmas verificações do item acima.

**IntelliJ: "release version 21 not supported" ou "invalid source release: 21"**
O projeto está usando um Java mais antigo. Refaça o passo 4 da seção do IntelliJ escolhendo o Java 21.

**Terminal com Maven: "release version 21 not supported"**
A variável `JAVA_HOME` do Windows aponta para outro Java (por exemplo, o 17). Aponte para o 21 antes de rodar, ajustando o caminho para onde o seu JDK 21 está instalado:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

**"Validate failed: Migrations have failed validation" ou "checksum mismatch"**
Alguém alterou uma migration que já tinha rodado no seu banco. Em ambiente local, a solução mais rápida é apagar os dados e começar do zero.

**A interface abre em branco ou com aparência antiga depois de atualizar o projeto**
O navegador guardou a versão anterior. Aperte **Ctrl + F5** para recarregar sem o cache.

**A bolinha no rodapé do menu está vermelha ("API sem resposta")**
A aplicação ou o banco não estão respondendo. Rode `docker compose -f docker/docker-compose.yml ps` e confira se `ce-app` e `ce-postgres` estão `Up (healthy)`.

**Swagger abre, mas a lista suspensa não mostra o que acabei de cadastrar**
Recarregue a página (F5). As listas são montadas quando a página carrega.

**O pgAdmin não mostra o servidor "Controle Estoque"**
O pgAdmin só cria o servidor automaticamente na primeira vez. Cadastre na mão: clique com o botão direito em **Servers > Register > Server**. Na aba **General**, coloque qualquer nome. Na aba **Connection**, use host `postgres`, porta `5432`, usuário `ce_user`, senha `ce_password`.

---

## Mac e Linux

Os passos são os mesmos, com duas diferenças:
- Use o **Terminal** no lugar do PowerShell.
- No Passo 4, o comando de copiar é `cp .env.example .env`, e para conferir é `cat .env`.

No Linux, instale o Docker Engine com o plugin `docker compose` em vez do Docker Desktop.
