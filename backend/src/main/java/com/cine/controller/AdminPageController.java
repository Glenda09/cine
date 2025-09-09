package com.cine.controller;

import com.cine.domain.*;
import com.cine.repo.*;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.cine.domain.Enums.*;

@Controller
@RequestMapping("/admin")
public class AdminPageController {
    private final CategoriaRepository categoriaRepository;
    private final PeliculaRepository peliculaRepository;
    private final SalaRepository salaRepository;
    private final FuncionRepository funcionRepository;
    private final AsientoRepository asientoRepository;
    private final AsientoFuncionRepository asientoFuncionRepository;

    public AdminPageController(CategoriaRepository categoriaRepository, PeliculaRepository peliculaRepository,
                               SalaRepository salaRepository, FuncionRepository funcionRepository,
                               AsientoRepository asientoRepository, AsientoFuncionRepository asientoFuncionRepository) {
        this.categoriaRepository = categoriaRepository;
        this.peliculaRepository = peliculaRepository;
        this.salaRepository = salaRepository;
        this.funcionRepository = funcionRepository;
        this.asientoRepository = asientoRepository;
        this.asientoFuncionRepository = asientoFuncionRepository;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("pelis", peliculaRepository.count());
        model.addAttribute("salas", salaRepository.count());
        model.addAttribute("funciones", funcionRepository.count());
        model.addAttribute("categorias", categoriaRepository.count());
        return "admin/index";
    }

    // Categorías
    @GetMapping("/categorias")
    public String categorias(Model model) {
        model.addAttribute("list", categoriaRepository.findAll());
        return "admin/categorias";
    }
    @PostMapping("/categorias")
    public String crearCategoria(@RequestParam String nombre) {
        if (nombre != null && !nombre.isBlank()) categoriaRepository.save(Categoria.builder().nombre(nombre.trim()).build());
        return "redirect:/admin/categorias";
    }

    // Películas
    @GetMapping("/peliculas")
    public String peliculas(Model model) {
        model.addAttribute("list", peliculaRepository.findAll());
        model.addAttribute("cats", categoriaRepository.findAll());
        return "admin/peliculas";
    }
    @PostMapping("/peliculas")
    public String crearPelicula(@RequestParam String titulo,
                                @RequestParam(required = false) String sinopsis,
                                @RequestParam(required = false) Integer duracionMin,
                                @RequestParam(required = false) String posterUrl,
                                @RequestParam(required = false) String trailerUrl,
                                @RequestParam(required = false) String clasif,
                                @RequestParam(required = false) String tipo) {
        Pelicula p = Pelicula.builder()
                .titulo(titulo)
                .sinopsis(sinopsis)
                .duracionMin(duracionMin)
                .posterUrl(posterUrl)
                .trailerUrl(trailerUrl)
                .clasificacionEdad(clasif!=null? ClasificacionEdad.valueOf(clasif): ClasificacionEdad.TP)
                .tipo(tipo!=null? TipoPelicula.valueOf(tipo): TipoPelicula.DOSD)
                .estado(EstadoPelicula.ACTIVA)
                .build();
        peliculaRepository.save(p);
        return "redirect:/admin/peliculas";
    }

    // Salas
    @GetMapping("/salas")
    public String salas(Model model) {
        model.addAttribute("list", salaRepository.findAll());
        return "admin/salas";
    }
    @PostMapping("/salas")
    public String crearSala(@RequestParam String nombre, @RequestParam Integer filas, @RequestParam Integer columnas) {
        Sala s = salaRepository.save(Sala.builder().nombre(nombre).filas(filas).columnas(columnas).build());
        for (int r=1;r<=filas;r++) for (int c=1;c<=columnas;c++)
            asientoRepository.save(Asiento.builder().sala(s).fila(r).columna(c).tipo(TipoAsiento.NORMAL).activo(true).build());
        return "redirect:/admin/salas";
    }

    // Funciones
    @GetMapping("/funciones")
    public String funciones(Model model) {
        model.addAttribute("peliculas", peliculaRepository.findAll());
        model.addAttribute("salas", salaRepository.findAll());
        model.addAttribute("list", funcionRepository.findAll());
        return "admin/funciones";
    }

    @PostMapping("/funciones")
    @Transactional
    public String crearFuncion(@RequestParam Long peliculaId, @RequestParam Long salaId,
                               @RequestParam String horaInicio,
                               @RequestParam BigDecimal precioBase,
                               @RequestParam(required = false) String idioma,
                               @RequestParam(required = false) String formato) {
        Pelicula p = peliculaRepository.findById(peliculaId).orElseThrow();
        Sala s = salaRepository.findById(salaId).orElseThrow();
        LocalDateTime hi = LocalDateTime.parse(horaInicio);
        Funcion f = Funcion.builder()
                .pelicula(p).sala(s).horaInicio(hi)
                .precioBase(precioBase)
                .idioma(idioma!=null? Idioma.valueOf(idioma): Idioma.SUB)
                .formato(formato!=null? Formato.valueOf(formato): Formato.DOSD)
                .build();
        f = funcionRepository.save(f);
        for (Asiento a : asientoRepository.findBySalaAndActivoTrue(s)) {
            asientoFuncionRepository.save(AsientoFuncion.builder().funcion(f).asiento(a).estado(EstadoAsientoFuncion.LIBRE).build());
        }
        return "redirect:/admin/funciones";
    }
}

