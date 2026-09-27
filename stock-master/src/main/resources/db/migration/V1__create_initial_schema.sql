CREATE TABLE sucursales (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    direccion VARCHAR(250),
    activa BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE productos (
    id VARCHAR(60) PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500),
    precio_compra NUMERIC(19,2) NOT NULL,
    precio_venta NUMERIC(19,2) NOT NULL
);

CREATE TABLE ppto (
    id BIGSERIAL PRIMARY KEY,
    monto NUMERIC(19,2) NOT NULL,
    fecha DATE NOT NULL
);

CREATE TABLE inventarios (
    id BIGSERIAL PRIMARY KEY,
    sucursal_id BIGINT NOT NULL REFERENCES sucursales(id),
    producto_id VARCHAR(60) NOT NULL REFERENCES productos(id),
    cantidad INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uk_inventario_sucursal_producto UNIQUE (sucursal_id, producto_id),
    CONSTRAINT ck_inventario_cantidad CHECK (cantidad >= 0)
);

CREATE TABLE ordenes_restock (
    id BIGSERIAL PRIMARY KEY,
    sucursal_id BIGINT NOT NULL REFERENCES sucursales(id),
    ppto_id BIGINT NOT NULL UNIQUE REFERENCES ppto(id),
    costo_total NUMERIC(19,2) NOT NULL,
    fecha TIMESTAMP NOT NULL,
    estado VARCHAR(30) NOT NULL
);

CREATE TABLE repuestos (
    id BIGSERIAL PRIMARY KEY,
    producto_id VARCHAR(60) NOT NULL REFERENCES productos(id),
    orden_restock_id BIGINT REFERENCES ordenes_restock(id),
    cantidad INTEGER NOT NULL,
    costo_unitario NUMERIC(19,2) NOT NULL,
    costo_total NUMERIC(19,2) NOT NULL
);

CREATE TABLE traspasos (
    id BIGSERIAL PRIMARY KEY,
    origen_id BIGINT NOT NULL REFERENCES sucursales(id),
    destino_id BIGINT NOT NULL REFERENCES sucursales(id),
    producto_id VARCHAR(60) NOT NULL REFERENCES productos(id),
    cantidad INTEGER NOT NULL,
    fecha TIMESTAMP NOT NULL,
    estado VARCHAR(30) NOT NULL,
    CONSTRAINT ck_traspaso_sucursales CHECK (origen_id <> destino_id)
);

CREATE TABLE gaps_presupuesto (
    id BIGSERIAL PRIMARY KEY,
    orden_restock_id BIGINT NOT NULL UNIQUE REFERENCES ordenes_restock(id),
    monto_presupuestado NUMERIC(19,2) NOT NULL,
    costo_real NUMERIC(19,2) NOT NULL,
    diferencia NUMERIC(19,2) NOT NULL,
    fecha_calculo TIMESTAMP NOT NULL
);
