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
    private final UsuarioRepository usuarioRepository;

    public AdminPageController(CategoriaRepository categoriaRepository, PeliculaRepository peliculaRepository,
                               SalaRepository salaRepository, FuncionRepository funcionRepository,
                               AsientoRepository asientoRepository, AsientoFuncionRepository asientoFuncionRepository,
                               UsuarioRepository usuarioRepository) {
        this.categoriaRepository = categoriaRepository;
        this.peliculaRepository = peliculaRepository;
        this.salaRepository = salaRepository;
        this.funcionRepository = funcionRepository;
        this.asientoRepository = asientoRepository;
        this.asientoFuncionRepository = asientoFuncionRepository;
        this.usuarioRepository = usuarioRepository;
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
        model.addAttribute("edit", new com.cine.domain.Categoria());
        return "admin/categorias";
    }
    @PostMapping("/categorias")
    public String crearCategoria(@RequestParam String nombre) {
        if (nombre != null && !nombre.isBlank()) categoriaRepository.save(Categoria.builder().nombre(nombre.trim()).build());
        return "redirect:/admin/categorias";
    }
    @GetMapping("/categorias/{id}/edit")
    public String editCategoria(@PathVariable Long id, Model model) {
        var c = categoriaRepository.findById(id).orElseThrow();
        model.addAttribute("list", categoriaRepository.findAll());
        model.addAttribute("edit", c);
        return "admin/categorias";
    }

    @PostMapping("/categorias/{id}/update")
    public String updateCategoria(@PathVariable Long id, @RequestParam String nombre) {
        var c = categoriaRepository.findById(id).orElseThrow();
        c.setNombre(nombre);
        categoriaRepository.save(c);
        return "redirect:/admin/categorias";
    }

    @PostMapping("/categorias/{id}/delete")
    public String deleteCategoria(@PathVariable Long id) {
        categoriaRepository.deleteById(id);
        return "redirect:/admin/categorias";
    }

    // Películas
    @GetMapping("/peliculas")
    public String peliculas(Model model) {
        model.addAttribute("list", peliculaRepository.findAll());
        model.addAttribute("cats", categoriaRepository.findAll());
        model.addAttribute("editPeli", new com.cine.domain.Pelicula());
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
        .posterUrl(safeUrl(posterUrl))
        .trailerUrl(safeUrl(trailerUrl))
                .clasificacionEdad(clasif!=null? ClasificacionEdad.valueOf(clasif): ClasificacionEdad.TP)
                .tipo(tipo!=null? TipoPelicula.valueOf(tipo): TipoPelicula.DOSD)
                .estado(EstadoPelicula.ACTIVA)
                .build();
        peliculaRepository.save(p);
        return "redirect:/admin/peliculas";
    }
    @GetMapping("/peliculas/{id}/edit")
    public String editPelicula(@PathVariable Long id, Model model) {
        var p = peliculaRepository.findById(id).orElseThrow();
        model.addAttribute("list", peliculaRepository.findAll());
        model.addAttribute("cats", categoriaRepository.findAll());
        model.addAttribute("editPeli", p);
        return "admin/peliculas";
    }

    @PostMapping("/peliculas/{id}/update")
    public String updatePelicula(@PathVariable Long id,
                                 @RequestParam String titulo,
                                 @RequestParam(required = false) String sinopsis,
                                 @RequestParam(required = false) Integer duracionMin,
                                 @RequestParam(required = false) String posterUrl,
                                 @RequestParam(required = false) String trailerUrl,
                                 @RequestParam(required = false) String clasif,
                                 @RequestParam(required = false) String tipo) {
        var p = peliculaRepository.findById(id).orElseThrow();
        p.setTitulo(titulo);
        p.setSinopsis(sinopsis);
        p.setDuracionMin(duracionMin);
        p.setPosterUrl(safeUrl(posterUrl));
        p.setTrailerUrl(safeUrl(trailerUrl));
        p.setClasificacionEdad(clasif!=null? ClasificacionEdad.valueOf(clasif): p.getClasificacionEdad());
        p.setTipo(tipo!=null? TipoPelicula.valueOf(tipo): p.getTipo());
        peliculaRepository.save(p);
        return "redirect:/admin/peliculas";
    }

    private String safeUrl(String url) {
        if (url == null) return null;
        if (url.length() <= 1024) return url;
        return url.substring(0, 1024);
    }

    @PostMapping("/peliculas/{id}/delete")
    public String deletePelicula(@PathVariable Long id) {
        peliculaRepository.deleteById(id);
        return "redirect:/admin/peliculas";
    }

    // Salas
    @GetMapping("/salas")
    public String salas(Model model) {
        model.addAttribute("list", salaRepository.findAll());
        model.addAttribute("editSala", new com.cine.domain.Sala());
        return "admin/salas";
    }
    @PostMapping("/salas")
    public String crearSala(@RequestParam String nombre, @RequestParam Integer filas, @RequestParam Integer columnas) {
        Sala s = salaRepository.save(Sala.builder().nombre(nombre).filas(filas).columnas(columnas).build());
        for (int r=1;r<=filas;r++) for (int c=1;c<=columnas;c++)
            asientoRepository.save(Asiento.builder().sala(s).fila(r).columna(c).tipo(TipoAsiento.NORMAL).activo(true).build());
        return "redirect:/admin/salas";
    }
    @GetMapping("/salas/{id}/edit")
    public String editSala(@PathVariable Long id, Model model) {
        var s = salaRepository.findById(id).orElseThrow();
        model.addAttribute("list", salaRepository.findAll());
        model.addAttribute("editSala", s);
        return "admin/salas";
    }

    @PostMapping("/salas/{id}/update")
    public String updateSala(@PathVariable Long id, @RequestParam String nombre, @RequestParam Integer filas, @RequestParam Integer columnas) {
        var s = salaRepository.findById(id).orElseThrow();
        s.setNombre(nombre);
        s.setFilas(filas);
        s.setColumnas(columnas);
        salaRepository.save(s);
        return "redirect:/admin/salas";
    }

    @PostMapping("/salas/{id}/delete")
    public String deleteSala(@PathVariable Long id) {
        salaRepository.deleteById(id);
        return "redirect:/admin/salas";
    }

    // Funciones
    @GetMapping("/funciones")
    public String funciones(Model model) {
        model.addAttribute("peliculas", peliculaRepository.findAll());
        model.addAttribute("salas", salaRepository.findAll());
        model.addAttribute("list", funcionRepository.findAll());
        model.addAttribute("editFunc", new com.cine.domain.Funcion());
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
    @GetMapping("/funciones/{id}/edit")
    public String editFuncion(@PathVariable Long id, Model model) {
        var f = funcionRepository.findById(id).orElseThrow();
        model.addAttribute("peliculas", peliculaRepository.findAll());
        model.addAttribute("salas", salaRepository.findAll());
        model.addAttribute("list", funcionRepository.findAll());
        model.addAttribute("editFunc", f);
        return "admin/funciones";
    }

    @PostMapping("/funciones/{id}/update")
    @Transactional
    public String updateFuncion(@PathVariable Long id, @RequestParam Long peliculaId, @RequestParam Long salaId,
                                @RequestParam String horaInicio, @RequestParam BigDecimal precioBase,
                                @RequestParam(required = false) String idioma, @RequestParam(required = false) String formato) {
        var f = funcionRepository.findById(id).orElseThrow();
        var p = peliculaRepository.findById(peliculaId).orElseThrow();
        var s = salaRepository.findById(salaId).orElseThrow();
        f.setPelicula(p);
        f.setSala(s);
        f.setHoraInicio(java.time.LocalDateTime.parse(horaInicio));
        f.setPrecioBase(precioBase);
        f.setIdioma(idioma!=null? Idioma.valueOf(idioma): f.getIdioma());
        f.setFormato(formato!=null? Formato.valueOf(formato): f.getFormato());
        funcionRepository.save(f);
        return "redirect:/admin/funciones";
    }

    @PostMapping("/funciones/{id}/delete")
    public String deleteFuncion(@PathVariable Long id) {
        funcionRepository.deleteById(id);
        return "redirect:/admin/funciones";
    }

    // Usuarios (roles)
    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        model.addAttribute("list", usuarioRepository.findAll());
        return "admin/usuarios";
    }
    @PostMapping("/usuarios/{id}/role")
    public String changeUserRole(@PathVariable Long id, @RequestParam String rol) {
        var u = usuarioRepository.findById(id).orElseThrow();
        u.setRol(com.cine.domain.Enums.RolUsuario.valueOf(rol));
        usuarioRepository.save(u);
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/delete")
    public String deleteUsuario(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
        return "redirect:/admin/usuarios";
    }
}

