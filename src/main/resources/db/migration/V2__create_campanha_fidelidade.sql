CREATE TABLE campanha_fidelidade (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    multiplicador DECIMAL(4,2) NOT NULL DEFAULT 1.0,
    inicio TIMESTAMP NOT NULL,
    fim TIMESTAMP NOT NULL,
    unidade_id BIGINT REFERENCES unidade(id) ON DELETE CASCADE,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_campanha_multiplicador CHECK (multiplicador > 0)
);

CREATE INDEX idx_campanha_periodo ON campanha_fidelidade(inicio, fim);
CREATE INDEX idx_campanha_unidade ON campanha_fidelidade(unidade_id);
