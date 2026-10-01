-- =============================================
-- Script de creación de tablas — PostgreSQL
-- Gestión de Usuarios - Arquitectura Hexagonal
-- =============================================
-- La base de datos NO se crea aquí:
--   - En local, la crea el contenedor de PostgreSQL (variable POSTGRES_DB).
--   - En la nube, se crea desde el panel de Neon.
-- =============================================

-- ─────────────────────────────────────────────
-- Tabla de usuarios
-- role y status usan VARCHAR + CHECK en lugar de ENUM:
-- el repositorio envía estos valores con setString(),
-- y un tipo ENUM de PostgreSQL rechazaría texto plano.
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id          VARCHAR(36)  NOT NULL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL
                CHECK (role IN ('ADMIN', 'MEMBER', 'REVIEWER')),
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                CHECK (status IN ('ACTIVE', 'INACTIVE', 'PENDING', 'BLOCKED')),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ─────────────────────────────────────────────
-- Usuario administrador inicial
-- Contraseña: Admin1234!  (hash BCrypt real, costo 12)
-- ON CONFLICT permite ejecutar el script varias veces sin error.
-- ─────────────────────────────────────────────
INSERT INTO users (id, name, email, password, role, status)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Administrador',
    'admin@example.com',
    '$2a$12$KITwpmH9ZG/bdV5NfJmnvuVKTgDXYbuYz6iol8IyR1asW0U4j6Ub2',
    'ADMIN',
    'ACTIVE'
)
ON CONFLICT (id) DO NOTHING;