package com.gabro.kaffe.controller;

import com.gabro.kaffe.dto.MacchinettaStatusDTO;
import com.gabro.kaffe.service.MacchinettaService;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;


@Controller
public class PageController {


    @Value("${app.jakarta.sync-url}")
    private String jakartaUrl;
    private final MacchinettaService macchinettaService;


    public PageController(MacchinettaService macchinettaService) {
        this.macchinettaService = macchinettaService;
    }


    @GetMapping("/")
    public String home() {
        return "redirect:/access-denied";
    }


    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("message", "Per favore, seleziona il tuo ruolo per procedere.");
        return "access-denied";
    }


    @GetMapping("/macchinetta")
    public String showMacchinettaPage(@RequestParam Optional<String> code, @RequestParam Optional<String> place, Model model) {

        String machineCode = code.orElse("--");
        model.addAttribute("macchinettaCode", machineCode);
        model.addAttribute("macchinettaPlace", place.orElse("--"));

        if (code.isPresent()) {
            MacchinettaStatusDTO status = macchinettaService.getMacchinettaStatus(machineCode);
            model.addAttribute("prodotti", status.getProdotti());
        }

        String baseUrl = jakartaUrl.replace("/sync", "");
        model.addAttribute("jakartaServerUrl", baseUrl);

        return "macchinetta";
    }


    @GetMapping("/customer")
    public String showCustomerPage(Model model) {
        String baseUrl = jakartaUrl.replace("/sync", "");
        model.addAttribute("jakartaServerUrl", baseUrl);
        return "customer";
    }


    @GetMapping("/addetto")
    public String showAddettoPage() {
        return "addetto";
    }


    @GetMapping("/gestore")
    public String showGestorePage(Model model) {
        String baseUrl = jakartaUrl.replace("/sync", "");
        model.addAttribute("jakartaServerUrl", baseUrl);
        return "gestore";
    }
}