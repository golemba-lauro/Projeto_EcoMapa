-- =====================================================================
-- EcoMapa — O destino certo para cada resíduo
-- AEP 2026.2 — UniCesumar — 4º semestre
-- Script de criação do banco de dados (etapa 10 do cronograma)
--
-- Execução:  mysql -u root -p < database/script.sql
-- =====================================================================

DROP DATABASE IF EXISTS ecomapa;
CREATE DATABASE ecomapa
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE ecomapa;

-- ---------------------------------------------------------------------
-- USUARIO
-- O campo "tipo" é a coluna discriminadora: guarda CIDADAO ou
-- ADMINISTRADOR e mapeia a herança Usuario -> Cidadao / Administrador
-- do diagrama de classes para uma única tabela.
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id     INT          NOT NULL AUTO_INCREMENT,
    nome   VARCHAR(100) NOT NULL,
    email  VARCHAR(150) NOT NULL,
    senha  VARCHAR(255) NOT NULL,
    tipo   VARCHAR(20)  NOT NULL,

    CONSTRAINT pk_usuario    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email)   -- R09: e-mail identifica o usuário
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- PONTO_COLETA
-- RN01: nome, endereco e horarioFuncionamento são obrigatórios.
-- RN06: status indica se o ponto está ativo (1) ou inativo (0).
-- ---------------------------------------------------------------------
CREATE TABLE ponto_coleta (
    id                   INT          NOT NULL AUTO_INCREMENT,
    nome                 VARCHAR(120) NOT NULL,
    endereco             VARCHAR(200) NOT NULL,
    telefone             VARCHAR(20)      NULL,
    horarioFuncionamento VARCHAR(100) NOT NULL,
    status               BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_ponto_coleta PRIMARY KEY (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- MATERIAL
-- RN02: o nome do material é único dentro do sistema.
-- ---------------------------------------------------------------------
CREATE TABLE material (
    id        INT          NOT NULL AUTO_INCREMENT,
    nome      VARCHAR(80)  NOT NULL,
    descricao VARCHAR(255)     NULL,
    categoria VARCHAR(50)      NULL,

    CONSTRAINT pk_material      PRIMARY KEY (id),
    CONSTRAINT uk_material_nome UNIQUE (nome)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- PONTO_MATERIAL
-- Tabela associativa que resolve o relacionamento N:N entre pontos de
-- coleta e materiais. A chave primária composta impede o vínculo
-- repetido do mesmo material em um ponto (R03).
-- R07: excluir um ponto remove seus vínculos (ON DELETE CASCADE).
-- ---------------------------------------------------------------------
CREATE TABLE ponto_material (
    idPontoColeta INT NOT NULL,
    idMaterial    INT NOT NULL,

    CONSTRAINT pk_ponto_material PRIMARY KEY (idPontoColeta, idMaterial),

    CONSTRAINT fk_ponto_material_ponto
        FOREIGN KEY (idPontoColeta) REFERENCES ponto_coleta (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_ponto_material_material
        FOREIGN KEY (idMaterial) REFERENCES material (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- FEEDBACK
-- RN07: as duas chaves estrangeiras são obrigatórias — um feedback
-- nunca existe sem um cidadão e sem um ponto de coleta.
-- R07: excluir o ponto remove seus feedbacks (ON DELETE CASCADE);
-- o usuário é protegido por RESTRICT para não apagar o autor por engano.
-- ---------------------------------------------------------------------
CREATE TABLE feedback (
    id            INT          NOT NULL AUTO_INCREMENT,
    descricao     VARCHAR(500) NOT NULL,
    data          DATE         NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    idUsuario     INT          NOT NULL,
    idPontoColeta INT          NOT NULL,

    CONSTRAINT pk_feedback PRIMARY KEY (id),

    CONSTRAINT fk_feedback_usuario
        FOREIGN KEY (idUsuario) REFERENCES usuario (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_feedback_ponto
        FOREIGN KEY (idPontoColeta) REFERENCES ponto_coleta (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE = InnoDB;
