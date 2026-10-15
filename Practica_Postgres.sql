--Creación de la base de datos
CREATE DATABASE bdventas_1;

--Creación de esquema general para base de datos
CREATE SCHEMA catalogos;

--Creación de tabla de marcas
CREATE TABLE catalogos.marcas
(
	id_marca SERIAL PRIMARY KEY,
	nombre VARCHAR(80) NOT NULL UNIQUE,
	pais_origen VARCHAR(80) NOT NULL,
	estado SMALLINT NOT NULL DEFAULT 1

	CONSTRAINT chk_marca_estado
		CHECK (estado IN (0,1))
);

--Pruebas para validar los CONSTRAINS
--Prueba 1: no permite valores nulos
INSERT INTO catalogos.marcas (nombre, pais_origen, estado)
VALUES (NULL, 'Nicaragua',1);

--Prueba 2: no prmitir que la columna nombre tenga el mismo valor 2 veces
INSERT INTO catalogos.marcas (nombre, pais_origen, estado)
VALUES ('Almohada', 'Nicaragua',1);

--Prueba 3: Agregar el registro por defecto de una fila en la columna de estado
INSERT INTO catalogos.marcas (nombre, pais_origen)
VALUES ('Audifonos', 'Nicaragua');

---- TABLA 2: Categorías
CREATE TABLE catalogos.categorias
(
	id_categoria SERIAL PRIMARY KEY,
	nombre VARCHAR(80) NOT NULL UNIQUE,
	descripcion VARCHAR(250),
	estado SMALLINT NOT NULL DEFAULT 1

	CONSTRAINT chk_categoria_estado
	CHECK (estado IN (0,1))
);

--Pruebas para validar los CONSTRAINS
--Prueba 1: no permite valores nulos
INSERT INTO catalogos.categorias (nombre, descripcion, estado)
VALUES (NULL,NULL,1);

--Prueba 2: no prmitir que la columna nombre tenga el mismo valor 2 veces
INSERT INTO catalogos.categorias (nombre, descripcion, estado)
VALUES ('Restaurante', 'EL mejor lugar para ir a comer',1);

--Prueba 3: Agregar el registro por defecto de una fila en la columna de estado
INSERT INTO catalogos.categorias (nombre, descripcion)
VALUES ('Mercado',NULL);

----Tabla 3: Productos
CREATE TABLE catalogos.productos
(
    id_producto SERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    precio NUMERIC(10, 2) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    fecha_creacion DATE NOT NULL DEFAULT CURRENT_DATE,
    id_categoria INTEGER NOT NULL,
    id_marca INTEGER NOT NULL,
    estado SMALLINT NOT NULL DEFAULT 1,

    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (id_categoria)
        REFERENCES catalogos.categorias(id_categoria),

    CONSTRAINT fk_producto_marcas
        FOREIGN KEY (id_marca)
        REFERENCES catalogos.marcas(id_marca),

    CONSTRAINT chk_producto_precio
        CHECK (precio > 0),

    CONSTRAINT chk_producto_stock
        CHECK (stock >= 0),

    CONSTRAINT chk_producto_estado
        CHECK (estado IN (0,1))
);

-- 5 REGISTROS DE CATEGORÍAS
INSERT INTO catalogos.categorias (nombre, descripcion, estado) 
VALUES
('Laptops y Computadoras', 'Equipos portátiles, ultrabooks y estaciones de trabajo', 1),
('Smartphones y Celulares', 'Teléfonos móviles de diversas gamas y accesorios', 1),
('Periféricos y Accesorios', 'Teclados, ratones, cámaras web y micrófonos', 1),
('Audio y Sonido', 'Auriculares, altavoces y sistemas de audio profesional', 1),
('Monitores y Pantallas', 'Monitores para oficina, diseño gráfico y gaming', 1);

-- 5 REGISTROS DE MARCAS
INSERT INTO catalogos.marcas (nombre, pais_origen, estado) 
VALUES
('Apple', 'Estados Unidos', 1),
('Samsung', 'Corea del Sur', 1),
('Logitech', 'Suiza', 1),
('Sony', 'Japón', 1),
('Dell', 'Estados Unidos', 1);

