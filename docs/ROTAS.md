# Mapa de rotas da API

Todas as rotas do projeto: **70 rotas de negócio** em `/api` e **9 rotas de infraestrutura** (interface web, documentação e health check).

Endereço base, com a aplicação rodando localmente: `http://localhost:8080`

> Para usar o sistema, abra a interface em http://localhost:8080. Para chamar qualquer rota pelo navegador, use o **Explorador da API** da interface ou o Swagger em http://localhost:8080/swagger-ui.html. Para subir o projeto, veja [COMO-RODAR.md](COMO-RODAR.md).

---

## Como ler estas tabelas

**Como os dados são enviados.** Nos `POST` e `PUT`, os campos vão como **parâmetros na URL** (query string), não como corpo JSON:

```
POST /api/produtos?nome=Pão francês&precoVenda=0.75&categoria=1&tipoProduto=1&unidadeMedida=2
```

**Campos que apontam para outro cadastro** (`categoria`, `cidade`, `produto`, `fornecedor`...) e os ids na rota (`{id}`, `{idFornecedor}`, `{idProduto}`) aceitam:
- só o número: `3`
- ou o formato das listas do Swagger: `3 - Pães` (a API lê o número do começo)

**Números decimais** usam ponto: `0.75`, `2.5`. Quantidades de estoque aceitam até 3 casas; preços e custos, até 2.

**Datas** usam o formato `AAAA-MM-DD`, por exemplo `2026-12-31`.

**Nas colunas de parâmetros**, os obrigatórios estão em **negrito**. Parâmetros entre `{chaves}` fazem parte da rota.

**Códigos de resposta:**

| Código | Significado |
|--------|-------------|
| 200 | Deu certo e a resposta traz os dados |
| 201 | Registro criado; o cabeçalho `Location` aponta para ele (quando indicado) |
| 204 | Deu certo, sem corpo na resposta (exclusões) |
| 400 | Dado inválido: campo obrigatório vazio, formato errado, regra de campo |
| 404 | Registro não existe (ou está inativo, nos cadastros com exclusão lógica) |
| 405 | Método não permitido nessa rota |
| 409 | Conflito com uma regra: registro em uso, duplicado, estoque insuficiente |
| 500 | Erro inesperado no servidor (sem detalhes internos na resposta) |

**Formato de todo erro:**

```json
{
  "status": 400,
  "erro": "Requisição inválida",
  "mensagem": "Um ou mais campos são inválidos",
  "caminho": "/api/produtos",
  "timestamp": "2026-10-14T21:50:38Z",
  "campos": [{ "campo": "precoVenda", "mensagem": "O preço de venda deve ser maior que zero" }]
}
```

A lista `campos` só aparece quando o erro é de um campo específico.

---

## Resumo por grupo

