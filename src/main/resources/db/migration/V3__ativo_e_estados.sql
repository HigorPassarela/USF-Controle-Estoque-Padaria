-- Exclusão lógica em cliente, fornecedor e produto
ALTER TABLE cliente    ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE fornecedor ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE produto    ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;

-- Documento único apenas entre registros ativos (permite recadastrar o documento de um inativo)
CREATE UNIQUE INDEX uq_cliente_cpf_cnpj_ativo ON cliente (cpf_cnpj) WHERE ativo;
CREATE UNIQUE INDEX uq_fornecedor_cnpj_ativo ON fornecedor (cnpj) WHERE ativo;

-- Estados brasileiros (dados de referência; sem CRUD na API)
INSERT INTO estado (nome, uf)
SELECT v.nome, v.uf
FROM (VALUES
    ('Acre', 'AC'),
    ('Alagoas', 'AL'),
    ('Amapá', 'AP'),
    ('Amazonas', 'AM'),
    ('Bahia', 'BA'),
    ('Ceará', 'CE'),
    ('Distrito Federal', 'DF'),
    ('Espírito Santo', 'ES'),
    ('Goiás', 'GO'),
    ('Maranhão', 'MA'),
    ('Mato Grosso', 'MT'),
    ('Mato Grosso do Sul', 'MS'),
    ('Minas Gerais', 'MG'),
    ('Pará', 'PA'),
    ('Paraíba', 'PB'),
    ('Paraná', 'PR'),
    ('Pernambuco', 'PE'),
    ('Piauí', 'PI'),
    ('Rio de Janeiro', 'RJ'),
    ('Rio Grande do Norte', 'RN'),
    ('Rio Grande do Sul', 'RS'),
    ('Rondônia', 'RO'),
    ('Roraima', 'RR'),
    ('Santa Catarina', 'SC'),
    ('São Paulo', 'SP'),
    ('Sergipe', 'SE'),
    ('Tocantins', 'TO')
) AS v(nome, uf)
WHERE NOT EXISTS (SELECT 1 FROM estado e WHERE e.uf = v.uf);
