package com.cine.controller;

import com.cine.repo.FuncionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class FunctionPageController {
    private final FuncionRepository funcionRepository;
    public FunctionPageController(FuncionRepository funcionRepository) { this.funcionRepository = funcionRepository; }

    @GetMapping("/funcion/{id}/seleccion")
    public String seleccion(@PathVariable Long id, Model model) {
        var f = funcionRepository.findById(id).orElseThrow();
        model.addAttribute("funcion", f);
        return "funcion_seats";
    }
}