| Grupo | Rota base | Rotas | Exclusão |
|-------|-----------|------:|----------|
| [Estoque](#estoque) | `/api/estoque` | 9 | Movimentações não são editáveis nem excluíveis |
| [Produtos](#produtos) | `/api/produtos` | 5 | Lógica (fica inativo) |
| [Fornecedores do Produto](#fornecedores-do-produto) | `/api/produtos/{id}/fornecedores` | 4 | Física (desvincula) |
| [Clientes](#clientes) | `/api/clientes` | 5 | Lógica (fica inativo) |
| [Fornecedores](#fornecedores) | `/api/fornecedores` | 5 | Lógica (fica inativo) |
| [Categorias de Produto](#categorias-de-produto) | `/api/categorias-produto` | 5 | Física; 409 se em uso |
| [Tipos de Produto](#tipos-de-produto) | `/api/tipos-produto` | 5 | Física; 409 se em uso |
| [Unidades de Medida](#unidades-de-medida) | `/api/unidades-medida` | 5 | Física; 409 se em uso |
| [Tipos de Movimentação de Estoque](#tipos-de-movimentação-de-estoque) | `/api/tipos-movimentacao` | 5 | Física; 409 se em uso |
| [Métodos de Pagamento](#métodos-de-pagamento) | `/api/metodos-pagamento` | 5 | Física; 409 se em uso |
| [Tipos de Cliente](#tipos-de-cliente) | `/api/tipos-cliente` | 5 | Física; 409 se em uso |
| [Estados](#estados) | `/api/estados` | 2 | Somente leitura |
| [Cidades](#cidades) | `/api/cidades` | 5 | Física; 409 se em uso |
| [Bairros](#bairros) | `/api/bairros` | 5 | Física; 409 se em uso |
| [Infraestrutura](#infraestrutura) | `/`, `/actuator`, `/swagger-ui`, `/v3/api-docs` | 9 | |
| **Total** | | **79** | |

---

## Estoque

Estoque por lote. Cada entrada cria um lote; as saídas comuns usam **FEFO** (o lote que vence primeiro sai primeiro) e **não consomem lotes vencidos**. O saldo do produto é sempre a soma dos saldos dos seus lotes.

| Método | Rota | O que faz | Parâmetros | Sucesso | Erros |
|--------|------|-----------|------------|---------|-------|
| POST | `/api/estoque/entradas` | Registra uma entrada: cria um lote, grava a movimentação de entrada e soma ao saldo do produto | **`produto`**, **`quantidade`**, **`tipoMovimentacao`** (de ENTRADA), `dataValidade`, `fornecedor`, `custoUnitario`, `numeroLote`, `observacao` | 201 + `Location` do lote | 400, 404 |
| POST | `/api/estoque/saidas` | Dá baixa pelo FEFO, passando por quantos lotes forem necessários, sem tocar em lote vencido | **`produto`**, **`quantidade`**, **`tipoMovimentacao`** (de SAIDA), `observacao` | 201 | 400, 404, 409 |
| POST | `/api/estoque/lotes/{id}/saidas` | Dá baixa em um lote específico, inclusive vencido (perda, descarte) | **`{id}`** do lote, **`quantidade`**, **`tipoMovimentacao`** (de SAIDA), `observacao` | 201 | 400, 404, 409 |
| GET | `/api/estoque/lotes` | Lista lotes, do que vence antes para o que vence depois (sem validade por último) | `produto`, `somenteComSaldo` (true/false), `somenteVencidos` (true/false) | 200 | |
| GET | `/api/estoque/lotes/{id}` | Busca um lote, com saldo atual e se está vencido | **`{id}`** do lote | 200 | 404 |
| GET | `/api/estoque/movimentacoes` | Histórico de entradas e saídas, da mais recente para a mais antiga | `produto`, `lote`, `de` (data), `ate` (data) | 200 | 400 |
| GET | `/api/estoque/movimentacoes/{id}` | Busca uma movimentação (`PUT` e `DELETE` aqui respondem 405) | **`{id}`** da movimentação | 200 | 404 |
| GET | `/api/estoque/saldos` | Saldo de cada produto ativo: total, disponível, vencido e próximo vencimento | | 200 | |
| GET | `/api/estoque/saldos/{idProduto}` | Saldo de um produto | **`{idProduto}`** | 200 | 404 |

**Regras do estoque:**
- `quantidade` deve ser maior que zero, com até 3 casas decimais. Caso contrário, 400.
- `dataValidade` não pode ser anterior a hoje (fuso de Brasília). Sem validade, o lote nunca vence.
- `tipoMovimentacao` precisa ser de `ENTRADA` nas entradas e de `SAIDA` nas saídas. Caso contrário, 400.
- Na entrada, se `custoUnitario` ficar vazio e o `fornecedor` estiver vinculado ao produto, o lote usa o custo do vínculo.
- **Lote vencido** é o que tem validade antes de hoje. No dia da validade, o lote ainda vale.
- Saída maior que o disponível (fora vencidos): **409** com a quantidade disponível na mensagem. Nada é alterado.
- Saída de lote maior que o saldo do lote: **409**.
- Produto inativo não recebe entrada nem saída comum (404).

**Exemplo de resposta de `GET /api/estoque/saldos/1`:**

```json
{ "idProduto": 1, "nomeProduto": "Leite", "siglaUnidadeMedida": "L",
  "saldoTotal": 40.5, "saldoDisponivel": 32.5, "saldoVencido": 8.0,
  "proximoVencimento": "2026-10-16" }
```

---

## Produtos

O saldo (`quantidadeEstoque`) não é editável aqui: nasce em 0 e só muda pelas rotas de [Estoque](#estoque).

| Método | Rota | O que faz | Parâmetros | Sucesso | Erros |
|--------|------|-----------|------------|---------|-------|
| GET | `/api/produtos` | Lista os produtos ativos | | 200 | |
| GET | `/api/produtos/{id}` | Busca um produto ativo | **`{id}`** | 200 | 404 |
| POST | `/api/produtos` | Cadastra um produto (nasce ativo, com saldo 0) | **`nome`**, **`precoVenda`**, **`categoria`**, **`tipoProduto`**, **`unidadeMedida`**, `fornecedor` | 201 + `Location` | 400, 404 |
| PUT | `/api/produtos/{id}` | Substitui os dados do produto (não altera o saldo) | **`{id}`**, **`nome`**, **`precoVenda`**, **`categoria`**, **`tipoProduto`**, **`unidadeMedida`**, `fornecedor` | 200 | 400, 404 |
| DELETE | `/api/produtos/{id}` | Inativa o produto (some das listas e buscas) | **`{id}`** | 204 | 404 |

- `precoVenda` deve ser maior que zero.
- `fornecedor`, se informado, precisa estar ativo.
- No `PUT`, campo opcional deixado vazio é apagado.
- A resposta traz `idCategoriaProduto`, `nomeCategoria`, `idTipoProduto`, `descricaoTipoProduto`, `idUnidadeMedida`, `nomeUnidadeMedida` (a sigla), `idFornecedor` e `nomeFornecedor`.

---

## Fornecedores do Produto

Quais fornecedores fornecem cada produto e por qual custo. O custo é usado como padrão nas entradas de estoque.

| Método | Rota | O que faz | Parâmetros | Sucesso | Erros |
|--------|------|-----------|------------|---------|-------|
| GET | `/api/produtos/{id}/fornecedores` | Lista os fornecedores ativos do produto, com o custo de cada um | **`{id}`** do produto | 200 | 404 |
| POST | `/api/produtos/{id}/fornecedores` | Vincula um fornecedor ao produto | **`{id}`** do produto, **`fornecedor`**, **`custoUnitario`** | 201 + `Location` | 400, 404, 409 |
| PUT | `/api/produtos/{id}/fornecedores/{idFornecedor}` | Altera o custo de um fornecedor já vinculado | **`{id}`**, **`{idFornecedor}`**, **`custoUnitario`** | 200 | 400, 404 |
| DELETE | `/api/produtos/{id}/fornecedores/{idFornecedor}` | Desvincula (lotes já recebidos não mudam) | **`{id}`**, **`{idFornecedor}`** | 204 | 404 |

- `custoUnitario` deve ser zero ou mais, com até 2 casas.
- Vincular o mesmo fornecedor duas vezes dá 409. Fornecedor inativo dá 404.

---

## Clientes

| Método | Rota | O que faz | Parâmetros | Sucesso | Erros |
|--------|------|-----------|------------|---------|-------|
| GET | `/api/clientes` | Lista os clientes ativos | | 200 | |
| GET | `/api/clientes/{id}` | Busca um cliente ativo | **`{id}`** | 200 | 404 |
| POST | `/api/clientes` | Cadastra um cliente (nasce ativo) | **`nome`**, **`cpfCnpj`**, **`tipoCliente`**, `telefone`, `email`, `cidade`, `bairro` | 201 + `Location` | 400, 404, 409 |
| PUT | `/api/clientes/{id}` | Substitui os dados do cliente | **`{id}`**, **`nome`**, **`cpfCnpj`**, **`tipoCliente`**, `telefone`, `email`, `cidade`, `bairro` | 200 | 400, 404, 409 |
| DELETE | `/api/clientes/{id}` | Inativa o cliente | **`{id}`** | 204 | 404 |

- `cpfCnpj` é único entre clientes ativos (409). O documento de um cliente inativo pode ser reaproveitado.
- `email`, se informado, precisa ter formato válido.
- `bairro`, se informado, exige `cidade` e precisa pertencer a ela (400).
- A resposta traz `idTipoCliente`, `descricaoTipoCliente`, `idCidade`, `nomeCidade`, `idBairro` e `nomeBairro`.

---

## Fornecedores

| Método | Rota | O que faz | Parâmetros | Sucesso | Erros |
|--------|------|-----------|------------|---------|-------|
| GET | `/api/fornecedores` | Lista os fornecedores ativos | | 200 | |
| GET | `/api/fornecedores/{id}` | Busca um fornecedor ativo | **`{id}`** | 200 | 404 |
| POST | `/api/fornecedores` | Cadastra um fornecedor (nasce ativo) | **`nome`**, **`cnpj`**, `telefone`, `email`, `cidade`, `bairro` | 201 + `Location` | 400, 404, 409 |
| PUT | `/api/fornecedores/{id}` | Substitui os dados do fornecedor | **`{id}`**, **`nome`**, **`cnpj`**, `telefone`, `email`, `cidade`, `bairro` | 200 | 400, 404, 409 |
| DELETE | `/api/fornecedores/{id}` | Inativa o fornecedor | **`{id}`** | 204 | 404 |

- `cnpj` é único entre fornecedores ativos (409).
- `bairro`, se informado, exige `cidade` e precisa pertencer a ela (400).
- A resposta traz `idCidade`, `nomeCidade`, `idBairro` e `nomeBairro`.

---

## Categorias de Produto

| Método | Rota | O que faz | Parâmetros | Sucesso | Erros |
|--------|------|-----------|------------|---------|-------|
| GET | `/api/categorias-produto` | Lista as categorias | | 200 | |
| GET | `/api/categorias-produto/{id}` | Busca uma categoria | **`{id}`** | 200 | 404 |
| POST | `/api/categorias-produto` | Cadastra uma categoria | **`descricao`** (até 100 caracteres) | 201 + `Location` | 400 |
| PUT | `/api/categorias-produto/{id}` | Altera uma categoria | **`{id}`**, **`descricao`** | 200 | 400, 404 |
| DELETE | `/api/categorias-produto/{id}` | Exclui uma categoria | **`{id}`** | 204 | 404, 409 (usada por produto) |

---

## Tipos de Produto

| Método | Rota | O que faz | Parâmetros | Sucesso | Erros |
|--------|------|-----------|------------|---------|-------|
| GET | `/api/tipos-produto` | Lista os tipos de produto | | 200 | |
| GET | `/api/tipos-produto/{id}` | Busca um tipo de produto | **`{id}`** | 200 | 404 |
| POST | `/api/tipos-produto` | Cadastra um tipo de produto | **`descricao`** (até 100 caracteres) | 201 + `Location` | 400 |
| PUT | `/api/tipos-produto/{id}` | Altera um tipo de produto | **`{id}`**, **`descricao`** | 200 | 400, 404 |
| DELETE | `/api/tipos-produto/{id}` | Exclui um tipo de produto | **`{id}`** | 204 | 404, 409 (usado por produto) |

---

## Unidades de Medida

| Método | Rota | O que faz | Parâmetros | Sucesso | Erros |
|--------|------|-----------|------------|---------|-------|
| GET | `/api/unidades-medida` | Lista as unidades de medida | | 200 | |
| GET | `/api/unidades-medida/{id}` | Busca uma unidade de medida | **`{id}`** | 200 | 404 |
| POST | `/api/unidades-medida` | Cadastra uma unidade de medida | **`descricao`** (até 100), **`sigla`** (até 10) | 201 + `Location` | 400 |
| PUT | `/api/unidades-medida/{id}` | Altera uma unidade de medida | **`{id}`**, **`descricao`**, **`sigla`** | 200 | 400, 404 |
| DELETE | `/api/unidades-medida/{id}` | Exclui uma unidade de medida | **`{id}`** | 204 | 404, 409 (usada por produto) |

---

## Tipos de Movimentação de Estoque

A migration já cadastra cinco tipos: "Compra de fornecedor" e "Ajuste de inventário (entrada)" como `ENTRADA`; "Consumo na produção", "Perda / vencimento" e "Ajuste de inventário (saída)" como `SAIDA`.

| Método | Rota | O que faz | Parâmetros | Sucesso | Erros |
|--------|------|-----------|------------|---------|-------|
| GET | `/api/tipos-movimentacao` | Lista os tipos de movimentação | | 200 | |
| GET | `/api/tipos-movimentacao/{id}` | Busca um tipo de movimentação | **`{id}`** | 200 | 404 |
| POST | `/api/tipos-movimentacao` | Cadastra um tipo de movimentação | **`descricao`** (até 100), **`tipo`** (`ENTRADA` ou `SAIDA`) | 201 + `Location` | 400 |
| PUT | `/api/tipos-movimentacao/{id}` | Altera um tipo de movimentação | **`{id}`**, **`descricao`**, **`tipo`** | 200 | 400, 404, 409 |
| DELETE | `/api/tipos-movimentacao/{id}` | Exclui um tipo de movimentação | **`{id}`** | 204 | 404, 409 (já tem movimentações) |

- Um tipo que já tem movimentações **não pode trocar de natureza** (`ENTRADA` para `SAIDA` ou o contrário): 409. A descrição continua editável.

---

## Métodos de Pagamento

| Método | Rota | O que faz | Parâmetros | Sucesso | Erros |
|--------|------|-----------|------------|---------|-------|
| GET | `/api/metodos-pagamento` | Lista os métodos de pagamento | | 200 | |
| GET | `/api/metodos-pagamento/{id}` | Busca um método de pagamento | **`{id}`** | 200 | 404 |
| POST | `/api/metodos-pagamento` | Cadastra um método de pagamento | **`descricao`** (até 100 caracteres) | 201 + `Location` | 400 |
| PUT | `/api/metodos-pagamento/{id}` | Altera um método de pagamento | **`{id}`**, **`descricao`** | 200 | 400, 404 |
| DELETE | `/api/metodos-pagamento/{id}` | Exclui um método de pagamento | **`{id}`** | 204 | 404, 409 |

---

## Tipos de Cliente

| Método | Rota | O que faz | Parâmetros | Sucesso | Erros |
|--------|------|-----------|------------|---------|-------|
| GET | `/api/tipos-cliente` | Lista os tipos de cliente | | 200 | |
| GET | `/api/tipos-cliente/{id}` | Busca um tipo de cliente | **`{id}`** | 200 | 404 |
| POST | `/api/tipos-cliente` | Cadastra um tipo de cliente | **`descricao`** (até 100 caracteres) | 201 + `Location` | 400 |
| PUT | `/api/tipos-cliente/{id}` | Altera um tipo de cliente | **`{id}`**, **`descricao`** | 200 | 400, 404 |
| DELETE | `/api/tipos-cliente/{id}` | Exclui um tipo de cliente | **`{id}`** | 204 | 404, 409 (usado por cliente) |

---

## Estados

Os 27 estados brasileiros, carregados pela migration. Somente leitura: `POST`, `PUT` e `DELETE` respondem 405.

| Método | Rota | O que faz | Parâmetros | Sucesso | Erros |
|--------|------|-----------|------------|---------|-------|
| GET | `/api/estados` | Lista os 27 estados, ordenados por nome | | 200 | |
| GET | `/api/estados/{id}` | Busca um estado (nome e UF) | **`{id}`** | 200 | 404 |

---

## Cidades

| Método | Rota | O que faz | Parâmetros | Sucesso | Erros |
|--------|------|-----------|------------|---------|-------|
| GET | `/api/cidades` | Lista as cidades | | 200 | |
| GET | `/api/cidades/{id}` | Busca uma cidade (com estado e UF) | **`{id}`** | 200 | 404 |
| POST | `/api/cidades` | Cadastra uma cidade | **`nome`** (até 100), **`estado`** | 201 + `Location` | 400, 404 |
| PUT | `/api/cidades/{id}` | Altera uma cidade | **`{id}`**, **`nome`**, **`estado`** | 200 | 400, 404 |
| DELETE | `/api/cidades/{id}` | Exclui uma cidade | **`{id}`** | 204 | 404, 409 (tem bairros, clientes ou fornecedores) |

---

## Bairros

| Método | Rota | O que faz | Parâmetros | Sucesso | Erros |
|--------|------|-----------|------------|---------|-------|
| GET | `/api/bairros` | Lista os bairros | | 200 | |
| GET | `/api/bairros/{id}` | Busca um bairro (com cidade e UF) | **`{id}`** | 200 | 404 |
| POST | `/api/bairros` | Cadastra um bairro | **`nome`** (até 100), **`cidade`** | 201 + `Location` | 400, 404 |
| PUT | `/api/bairros/{id}` | Altera um bairro | **`{id}`**, **`nome`**, **`cidade`** | 200 | 400, 404 |
| DELETE | `/api/bairros/{id}` | Exclui um bairro | **`{id}`** | 204 | 404, 409 (usado por cliente ou fornecedor) |

---

## Infraestrutura

Rotas que não são de negócio: interface, documentação e saúde da aplicação.

| Método | Rota | O que faz |
|--------|------|-----------|
| GET | `/` | Interface web de administração (`index.html`, com estilos e scripts em `/assets/...`) |
| GET | `/swagger-ui.html` | Abre o Swagger UI (redireciona para `/swagger-ui/index.html`) |
| GET | `/swagger-ui/index.html` | Página do Swagger UI, onde dá para testar todas as rotas pelo navegador |
| GET | `/v3/api-docs` | Documentação OpenAPI em JSON (o que o Swagger lê) |
| GET | `/v3/api-docs/swagger-config` | Configuração usada pelo Swagger UI |
| GET | `/actuator` | Lista as rotas do Actuator disponíveis |
| GET | `/actuator/health` | Saúde da aplicação e do banco (`UP` ou `DOWN`, com detalhes) |
| GET | `/actuator/health/{*path}` | Saúde de um componente específico, ex.: `/actuator/health/db` |
| GET | `/actuator/info` | Informações da aplicação |
