package com.cine.controller;

import com.cine.domain.AsientoFuncion;
import com.cine.domain.Funcion;
import com.cine.repo.AsientoFuncionRepository;
import com.cine.repo.FuncionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.time.*;
import com.cine.domain.Enums;

@RestController
public class FuncionController {
    private final FuncionRepository funcionRepository;
    private final AsientoFuncionRepository asientoFuncionRepository;

    public FuncionController(FuncionRepository funcionRepository, AsientoFuncionRepository asientoFuncionRepository) {
        this.funcionRepository = funcionRepository;
        this.asientoFuncionRepository = asientoFuncionRepository;
    }

    @GetMapping("/funcion/{id}")
    public ResponseEntity<?> funcion(@PathVariable Long id) {
        Funcion f = funcionRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(f);
    }

    @GetMapping("/funcion/{id}/asientos")
    public ResponseEntity<?> asientos(@PathVariable Long id) {
        List<AsientoFuncion> list = asientoFuncionRepository.findByFuncion(funcionRepository.findById(id).orElseThrow());
        return ResponseEntity.ok(list.stream().map(af -> Map.of(
                "id", af.getAsiento().getId(),
                "asientoFuncionId", af.getId(),
                "fila", af.getAsiento().getFila(),
                "columna", af.getAsiento().getColumna(),
                "estado", af.getEstado().name()
        )).toList());
    }

    @GetMapping("/api/funciones")
    public ResponseEntity<?> listarFunciones(
            @RequestParam LocalDate fecha,
            @RequestParam(required = false) Long peliculaId,
            @RequestParam(required = false) Enums.Formato formato
    ) {
        LocalDateTime start = fecha.atStartOfDay();
        LocalDateTime end = fecha.plusDays(1).atStartOfDay();
        var funciones = funcionRepository.findByHoraInicioBetween(start, end);
        var list = funciones.stream()
                .filter(f -> peliculaId == null || f.getPelicula().getId().equals(peliculaId))
                .filter(f -> formato == null || f.getFormato() == formato)
                .sorted(java.util.Comparator.comparing(com.cine.domain.Funcion::getHoraInicio))
                .map(f -> Map.of(
                        "id", f.getId(),
                        "hora", f.getHoraInicio().toLocalTime().toString(),
                        "sala", f.getSala().getNombre(),
                        "formato", f.getFormato().name(),
                        "idioma", f.getIdioma().name(),
                        "peliculaId", f.getPelicula().getId(),
                        "pelicula", f.getPelicula().getTitulo()
                )).toList();
        return ResponseEntity.ok(list);
    }
}
