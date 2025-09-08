-- Categorias
INSERT INTO categoria(nombre) VALUES ('Accion'),('Drama'),('Comedia')
    ON CONFLICT DO NOTHING;

-- Pelicula demo
INSERT INTO pelicula(titulo,sinopsis,duracion_min,poster_url,estado)
VALUES ('Demo: La Gran Aventura','Una pelicula de prueba',120,'https://via.placeholder.com/200x300','ACTIVA')
RETURNING id;

-- Vincular a Accion
INSERT INTO pelicula_categoria(pelicula_id,categoria_id)
VALUES (currval('pelicula_id_seq'), (SELECT id FROM categoria WHERE nombre='Accion'))
ON CONFLICT DO NOTHING;

-- Sala y asientos
INSERT INTO sala(nombre, filas, columnas) VALUES ('Sala 1', 5, 8) RETURNING id;

DO $$
DECLARE r INT; c INT; salaId INT; BEGIN
  salaId := currval('sala_id_seq');
  FOR r IN 1..5 LOOP
    FOR c IN 1..8 LOOP
      INSERT INTO asiento(sala_id,fila,columna,tipo,activo)
      VALUES (salaId, r, c, 'NORMAL', TRUE);
    END LOOP;
  END LOOP;
END $$;

-- Funcion para hoy + asiento_funcion
INSERT INTO funcion(pelicula_id,sala_id,hora_inicio,idioma,formato,precio_base)
VALUES (currval('pelicula_id_seq'), currval('sala_id_seq'), NOW() + INTERVAL '2 hour','SUB','DOSD', 15000);

INSERT INTO asiento_funcion(funcion_id, asiento_id, estado)
SELECT f.id, a.id, 'LIBRE'
FROM funcion f CROSS JOIN asiento a
WHERE f.sala_id = a.sala_id;

-- Usuario demo
INSERT INTO usuario(nombre,email,hash_password,rol)
VALUES ('Admin','admin@cine.local','$2a$10$QkF2fN5ludc9Qj2rj7S0xOKxKi1qP8xkzcjz7xwo2H8z0U/9xv42u','ADMIN')
ON CONFLICT DO NOTHING;

