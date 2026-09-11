-- =====================================================================
-- EcoMapa - O destino certo para cada residuo
-- Script de criacao do banco de dados
-- AEP 2026.2 - 4o semestre - ADS / Engenharia de Software - UniCesumar
-- SGBD: MySQL 8
-- =====================================================================

DROP DATABASE IF EXISTS ecomapa;

CREATE DATABASE ecomapa
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE ecomapa;

-- ---------------------------------------------------------------------
-- USUARIO
-- Mapeamento da heranca do diagrama de classes em tabela unica:
-- a coluna "tipo" e a coluna discriminadora entre Cidadao e Administrador.
-- Requisito R09 | Regras RN04 e RN05
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    nome   VARCHAR(100) NOT NULL,
    email  VARCHAR(120) NOT NULL UNIQUE,
    senha  VARCHAR(255) NOT NULL,
    tipo   ENUM('CIDADAO', 'ADMINISTRADOR') NOT NULL DEFAULT 'CIDADAO'
);

-- ---------------------------------------------------------------------
-- PONTO_COLETA  (entidade principal do CRUD)
-- Requisitos R01, R04, R05, R06, R07
-- RN01: nome, endereco e horario de funcionamento sao obrigatorios
-- RN06: pontos INATIVOS nao aparecem nas consultas do cidadao
-- ---------------------------------------------------------------------
CREATE TABLE ponto_coleta (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    nome                  VARCHAR(100) NOT NULL,
    tipo_estabelecimento  VARCHAR(60),
    logradouro            VARCHAR(120) NOT NULL,
    numero                VARCHAR(10),
    bairro                VARCHAR(60),
    cidade                VARCHAR(60),
    cep                   VARCHAR(9),
    telefone              VARCHAR(20),
    horario_funcionamento VARCHAR(100) NOT NULL,
    status                ENUM('ATIVO', 'INATIVO') NOT NULL DEFAULT 'ATIVO'
);

-- ---------------------------------------------------------------------
-- MATERIAL
-- Requisito R02 | RN02: nome unico no sistema
-- ---------------------------------------------------------------------
CREATE TABLE material (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    nome      VARCHAR(80) NOT NULL UNIQUE,
    descricao VARCHAR(255),
    categoria VARCHAR(60) NOT NULL
);

