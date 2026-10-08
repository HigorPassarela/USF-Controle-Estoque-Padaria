CREATE TABLE cidade (
    id_cidade   SERIAL PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    id_estado   INT NOT NULL,
    CONSTRAINT fk_cidade_estado
        FOREIGN KEY (id_estado) REFERENCES estado (id_estado)
);

CREATE TABLE cliente (
    id_cliente      SERIAL PRIMARY KEY,
    nome            VARCHAR(100) NOT NULL,
    cpf_cnpj        VARCHAR(20) NOT NULL,
    telefone        VARCHAR(20),
    email           VARCHAR(100),
    id_tipo_cliente INT NOT NULL,
    id_cidade       INT,
    CONSTRAINT fk_cliente_tipo
        FOREIGN KEY (id_tipo_cliente) REFERENCES tipo_cliente (id_tipo_cliente),
    CONSTRAINT fk_cliente_cidade
        FOREIGN KEY (id_cidade) REFERENCES cidade (id_cidade)
);

CREATE TABLE fornecedor (
    id_fornecedor SERIAL PRIMARY KEY,
    nome          VARCHAR(100) NOT NULL,
    cnpj          VARCHAR(20) NOT NULL,
    telefone      VARCHAR(20),
    email         VARCHAR(100),
    id_cidade     INT,
    CONSTRAINT fk_fornecedor_cidade
        FOREIGN KEY (id_cidade) REFERENCES cidade (id_cidade)
);

CREATE TABLE produto (
    id_produto           SERIAL PRIMARY KEY,
    nome                 VARCHAR(100) NOT NULL,
    preco_venda          NUMERIC(10, 2) NOT NULL,
    quantidade_estoque   INT NOT NULL DEFAULT 0,
    id_categoria_produto INT NOT NULL,
    id_tipo_produto      INT NOT NULL,
    id_unidade_medida    INT NOT NULL,
    id_fornecedor        INT,
    CONSTRAINT fk_produto_categoria
        FOREIGN KEY (id_categoria_produto) REFERENCES categoria_produto (id_categoria_produto),
    CONSTRAINT fk_produto_tipo
        FOREIGN KEY (id_tipo_produto) REFERENCES tipo_produto (id_tipo_produto),
    CONSTRAINT fk_produto_unidade
        FOREIGN KEY (id_unidade_medida) REFERENCES unidade_medida (id_unidade_medida),
    CONSTRAINT fk_produto_fornecedor
        FOREIGN KEY (id_fornecedor) REFERENCES fornecedor (id_fornecedor)
);