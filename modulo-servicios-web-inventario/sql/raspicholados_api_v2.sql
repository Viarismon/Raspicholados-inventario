-- ============================================================
-- Proyecto: RASPICHOLADOS - Sistema de inventario
-- Script:   raspicholados_api_v2.sql
-- Evidencia: GA7-220501096-AA5-EV03 (servicios web del proyecto)
-- Propósito: Evolución del modelo de datos para la API REST:
--            usuario, categoria, proveedor, producto y
--            movimiento_inventario.
-- Autora: Viviana Arias Montilla
-- Fecha: 2026-11-14
--
-- IMPORTANTE: el script RECREA las tablas (borra los datos de
-- prueba anteriores). La tabla producto conserva las columnas
-- originales de la AA2-EV01 (nombre_producto, categoria,
-- unidad_medida, stock_actual, stock_minimo, fecha_registro),
-- por lo que los módulos anteriores siguen funcionando.
-- ============================================================

CREATE DATABASE IF NOT EXISTS raspicholados_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_spanish_ci;

USE raspicholados_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS movimiento_inventario;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS proveedor;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS usuario;
SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- Usuarios del sistema. La contraseña se guarda como hash
-- PBKDF2 (formato pbkdf2_sha256$iteraciones$salt$hash).
-- El administrador inicial lo crea la API al arrancar.
-- ------------------------------------------------------------
CREATE TABLE usuario (
    id_usuario       INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario   VARCHAR(30)  NOT NULL,
    nombre_completo  VARCHAR(100) NOT NULL,
    rol              ENUM('ADMINISTRADOR', 'EMPLEADO') NOT NULL DEFAULT 'EMPLEADO',
    hash_contrasena  VARCHAR(200) NOT NULL,
    activo           TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_registro   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuario_nombre UNIQUE (nombre_usuario)
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- Categorías. El nombre es único porque producto.categoria
-- lo referencia (llave foránea con ON UPDATE CASCADE).
-- ------------------------------------------------------------
CREATE TABLE categoria (
    id_categoria  INT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(50)  NOT NULL,
    descripcion   VARCHAR(200) NULL,
    CONSTRAINT uq_categoria_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

CREATE TABLE proveedor (
    id_proveedor  INT AUTO_INCREMENT PRIMARY KEY,
    nit           VARCHAR(14)  NOT NULL,
    nombre        VARCHAR(100) NOT NULL,
    telefono      VARCHAR(20)  NULL,
    correo        VARCHAR(100) NULL,
    direccion     VARCHAR(150) NULL,
    CONSTRAINT uq_proveedor_nit UNIQUE (nit)
) ENGINE = InnoDB;

CREATE TABLE producto (
    id_producto      INT AUTO_INCREMENT PRIMARY KEY,
    nombre_producto  VARCHAR(80)    NOT NULL,
    categoria        VARCHAR(50)    NOT NULL,
    unidad_medida    VARCHAR(20)    NOT NULL,
    stock_actual     INT            NOT NULL DEFAULT 0,
    stock_minimo     INT            NOT NULL DEFAULT 0,
    precio_unitario  DECIMAL(12, 2) NOT NULL DEFAULT 0,
    id_proveedor     INT            NULL,
    activo           TINYINT(1)     NOT NULL DEFAULT 1,
    fecha_registro   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria)
        REFERENCES categoria (nombre) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_producto_proveedor FOREIGN KEY (id_proveedor)
        REFERENCES proveedor (id_proveedor) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT ck_producto_stock CHECK (stock_actual >= 0 AND stock_minimo >= 0),
    CONSTRAINT ck_producto_precio CHECK (precio_unitario >= 0),
    INDEX idx_producto_activo (activo)
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- Historial de movimientos: cada cambio de stock deja rastro
-- de quién, cuándo, cuánto y por qué.
-- ------------------------------------------------------------
CREATE TABLE movimiento_inventario (
    id_movimiento     INT AUTO_INCREMENT PRIMARY KEY,
    id_producto       INT          NOT NULL,
    tipo              ENUM('ENTRADA', 'SALIDA', 'AJUSTE') NOT NULL,
    cantidad          INT          NOT NULL,
    stock_anterior    INT          NOT NULL,
    stock_resultante  INT          NOT NULL,
    motivo            VARCHAR(200) NULL,
    id_usuario        INT          NOT NULL,
    fecha             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_movimiento_producto FOREIGN KEY (id_producto)
        REFERENCES producto (id_producto) ON DELETE RESTRICT,
    CONSTRAINT fk_movimiento_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT,
    CONSTRAINT ck_movimiento_cantidad CHECK (cantidad >= 0),
    INDEX idx_movimiento_fecha (fecha),
    INDEX idx_movimiento_producto (id_producto, fecha)
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- Datos de ejemplo (los mismos que usa el modo memoria)
-- ------------------------------------------------------------
INSERT INTO categoria (nombre, descripcion) VALUES
    ('Raspados', 'Raspados de hielo con jarabe de fruta'),
    ('Helados',  'Helados por litro para servir en copa o cono'),
    ('Toppings', 'Coberturas y adiciones'),
    ('Insumos',  'Materia prima para preparar los productos'),
    ('Empaques', 'Vasos, cucharas y desechables');

INSERT INTO proveedor (nit, nombre, telefono, correo, direccion) VALUES
    ('900123456-1', 'Frutas del Valle S.A.S.', '3104567890', 'ventas@frutasdelvalle.co',   'Cra 5 # 12-30, Cali'),
    ('800765432-9', 'Lácteos San Rafael',      '3157654321', 'pedidos@lacteossanrafael.co', 'Cl 25 # 8-14, Palmira'),
    ('901555222-3', 'Distribuidora Central',   '6024441122', 'contacto@distcentral.co',    'Av 3N # 45-10, Cali'),
    ('901888777-5', 'Empaques del Sur',        '3201112233', 'info@empaquesdelsur.co',     'Cl 70 # 1-50, Cali');

INSERT INTO producto (nombre_producto, categoria, unidad_medida, stock_actual, stock_minimo, precio_unitario, id_proveedor) VALUES
    ('Hielo raspado',       'Insumos',  'Kilogramo', 50,  10,  800,   3),
    ('Leche condensada',    'Toppings', 'Unidad',    20,  8,   6200,  3),
    ('Cereza en almíbar',   'Toppings', 'Frasco',    15,  5,   9500,  3),
    ('Jarabe de mora',      'Raspados', 'Litro',     12,  4,   14000, 1),
    ('Jarabe de mango',     'Raspados', 'Litro',     3,   4,   14000, 1),
    ('Helado de vainilla',  'Helados',  'Litro',     12,  5,   18000, 2),
    ('Helado de chocolate', 'Helados',  'Litro',     0,   5,   18500, 2),
    ('Vaso cholado grande', 'Empaques', 'Unidad',    120, 30,  350,   4),
    ('Cucharas plásticas',  'Empaques', 'Unidad',    90,  100, 60,    4);