-- ---------------------------------------------------------------------
-- PONTO_MATERIAL  (tabela associativa - resolve o N:N)
-- Requisito R03 | RN03: todo ponto precisa de ao menos um material
-- A chave primaria composta impede o vinculo repetido do mesmo material.
-- ON DELETE CASCADE atende ao requisito R07.
-- ---------------------------------------------------------------------
CREATE TABLE ponto_material (
    id_ponto_coleta INT NOT NULL,
    id_material     INT NOT NULL,
    PRIMARY KEY (id_ponto_coleta, id_material),
    CONSTRAINT fk_pm_ponto
        FOREIGN KEY (id_ponto_coleta) REFERENCES ponto_coleta(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_pm_material
        FOREIGN KEY (id_material) REFERENCES material(id)
        ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- FEEDBACK
-- Requisito R08 | RN07: sempre vinculado a um cidadao e a um ponto
-- Composicao 1:N com ponto_coleta - o feedback nao existe sem o ponto,
-- por isso ON DELETE CASCADE.
-- ---------------------------------------------------------------------
CREATE TABLE feedback (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    descricao       VARCHAR(500) NOT NULL,
    data            DATE NOT NULL,
    status          ENUM('PENDENTE', 'ANALISADO') NOT NULL DEFAULT 'PENDENTE',
    id_usuario      INT NOT NULL,
    id_ponto_coleta INT NOT NULL,
    CONSTRAINT fk_fb_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_fb_ponto
        FOREIGN KEY (id_ponto_coleta) REFERENCES ponto_coleta(id)
        ON DELETE CASCADE
);

-- =====================================================================
-- DADOS DE EXEMPLO (para teste da aplicacao)
-- =====================================================================

INSERT INTO usuario (nome, email, senha, tipo) VALUES
    ('Administrador EcoMapa', 'admin@ecomapa.com', 'admin123', 'ADMINISTRADOR'),
    ('Maria Souza',           'maria@email.com',   'maria123', 'CIDADAO');

INSERT INTO material (nome, descricao, categoria) VALUES
    ('Pilhas e baterias',   'Pilhas comuns, alcalinas e baterias de celular', 'Residuo perigoso'),
    ('Eletronicos',         'Celulares, computadores, monitores e cabos',     'Eletroeletronico'),
    ('Oleo de cozinha',     'Oleo de fritura usado, em garrafa fechada',      'Residuo organico'),
    ('Papelao',             'Caixas e embalagens de papelao',                 'Reciclavel'),
    ('Vidro',               'Garrafas e potes de vidro',                       'Reciclavel'),
    ('Plastico',            'Garrafas PET e embalagens plasticas',             'Reciclavel'),
    ('Roupas',              'Roupas e calcados em condicoes de uso',           'Reutilizavel');

INSERT INTO ponto_coleta
    (nome, tipo_estabelecimento, logradouro, numero, bairro, cidade, cep, telefone, horario_funcionamento, status)
VALUES
    ('Ecoponto Zona 7',      'Ecoponto municipal', 'Avenida Tuiuti',      '1500', 'Zona 07',   'Maringa', '87020-000', '(44) 3000-0001', 'Segunda a sexta, 8h as 17h',  'ATIVO'),
    ('Supermercado Central', 'Comercio',           'Avenida Brasil',      '2300', 'Centro',    'Maringa', '87013-000', '(44) 3000-0002', 'Segunda a sabado, 9h as 20h', 'ATIVO'),
    ('Cooperativa Recicla',  'Cooperativa',        'Rua Santos Dumont',   '450',  'Zona 03',   'Maringa', '87050-000', '(44) 3000-0003', 'Segunda a sexta, 8h as 18h',  'ATIVO'),
    ('Posto Antigo Norte',   'Posto de coleta',    'Rua Neo Alves Martins','980', 'Zona 04',   'Maringa', '87014-000', '(44) 3000-0004', 'Encerrado',                    'INATIVO');

-- Vinculos entre pontos e materiais aceitos (R03)
INSERT INTO ponto_material (id_ponto_coleta, id_material) VALUES
    (1, 1), (1, 2), (1, 4), (1, 5), (1, 6),
    (2, 1), (2, 3),
    (3, 4), (3, 5), (3, 6), (3, 7),
    (4, 2);

INSERT INTO feedback (descricao, data, status, id_usuario, id_ponto_coleta) VALUES
    ('O horario informado esta desatualizado, fecha as 16h.', '2026-09-05', 'PENDENTE',  2, 1),
    ('Local bem sinalizado e de facil acesso.',               '2026-09-06', 'ANALISADO', 2, 3);

-- =====================================================================
-- CONSULTAS DE APOIO (base para os metodos do PontoColetaDAO)
-- =====================================================================

-- R04 - listar pontos ativos com os materiais aceitos
-- SELECT p.id, p.nome, p.logradouro, p.numero, p.bairro, p.cidade,
--        p.telefone, p.horario_funcionamento,
--        GROUP_CONCAT(m.nome ORDER BY m.nome SEPARATOR ', ') AS materiais
--   FROM ponto_coleta p
--   JOIN ponto_material pm ON pm.id_ponto_coleta = p.id
--   JOIN material m        ON m.id = pm.id_material
--  WHERE p.status = 'ATIVO'
--  GROUP BY p.id
--  ORDER BY p.nome;

-- R05 - pesquisar pontos ativos que aceitam determinado material
-- SELECT p.nome, p.logradouro, p.numero, p.bairro, p.horario_funcionamento
--   FROM ponto_coleta p
--   JOIN ponto_material pm ON pm.id_ponto_coleta = p.id
--   JOIN material m        ON m.id = pm.id_material
--  WHERE m.nome = ? AND p.status = 'ATIVO'
--  ORDER BY p.nome;
