package com.cine.controller;

import com.cine.domain.Pelicula;
import com.cine.repo.PeliculaRepository;
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

    public PublicController(PeliculaRepository peliculaRepository) {
        this.peliculaRepository = peliculaRepository;
    }

    @GetMapping("/")
    public String cartelera(@RequestParam(value = "fecha", required = false)
                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                            Model model) {
        List<Pelicula> peliculas = peliculaRepository.findAll();
        model.addAttribute("peliculas", peliculas);
        model.addAttribute("fecha", fecha != null ? fecha : LocalDate.now());
        return "cartelera";
    }

    @GetMapping("/peliculas/{id}")
    public String pelicula(@PathVariable Long id, Model model) {
        Pelicula p = peliculaRepository.findById(id).orElseThrow();
        model.addAttribute("pelicula", p);
        return "pelicula";
    }
}

