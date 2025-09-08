package com.cine.controller;

import com.cine.domain.Pago;
import com.cine.service.PagoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CheckoutController {
    private final PagoService pagoService;

    public CheckoutController(PagoService pagoService) { this.pagoService = pagoService; }

    @GetMapping("/checkout/start/{reservaId}")
    public String start(@PathVariable Long reservaId, Model model) {
        Pago p = pagoService.crearPagoCheckout(reservaId);
        long cents = p.getReserva().getTotal().movePointRight(2).longValueExact();
        model.addAttribute("publicKey", pagoService.getWompiPublicKey());
        model.addAttribute("currency", pagoService.getCurrency());
        model.addAttribute("amountInCents", cents);
        model.addAttribute("reference", p.getReferencia());
        model.addAttribute("redirectUrl", pagoService.getRedirectUrl());
        model.addAttribute("signature", pagoService.buildIntegritySignature(p.getReferencia(), cents, pagoService.getCurrency()));
        return "checkout";
    }

    @GetMapping("/checkout/return")
    public String returned(String id, Model model) {
        model.addAttribute("transactionId", id);
        return "checkout_return";
    }
}

