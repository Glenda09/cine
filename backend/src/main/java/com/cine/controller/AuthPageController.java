package com.cine.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPageController {
    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @GetMapping("/logout")
    public String logout(HttpServletResponse resp) {
        Cookie cookie = new Cookie("AUTH", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        resp.addCookie(cookie);
        return "redirect:/";
    }
}

