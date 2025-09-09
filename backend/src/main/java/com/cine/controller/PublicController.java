package com.cine.controller;

import com.cine.domain.Pelicula;
import com.cine.domain.Enums;
import com.cine.repo.PeliculaRepository;
import com.cine.repo.FuncionRepository;
import com.cine.repo.CategoriaRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class PublicController {
    private final PeliculaRepository peliculaRepository;
    private final FuncionRepository funcionRepository;
    private final CategoriaRepository categoriaRepository;

    public PublicController(PeliculaRepository peliculaRepository,
                            FuncionRepository funcionRepository,
                            CategoriaRepository categoriaRepository) {
        this.peliculaRepository = peliculaRepository;
        this.funcionRepository = funcionRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping("/")
    public String cartelera(@RequestParam(value = "fecha", required = false)
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                            @RequestParam(value = "categoria", required = false) String categoria,
                            @RequestParam(value = "clasif", required = false) Enums.ClasificacionEdad clasif,
                            Model model) {
        var all = peliculaRepository.findAll();
        LocalDate f = (fecha != null ? fecha : LocalDate.now());
        var filtered = all.stream()
                .filter(p -> p.getEstado() == Enums.EstadoPelicula.ACTIVA)
                .filter(p -> categoria == null || p.getCategorias().stream().anyMatch(c -> c.getNombre().equalsIgnoreCase(categoria)))
                .filter(p -> clasif == null || p.getClasificacionEdad() == clasif)
                .toList();
        model.addAttribute("peliculas", filtered);
        model.addAttribute("fecha", f);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("clasifs", Enums.ClasificacionEdad.values());
        model.addAttribute("categoriaSel", categoria);
        model.addAttribute("clasifSel", clasif);
        return "cartelera";
    }

    @GetMapping("/peliculas/{id}")
    public String pelicula(@PathVariable Long id, Model model) {
        Pelicula p = peliculaRepository.findById(id).orElseThrow();
        model.addAttribute("pelicula", p);
        // funciones próximas 7 días
        var start = java.time.LocalDateTime.now();
        var end = start.plusDays(7);
        model.addAttribute("funciones", funcionRepository.findByPeliculaAndHoraInicioBetween(p, start, end));
        return "pelicula";
    }
}
