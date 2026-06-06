-- Unidade table (must come before usuario due to FK)
CREATE TABLE unidade (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    endereco VARCHAR(300) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    estado CHAR(2) NOT NULL,
    cozinha_completa BOOLEAN NOT NULL DEFAULT TRUE,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Usuario table
CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    senha_hash VARCHAR(72) NOT NULL,
    telefone VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    unidade_id BIGINT REFERENCES unidade(id) ON DELETE RESTRICT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_role CHECK (role IN ('CLIENTE', 'ATENDENTE', 'GERENTE', 'ADMIN'))
);

-- Produto table
CREATE TABLE produto (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10,2) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    sazonal BOOLEAN NOT NULL DEFAULT FALSE,
    disponivel_de DATE,
    disponivel_ate DATE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_produto_preco CHECK (preco > 0)
);

-- Estoque por unidade
CREATE TABLE estoque_unidade (
    id BIGSERIAL PRIMARY KEY,
    unidade_id BIGINT NOT NULL REFERENCES unidade(id) ON DELETE RESTRICT,
    produto_id BIGINT NOT NULL REFERENCES produto(id) ON DELETE RESTRICT,
    quantidade INTEGER NOT NULL DEFAULT 0,
    estoque_minimo INTEGER NOT NULL DEFAULT 0,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_estoque_unidade_produto UNIQUE (unidade_id, produto_id),
    CONSTRAINT ck_estoque_quantidade CHECK (quantidade >= 0)
);

-- Pedido table
CREATE TABLE pedido (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    unidade_id BIGINT NOT NULL REFERENCES unidade(id) ON DELETE RESTRICT,
    canal_pedido VARCHAR(10) NOT NULL,
    status VARCHAR(25) NOT NULL DEFAULT 'AGUARDANDO_PAGAMENTO',
    valor_total DECIMAL(10,2) NOT NULL DEFAULT 0,
    observacao TEXT,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_pedido_canal CHECK (canal_pedido IN ('APP', 'TOTEM', 'BALCAO', 'PICKUP', 'WEB')),
    CONSTRAINT ck_pedido_status CHECK (status IN ('AGUARDANDO_PAGAMENTO', 'RECEBIDO', 'EM_PREPARACAO', 'PRONTO', 'ENTREGUE', 'CANCELADO'))
);

-- Item do pedido
CREATE TABLE item_pedido (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedido(id) ON DELETE CASCADE,
    produto_id BIGINT NOT NULL REFERENCES produto(id) ON DELETE RESTRICT,
    quantidade INTEGER NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    CONSTRAINT ck_item_quantidade CHECK (quantidade > 0)
);

-- Pagamento
CREATE TABLE pagamento (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedido(id) ON DELETE RESTRICT,
    forma_pagamento VARCHAR(20) NOT NULL,
    status VARCHAR(10) NOT NULL,
    transacao_externa_id VARCHAR(100),
    motivo_recusa VARCHAR(300),
    valor_pago DECIMAL(10,2) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_pagamento_pedido UNIQUE (pedido_id),
    CONSTRAINT ck_pagamento_forma CHECK (forma_pagamento IN ('CARTAO_CREDITO', 'CARTAO_DEBITO', 'PIX')),
    CONSTRAINT ck_pagamento_status CHECK (status IN ('APROVADO', 'RECUSADO'))
);

-- Consentimento de fidelidade (event-sourced)
CREATE TABLE fidelidade_consentimento (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    consentimento BOOLEAN NOT NULL,
    ip_origem VARCHAR(45),
    versao_termo VARCHAR(20) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Pontos de fidelidade
CREATE TABLE ponto_fidelidade (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    pedido_id BIGINT NOT NULL REFERENCES pedido(id) ON DELETE RESTRICT,
    pontos INTEGER NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_ponto_pedido UNIQUE (pedido_id),
    CONSTRAINT ck_ponto_valor CHECK (pontos > 0)
);

-- Log de auditoria
CREATE TABLE auditoria_log (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    acao VARCHAR(50) NOT NULL,
    entidade VARCHAR(50) NOT NULL,
    entidade_id BIGINT NOT NULL,
    dados_antes TEXT,
    dados_depois TEXT,
    motivo VARCHAR(500),
    ip_origem VARCHAR(45),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for common queries
CREATE INDEX idx_pedido_cliente ON pedido(cliente_id);
CREATE INDEX idx_pedido_unidade ON pedido(unidade_id);
CREATE INDEX idx_pedido_canal ON pedido(canal_pedido);
CREATE INDEX idx_pedido_status ON pedido(status);
CREATE INDEX idx_estoque_unidade ON estoque_unidade(unidade_id);
CREATE INDEX idx_ponto_cliente ON ponto_fidelidade(cliente_id);
CREATE INDEX idx_auditoria_entidade ON auditoria_log(entidade, entidade_id);
CREATE INDEX idx_consentimento_cliente ON fidelidade_consentimento(cliente_id);