-- 15 REGISTROS DE PRODUCTOS
INSERT INTO catalogos.productos (
    codigo,
    nombre,
    descripcion,
    precio,
    stock,
    fecha_creacion,
    id_categoria,
    id_marca,
    estado
)
VALUES
-- Categoría 1: Laptops (Apple: 1, Dell: 5)
('PROD-001', 'MacBook Air M2', 'Laptop ligera Chip M2, 8GB RAM, 256GB SSD', 1099.99, 15, CURRENT_DATE, 1, 1, 1),
('PROD-002', 'MacBook Pro 16 M3', 'Laptop profesional Chip M3 Pro, 18GB RAM', 2499.00, 8, CURRENT_DATE, 1, 1, 1),
('PROD-003', 'Dell XPS 13', 'Ultrabook Intel Core i7 13va Gen, 16GB RAM', 1299.50, 12, CURRENT_DATE, 1, 5, 1),
('PROD-004', 'Dell Inspiron 15', 'Laptop de uso diario Core i5, 8GB RAM, 512GB SSD', 650.00, 20, CURRENT_DATE, 1, 5, 1),

-- Categoría 2: Smartphones (Apple: 1, Samsung: 2)
('PROD-005', 'iPhone 15 Pro 128GB', 'Acabado en titanio con cámara principal de 48MP', 999.00, 25, CURRENT_DATE, 2, 1, 1),
('PROD-006', 'Samsung Galaxy S24 Ultra', 'Pantalla AMOLED 2X con soporte y lápiz S-Pen', 1199.99, 18, CURRENT_DATE, 2, 2, 1),
('PROD-007', 'Samsung Galaxy A54', 'Gama media con pantalla Super AMOLED 120Hz', 349.99, 30, CURRENT_DATE, 2, 2, 1),

-- Categoría 3: Periféricos (Logitech: 3)
('PROD-008', 'Logitech MX Master 3S', 'Mouse ergonómico inalámbrico con sensor 8K DPI', 99.90, 45, CURRENT_DATE, 3, 3, 1),
('PROD-009', 'Logitech MX Keys Mini', 'Teclado inalámbrico compacto iluminado', 89.90, 35, CURRENT_DATE, 3, 3, 1),
('PROD-010', 'Logitech C920 Pro', 'Cámara web Full HD 1080p con micrófonos duales', 69.99, 50, CURRENT_DATE, 3, 3, 1),

-- Categoría 4: Audio (Sony: 4, Apple: 1)
('PROD-011', 'Sony WH-1000XM5', 'Auriculares de diadema con cancelación de ruido', 398.00, 22, CURRENT_DATE, 4, 4, 1),
('PROD-012', 'Sony WF-1000XM5', 'Auriculares True Wireless cancelación de ruido', 279.99, 16, CURRENT_DATE, 4, 4, 1),
('PROD-013', 'AirPods Pro 2da Gen', 'Cancelación activa de ruido con estuche USB-C', 249.00, 40, CURRENT_DATE, 4, 1, 1),

-- Categoría 5: Monitores (Dell: 5, Samsung: 2)
('PROD-014', 'Dell UltraSharp 27 4K', 'Monitor IPS profesional UHD con puerto USB-C', 580.00, 10, CURRENT_DATE, 5, 5, 1),
('PROD-015', 'Samsung Odyssey G7 32"', 'Monitor curvo para gaming 240Hz 1ms QHD', 699.99, 7, CURRENT_DATE, 5, 2, 1);


SELECT *
	FROM catalogos.productos
		WHERE estado = 1 AND stock>0;

SELECT *
	FROM catalogos.productos
		WHERE precio >= 50;

SELECT *
	FROM catalogos.productos
		WHERE precio BETWEEN 20 AND 100;

SELECT *
	FROM catalogos.productos
		WHERE nombre ilike %USB%;

SELECT *
	FROM catalogos.productos
		WHERE codigo ILIKE '%PROD%' OR nombre ILIKE '%PROD%';

