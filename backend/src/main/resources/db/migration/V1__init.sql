CREATE TABLE categoria (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE pelicula (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    sinopsis TEXT,
    duracion_min INTEGER,
    poster_url VARCHAR(1024),
    trailer_url VARCHAR(1024),
    tipo VARCHAR(20),
    clasificacion_edad VARCHAR(20),
    estado VARCHAR(20)
);

CREATE TABLE pelicula_categoria (
    pelicula_id BIGINT NOT NULL REFERENCES pelicula(id) ON DELETE CASCADE,
    categoria_id BIGINT NOT NULL REFERENCES categoria(id) ON DELETE CASCADE,
    PRIMARY KEY (pelicula_id, categoria_id)
);

CREATE TABLE sala (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) UNIQUE NOT NULL,
    filas INTEGER,
    columnas INTEGER
);

CREATE TABLE asiento (
    id BIGSERIAL PRIMARY KEY,
    sala_id BIGINT NOT NULL REFERENCES sala(id) ON DELETE CASCADE,
    fila INTEGER NOT NULL,
    columna INTEGER NOT NULL,
    tipo VARCHAR(30),
    activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE funcion (
    id BIGSERIAL PRIMARY KEY,
    pelicula_id BIGINT NOT NULL REFERENCES pelicula(id) ON DELETE CASCADE,
    sala_id BIGINT NOT NULL REFERENCES sala(id) ON DELETE CASCADE,
    hora_inicio TIMESTAMP NOT NULL,
    idioma VARCHAR(20),
    formato VARCHAR(20),
    precio_base NUMERIC(12,2) NOT NULL
);

CREATE TABLE asiento_funcion (
    id BIGSERIAL PRIMARY KEY,
    funcion_id BIGINT NOT NULL REFERENCES funcion(id) ON DELETE CASCADE,
    asiento_id BIGINT NOT NULL REFERENCES asiento(id) ON DELETE CASCADE,
    estado VARCHAR(20) NOT NULL,
    hold_expires_at TIMESTAMPTZ NULL,
    UNIQUE(funcion_id, asiento_id)
);

CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    hash_password VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL
);

CREATE TABLE reserva (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    funcion_id BIGINT NOT NULL REFERENCES funcion(id) ON DELETE CASCADE,
    estado VARCHAR(20) NOT NULL,
    total NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE reserva_asientos (
    reserva_id BIGINT NOT NULL REFERENCES reserva(id) ON DELETE CASCADE,
    asiento_funcion_id BIGINT NOT NULL REFERENCES asiento_funcion(id) ON DELETE CASCADE,
    PRIMARY KEY (reserva_id, asiento_funcion_id)
);

CREATE TABLE pago (
    id BIGSERIAL PRIMARY KEY,
    reserva_id BIGINT NOT NULL REFERENCES reserva(id) ON DELETE CASCADE,
    proveedor VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    referencia VARCHAR(255) UNIQUE,
    raw_payload TEXT,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
);
