CREATE DATABASE IF NOT EXISTS banco_practica1_damian;
USE banco_practica1_damian;

-- 1. Tabla Clientes
CREATE TABLE IF NOT EXISTS cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(150) NOT NULL
);

-- 2. Tabla Cuentas (ya no lleva cliente_id)
CREATE TABLE IF NOT EXISTS cuenta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    iban VARCHAR(34) NOT NULL UNIQUE,
    saldo DECIMAL(13, 2) NOT NULL DEFAULT 0.00
);

-- 3. Tabla Intermedia (M:N)
CREATE TABLE IF NOT EXISTS cliente_cuenta (
    cliente_id BIGINT NOT NULL,
    cuenta_id BIGINT NOT NULL,
    PRIMARY KEY (cliente_id, cuenta_id),
    CONSTRAINT fk_intermedia_cliente 
        FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE,
    CONSTRAINT fk_intermedia_cuenta 
        FOREIGN KEY (cuenta_id) REFERENCES cuenta(id) ON DELETE CASCADE
);



-- ---------------------------------------------------------
-- DATOS DE PRUEBA
-- ---------------------------------------------------------

-- Inserción de 10 Clientes (IDs del 1 al 10)
INSERT INTO cliente (id, dni, nombre, apellidos) VALUES
(1, '12345678A', 'Carlos', 'Gómez Ruiz'),
(2, '23456789B', 'Lucía', 'Fernández Soto'),
(3, '34567890C', 'Marcos', 'Navarro Gil'),
(4, '45678901D', 'Elena', 'Prieto Ramos'),
(5, '56789012E', 'Javier', 'Morales Vega'),
(6, '67890123F', 'Sara', 'Castro Blanco'),
(7, '78901234G', 'David', 'Herrera Marín'),
(8, '89012345H', 'Paula', 'Ortega Cano'),
(9, '90123456J', 'Adrián', 'Vázquez Méndez'),
(10, '01234567K', 'Nuria', 'Iglesias Peña');

-- Inserción de 20 Cuentas (IDs del 1 al 20)
INSERT INTO cuenta (id, iban, saldo) VALUES
(1, 'ES9121000418450200051331', 4500.50),
(2, 'ES9121000418450200051332', 120.00),
(3, 'ES9121000418450200051333', 18500.75),
(4, 'ES9121000418450200051334', 320.10),
(5, 'ES9121000418450200051335', 9400.00),
(6, 'ES9121000418450200051336', 50.00),
(7, 'ES9121000418450200051337', 2300.20),
(8, 'ES9121000418450200051338', 15400.00),
(9, 'ES9121000418450200051339', 610.80),
(10, 'ES9121000418450200051340', 890.00),
(11, 'ES9121000418450200051341', 34200.00),
(12, 'ES9121000418450200051342', 75.30),
(13, 'ES9121000418450200051343', 1500.00),
(14, 'ES9121000418450200051344', 4300.90),
(15, 'ES9121000418450200051345', 820.45),
(16, 'ES9121000418450200051346', 11200.00),
(17, 'ES9121000418450200051347', 95.00),
(18, 'ES9121000418450200051348', 6700.60),
(19, 'ES9121000418450200051349', 250.00),
(20, 'ES9121000418450200051350', 5000.00);

-- Relaciones cliente_cuenta
INSERT INTO cliente_cuenta (cliente_id, cuenta_id) VALUES
-- Cuentas compartidas entre Carlos (1) y Lucía (2)
(1, 1),
(2, 1),
(1, 2),
(2, 2),

-- Carlos (1) tiene además dos cuentas individuales propias (total 4 cuentas)
(1, 3),
(1, 4),

-- Marcos (3) tiene tres cuentas individuales
(3, 5),
(3, 6),
(3, 7),

-- Cuenta compartida entre Marcos (3) y Elena (4)
(3, 8),
(4, 8),

-- Elena (4) tiene además dos cuentas propias
(4, 9),
(4, 10),

-- Javier (5) tiene cuatro cuentas
(5, 11),
(5, 12),
(5, 13),
(5, 14),

-- Cuenta compartida entre Sara (6), David (7) y Paula (8)
(6, 15),
(7, 15),
(8, 15),

-- Sara (6) y David (7) con cuentas individuales adicionales
(6, 16),
(7, 17),
(7, 18),

-- Paula (8) con dos cuentas individuales adicionales
(8, 19),
(8, 20);

-- Clientes sin cuenta asignada a propósito para pruebas:
-- Cliente 9 (Adrián Vázquez Méndez) -> 0 cuentas asociadas
-- Cliente 10 (Nuria Iglesias Peña)  -> 0 cuentas asociadas