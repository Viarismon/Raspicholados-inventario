-- ============================================================
-- Módulo: Inventario
-- Propósito: Creación de la base de datos y tabla del módulo de
--            inventario del sistema RASPICHOLADOS.
-- Autora: Viviana Arias Montilla
-- Fecha de creación: 2026-08-28
-- ============================================================

CREATE DATABASE IF NOT EXISTS raspicholados_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_spanish_ci;

USE raspicholados_db;

-- Tabla del módulo de inventario (soporta RF-01 a RF-05 del
-- informe de especificaciones funcionales).
CREATE TABLE IF NOT EXISTS producto (
    id_producto      INT AUTO_INCREMENT PRIMARY KEY,
    nombre_producto  VARCHAR(80)     NOT NULL,
    categoria        VARCHAR(50)     NOT NULL,
    unidad_medida    VARCHAR(20)     NOT NULL,
    stock_actual     INT             NOT NULL DEFAULT 0,
    stock_minimo     INT             NOT NULL DEFAULT 0,
    fecha_registro   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB;

-- Datos de ejemplo para pruebas del CRUD.
INSERT INTO producto (nombre_producto, categoria, unidad_medida, stock_actual, stock_minimo)
VALUES
    ('Hielo raspado',        'Insumo',    'Kilogramo', 50, 10),
    ('Leche condensada',     'Insumo',    'Unidad',    30, 8),
    ('Vaso cholado grande',  'Empaque',   'Unidad',    120, 30),
    ('Cereza en almíbar',    'Insumo',    'Frasco',    15, 5);
