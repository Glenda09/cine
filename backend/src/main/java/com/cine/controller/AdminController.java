package com.cine.controller;

import com.cine.domain.*;
import com.cine.repo.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.cine.domain.Enums.*;

@RestController
@RequestMapping("/api/admin")
//prueba
public class AdminController {
    private final PeliculaRepository peliculaRepository;
    private final SalaRepository salaRepository;
    private final FuncionRepository funcionRepository;
    private final AsientoRepository asientoRepository;
    private final AsientoFuncionRepository asientoFuncionRepository;

    public AdminController(PeliculaRepository peliculaRepository, SalaRepository salaRepository,
                           FuncionRepository funcionRepository, AsientoRepository asientoRepository,
                           AsientoFuncionRepository asientoFuncionRepository) {
        this.peliculaRepository = peliculaRepository;
        this.salaRepository = salaRepository;
        this.funcionRepository = funcionRepository;
        this.asientoRepository = asientoRepository;
        this.asientoFuncionRepository = asientoFuncionRepository;
    }

    public record PeliculaReq(@NotNull String titulo, String sinopsis, Integer duracionMin) {}

    @PostMapping("/peliculas")
    public ResponseEntity<?> crearPelicula(@RequestBody PeliculaReq req) {
        Pelicula p = Pelicula.builder().titulo(req.titulo()).sinopsis(req.sinopsis()).duracionMin(req.duracionMin())
                .estado(EstadoPelicula.ACTIVA).tipo(TipoPelicula.DOSD).clasificacionEdad(ClasificacionEdad.TP).build();
        return ResponseEntity.ok(peliculaRepository.save(p));
    }

    // Alias singular por conveniencia
    @PostMapping("/pelicula")
    public ResponseEntity<?> crearPeliculaAlias(@RequestBody PeliculaReq req) { return crearPelicula(req); }

    public record SalaReq(@NotNull String nombre, Integer filas, Integer columnas) {}

    @PostMapping("/salas")
    public ResponseEntity<?> crearSala(@RequestBody SalaReq req) {
        Sala s = salaRepository.save(Sala.builder().nombre(req.nombre()).filas(req.filas()).columnas(req.columnas()).build());
        // generar asientos base
        for (int r=1;r<=req.filas();r++) for (int c=1;c<=req.columnas();c++)
            asientoRepository.save(Asiento.builder().sala(s).fila(r).columna(c).tipo(TipoAsiento.NORMAL).activo(true).build());
        return ResponseEntity.ok(s);
    }

    public record FuncionReq(@NotNull Long peliculaId, @NotNull Long salaId, @NotNull LocalDateTime horaInicio,
                              String idioma, String formato, @NotNull BigDecimal precioBase) {}

    @PostMapping("/funciones")
    @Transactional
    public ResponseEntity<?> crearFuncion(@RequestBody FuncionReq req) {
        Pelicula p = peliculaRepository.findById(req.peliculaId()).orElseThrow();
        Sala s = salaRepository.findById(req.salaId()).orElseThrow();
        Funcion f = Funcion.builder().pelicula(p).sala(s).horaInicio(req.horaInicio())
                .idioma(req.idioma()!=null? Idioma.valueOf(req.idioma()): Idioma.SUB)
                .formato(req.formato()!=null? Formato.valueOf(req.formato()): Formato.DOSD)
                .precioBase(req.precioBase()).build();
        f = funcionRepository.save(f);
        for (Asiento a : asientoRepository.findBySalaAndActivoTrue(s)) {
            asientoFuncionRepository.save(AsientoFuncion.builder().funcion(f).asiento(a).estado(EstadoAsientoFuncion.LIBRE).build());
        }
        return ResponseEntity.ok(f);
    }

    // Índice simple para /api/admin
    @GetMapping
    public ResponseEntity<?> index() {
        return ResponseEntity.ok(java.util.Map.of(
                "endpoints", java.util.List.of(
                        "POST /api/admin/peliculas",
                        "POST /api/admin/pelicula (alias)",
                        "POST /api/admin/salas",
                        "POST /api/admin/funciones"
                )
        ));
    }
}
