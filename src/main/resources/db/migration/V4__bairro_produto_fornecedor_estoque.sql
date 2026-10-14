-- Bairros (cada um pertence a uma cidade) e bairro opcional no endereço de cliente e fornecedor
CREATE TABLE bairro (
    id_bairro SERIAL PRIMARY KEY,
    nome      VARCHAR(100) NOT NULL,
    id_cidade INT NOT NULL,
    CONSTRAINT fk_bairro_cidade
        FOREIGN KEY (id_cidade) REFERENCES cidade (id_cidade)
);

ALTER TABLE cliente    ADD COLUMN id_bairro INT,
    ADD CONSTRAINT fk_cliente_bairro FOREIGN KEY (id_bairro) REFERENCES bairro (id_bairro);
ALTER TABLE fornecedor ADD COLUMN id_bairro INT,
    ADD CONSTRAINT fk_fornecedor_bairro FOREIGN KEY (id_bairro) REFERENCES bairro (id_bairro);

-- Fornecedores de cada produto, com o custo unitário de compra
CREATE TABLE produto_fornecedor (
    id_produto_fornecedor SERIAL PRIMARY KEY,
    id_produto            INT NOT NULL,
    id_fornecedor         INT NOT NULL,
    custo_unitario        NUMERIC(10, 2) NOT NULL CHECK (custo_unitario >= 0),
    CONSTRAINT fk_produto_fornecedor_produto
        FOREIGN KEY (id_produto) REFERENCES produto (id_produto),
    CONSTRAINT fk_produto_fornecedor_fornecedor
        FOREIGN KEY (id_fornecedor) REFERENCES fornecedor (id_fornecedor),
    CONSTRAINT uq_produto_fornecedor UNIQUE (id_produto, id_fornecedor)
);

-- Saldo decimal (kg, litros); passa a ser a soma dos saldos dos lotes
ALTER TABLE produto
    ALTER COLUMN quantidade_estoque DROP DEFAULT,
    ALTER COLUMN quantidade_estoque TYPE NUMERIC(10, 3) USING quantidade_estoque::numeric,
    ALTER COLUMN quantidade_estoque SET DEFAULT 0;

-- Lotes: cada entrada de estoque cria um lote com o que sobra dele em quantidade_atual
CREATE TABLE lote (
    id_lote            SERIAL PRIMARY KEY,
    id_produto         INT NOT NULL,
    id_fornecedor      INT,
    numero_lote        VARCHAR(50),
    data_entrada       TIMESTAMP WITH TIME ZONE NOT NULL,
    data_validade      DATE,
    quantidade_inicial NUMERIC(10, 3) NOT NULL CHECK (quantidade_inicial > 0),
    quantidade_atual   NUMERIC(10, 3) NOT NULL CHECK (quantidade_atual >= 0),
    custo_unitario     NUMERIC(10, 2) CHECK (custo_unitario >= 0),
    CONSTRAINT fk_lote_produto
        FOREIGN KEY (id_produto) REFERENCES produto (id_produto),
    CONSTRAINT fk_lote_fornecedor
        FOREIGN KEY (id_fornecedor) REFERENCES fornecedor (id_fornecedor)
);

CREATE INDEX ix_lote_produto_validade ON lote (id_produto, data_validade);

-- Histórico imutável de entradas e saídas, sempre ligado a um lote
CREATE TABLE movimentacao_estoque (
    id_movimentacao      SERIAL PRIMARY KEY,
    id_lote              INT NOT NULL,
    id_produto           INT NOT NULL,
    id_tipo_movimentacao INT NOT NULL,
    quantidade           NUMERIC(10, 3) NOT NULL CHECK (quantidade > 0),
    data_movimentacao    TIMESTAMP WITH TIME ZONE NOT NULL,
    observacao           VARCHAR(255),
    CONSTRAINT fk_movimentacao_lote
        FOREIGN KEY (id_lote) REFERENCES lote (id_lote),
    CONSTRAINT fk_movimentacao_produto
        FOREIGN KEY (id_produto) REFERENCES produto (id_produto),
    CONSTRAINT fk_movimentacao_tipo
        FOREIGN KEY (id_tipo_movimentacao) REFERENCES tipo_movimentacao_estoque (id_tipo_movimentacao)
);

CREATE INDEX ix_movimentacao_produto_data ON movimentacao_estoque (id_produto, data_movimentacao);
CREATE INDEX ix_movimentacao_lote ON movimentacao_estoque (id_lote);

-- Tipos de movimentação básicos (só os que ainda não existem com a mesma descrição)
INSERT INTO tipo_movimentacao_estoque (descricao, tipo)
SELECT v.descricao, v.tipo
FROM (VALUES
    ('Compra de fornecedor', 'ENTRADA'),
    ('Ajuste de inventário (entrada)', 'ENTRADA'),
    ('Consumo na produção', 'SAIDA'),
    ('Perda / vencimento', 'SAIDA'),
    ('Ajuste de inventário (saída)', 'SAIDA')
) AS v(descricao, tipo)
WHERE NOT EXISTS (SELECT 1 FROM tipo_movimentacao_estoque t WHERE t.descricao = v.descricao);

-- Saldo existente sem lote (só possível por edição manual) vira um lote sem validade,
-- para manter saldo do produto = soma dos lotes
INSERT INTO lote (id_produto, numero_lote, data_entrada, quantidade_inicial, quantidade_atual)
SELECT id_produto, 'SALDO-INICIAL', now(), quantidade_estoque, quantidade_estoque
FROM produto
WHERE quantidade_estoque > 0;
