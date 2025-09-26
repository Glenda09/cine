package com.cine.controller;

import com.cine.domain.Pago;
import com.cine.service.PagoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CheckoutController {
    private final PagoService pagoService;

    public CheckoutController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    // Llamada desde la selección de asientos
    @GetMapping("/checkout/start/{reservaId}")
    public String start(@PathVariable Long reservaId, Model model) {
        // 1) crea/asegura el Pago con referencia PENDING
        Pago p = pagoService.crearPagoCheckout(reservaId);

        // 2) amount-in-cents
        long cents = p.getReserva().getTotal()
                .movePointRight(2)
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .longValue();

        // 3) datos para la vista
        model.addAttribute("publicKey",      pagoService.getWompiPublicKey());
        model.addAttribute("currency",       pagoService.getCurrency());
        model.addAttribute("amountInCents",  cents);
        model.addAttribute("reference",      p.getReferencia());
        model.addAttribute("redirectUrl",    pagoService.getRedirectUrl());
        model.addAttribute("checkoutBase",   pagoService.getCheckoutBase());
        model.addAttribute("signature",      pagoService.buildIntegritySignature(p.getReferencia(), cents, pagoService.getCurrency()));
        return "checkout"; // templates/checkout.html
    }

    @GetMapping("/checkout/return")
    public String returned(@RequestParam(required = false) String id, Model model) {
        model.addAttribute("transactionId", id);
        return "checkout_return";
    }
}
