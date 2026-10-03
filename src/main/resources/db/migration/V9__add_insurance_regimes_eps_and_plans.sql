-- HU-006/HU-007 (DEC-009): tablas tomadas tal cual de database/reference/db.sql.
-- insurance_regimes es catálogo fijo (RF-05); eps y eps_plans son catálogos
-- configurables por ADMIN (RF-06) — nunca se borran físicamente, solo se desactivan.

CREATE TABLE insurance_regimes (
    id SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(80) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE eps (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE eps_plans (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    eps_id BIGINT UNSIGNED NOT NULL,
    regime_id SMALLINT UNSIGNED NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_eps_plan_code UNIQUE (eps_id, code),
    CONSTRAINT fk_eps_plans_eps FOREIGN KEY (eps_id) REFERENCES eps(id) ON DELETE RESTRICT,
    CONSTRAINT fk_eps_plans_regime FOREIGN KEY (regime_id) REFERENCES insurance_regimes(id) ON DELETE RESTRICT,
    INDEX ix_eps_plans_regime (regime_id)
) ENGINE=InnoDB;

INSERT INTO insurance_regimes (id, code, name) VALUES
    (1, 'CONTRIBUTIVO', 'Contributivo'),
    (2, 'SUBSIDIADO', 'Subsidiado'),
    (3, 'ESPECIAL', 'Especial'),
    (4, 'EXCEPCION', 'Excepción'),
    (5, 'PARTICULAR', 'Particular');

INSERT INTO eps (id, code, name, active) VALUES
    (1, 'EPS_DEMO_A', 'EPS Demo Salud', TRUE),
    (2, 'EPS_DEMO_B', 'EPS Demo Familiar', TRUE),
    (3, 'PARTICULAR_DEMO', 'Atención Particular Demo', TRUE);

INSERT INTO eps_plans (id, eps_id, regime_id, code, name, active) VALUES
    (1, 1, 1, 'A-CONTRIB', 'Plan Contributivo Demo', TRUE),
    (2, 1, 2, 'A-SUBS', 'Plan Subsidiado Demo', TRUE),
    (3, 2, 1, 'B-CONTRIB', 'Plan Contributivo Familiar Demo', TRUE),
    (4, 2, 3, 'B-ESPECIAL', 'Plan Especial Demo', TRUE),
    (5, 3, 5, 'PARTICULAR', 'Particular / pago directo', TRUE);
