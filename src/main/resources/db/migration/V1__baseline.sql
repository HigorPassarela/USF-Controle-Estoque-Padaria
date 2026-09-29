-- Migration inicial (marcador). As tabelas do modelo serão criadas nas próximas migrations (V2, V3, ...).

CREATE TABLE estado (
    id_estado SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    uf VARCHAR(2) NOT NULL
);

CREATE TABLE tipo_cliente (
    id_tipo_cliente SERIAL PRIMARY KEY,
    descricao VARCHAR(100) NOT NULL
);

CREATE TABLE tipo_produto (
    id_tipo_produto SERIAL PRIMARY KEY,
    descricao VARCHAR(100) NOT NULL
);

CREATE TABLE categoria_produto (
    id_categoria_produto SERIAL PRIMARY KEY,
    descricao VARCHAR(100) NOT NULL
);

CREATE TABLE unidade_medida (
    id_unidade_medida SERIAL PRIMARY KEY,
    descricao VARCHAR(100) NOT NULL,
    sigla VARCHAR(10) NOT NULL
);

CREATE TABLE metodo_pagamento (
    id_metodo_pagamento SERIAL PRIMARY KEY,
    descricao VARCHAR(100) NOT NULL
);

CREATE TABLE tipo_movimentacao_estoque (
    id_tipo_movimentacao SERIAL PRIMARY KEY,
    descricao VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL
);