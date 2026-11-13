-- Marido de Aluguel
-- Script de criação do banco de dados (PostgreSQL 14+)
-- Única fonte da estrutura do banco: a aplicação usa spring.jpa.hibernate.ddl-auto=validate.
-- Parte do DER da 1ª entrega; colunas e tabelas acrescentadas estão marcadas com "[2º bim]"
-- e justificadas no README (seção "Ajustes em relação à 1ª entrega").

DROP TABLE IF EXISTS avaliacoes CASCADE;
DROP TABLE IF EXISTS ordem_status_historico CASCADE;
DROP TABLE IF EXISTS ordens_servico CASCADE;
DROP TABLE IF EXISTS servicos CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;
DROP FUNCTION IF EXISTS impedir_alteracao_avaliacao() CASCADE;

-- Tabela usuarios
-- Centraliza credenciais e dados pessoais. A coluna tipo_usuario é o discriminador da herança
-- Usuario -> Cliente / Prestador (JPA SINGLE_TABLE).
CREATE TABLE usuarios (
    id                     SERIAL PRIMARY KEY,
    nome                   VARCHAR(100) NOT NULL,
    email                  VARCHAR(100) NOT NULL UNIQUE,
    telefone               VARCHAR(20)  NOT NULL,
    senha_hash             VARCHAR(255) NOT NULL,
    endereco               VARCHAR(150),
    latitude               DOUBLE PRECISION NOT NULL CHECK (latitude BETWEEN -90 AND 90),     -- [2º bim] RF03 (Haversine)
    longitude              DOUBLE PRECISION NOT NULL CHECK (longitude BETWEEN -180 AND 180),  -- [2º bim] RF03 (Haversine)
    tipo_usuario           VARCHAR(20)  NOT NULL CHECK (tipo_usuario IN ('CLIENTE', 'PRESTADOR')),
    nota_media             NUMERIC(3,2) NOT NULL DEFAULT 0 CHECK (nota_media BETWEEN 0 AND 5),
    quantidade_avaliacoes  INTEGER      NOT NULL DEFAULT 0 CHECK (quantidade_avaliacoes >= 0) -- [2º bim] RF06 (recálculo da média)
);

-- Tabela servicos
-- Catálogo de cada prestador. tipo_cobranca é a contrapartida relacional do polimorfismo: decide se a
-- aplicação instancia ServicoPorHora ou ServicoPorEmpreitada (ambas implementam a interface Servico).
-- ON DELETE CASCADE em prestador_id: o serviço não existe sem o prestador que o oferece.
CREATE TABLE servicos (
    id                   SERIAL PRIMARY KEY,
    prestador_id         INTEGER       NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    categoria            VARCHAR(50)   NOT NULL,
    descricao            TEXT          NOT NULL,                                                      -- [2º bim] RF02
    raio_atendimento_km  INTEGER       NOT NULL CHECK (raio_atendimento_km BETWEEN 1 AND 200),       -- [2º bim] RF02
    tipo_cobranca        VARCHAR(20)   NOT NULL CHECK (tipo_cobranca IN ('POR_HORA', 'POR_EMPREITADA')),
    valor_base           NUMERIC(10,2) NOT NULL CHECK (valor_base > 0)
);

-- Tabela ordens_servico
-- Materializa a contratação, ligando um cliente a uma oferta de serviço.
CREATE TABLE ordens_servico (
    id              SERIAL PRIMARY KEY,
    cliente_id      INTEGER       NOT NULL REFERENCES usuarios(id),
    servico_id      INTEGER       NOT NULL REFERENCES servicos(id),
    data_abertura   DATE          NOT NULL DEFAULT CURRENT_DATE,
    status          VARCHAR(20)   NOT NULL DEFAULT 'ABERTA'
                    CHECK (status IN ('ABERTA', 'ACEITA', 'EM_EXECUCAO', 'CONCLUIDA', 'CANCELADA')),
    horas_estimadas NUMERIC(5,2)  CHECK (horas_estimadas > 0),   -- [2º bim] base do orçamento por hora
    valor_final     NUMERIC(10,2) CHECK (valor_final >= 0)
);

-- Tabela ordem_status_historico  [2º bim]
-- RF05: cada mudança de status fica registrada com data e hora.
CREATE TABLE ordem_status_historico (
    id         SERIAL PRIMARY KEY,
    ordem_id   INTEGER     NOT NULL REFERENCES ordens_servico(id) ON DELETE CASCADE,
    status     VARCHAR(20) NOT NULL
               CHECK (status IN ('ABERTA', 'ACEITA', 'EM_EXECUCAO', 'CONCLUIDA', 'CANCELADA')),
    data_hora  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabela avaliacoes
-- Reputação verificável. UNIQUE em ordem_id: no máximo uma avaliação por ordem.
-- ON DELETE CASCADE: o ciclo de vida da avaliação depende da ordem (composição OrdemServico <>-- Avaliacao).
CREATE TABLE avaliacoes (
    id             SERIAL PRIMARY KEY,
    ordem_id       INTEGER NOT NULL UNIQUE REFERENCES ordens_servico(id) ON DELETE CASCADE,
    nota           INTEGER NOT NULL CHECK (nota BETWEEN 1 AND 5),
    comentario     TEXT,
    data_avaliacao  DATE    NOT NULL DEFAULT CURRENT_DATE
);

-- RNF04: a avaliação é imutável após o envio. Última linha de defesa, além da regra no Java.
CREATE FUNCTION impedir_alteracao_avaliacao() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Avaliações são imutáveis após o envio (RNF04)';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_avaliacoes_imutaveis
    BEFORE UPDATE ON avaliacoes
    FOR EACH ROW EXECUTE FUNCTION impedir_alteracao_avaliacao();

-- Índices de apoio às buscas (RF03 / RNF01: categoria, preço, nota e distância)
CREATE INDEX idx_servicos_categoria_valor ON servicos(categoria, valor_base);
CREATE INDEX idx_servicos_valor_base      ON servicos(valor_base);
CREATE INDEX idx_servicos_prestador       ON servicos(prestador_id);
CREATE INDEX idx_usuarios_nota_media      ON usuarios(nota_media) WHERE tipo_usuario = 'PRESTADOR';
CREATE INDEX idx_ordens_cliente           ON ordens_servico(cliente_id);
CREATE INDEX idx_ordens_servico           ON ordens_servico(servico_id);
CREATE INDEX idx_ordens_status            ON ordens_servico(status);
CREATE INDEX idx_historico_ordem          ON ordem_status_historico(ordem_id);
