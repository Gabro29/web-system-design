package com.gabro.kaffe.controller;

import com.gabro.kaffe.dto.ClienteDTO;
import com.gabro.kaffe.service.ClienteService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import java.security.Principal;


@RestController
@RequestMapping("/api/cliente")
public class ClienteRestController {


    private final ClienteService clienteService;


    public ClienteRestController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }


    @PostMapping(value = "/register", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<ClienteDTO> register(@RequestParam String email, @RequestParam String password) {
        return ResponseEntity.ok(clienteService.register(email, password));
    }


    @GetMapping(value = "/me", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<ClienteDTO> getCurrentUser(Principal principal) {
        return ResponseEntity.ok(clienteService.getStato(principal.getName()));
    }


    @GetMapping(value = "/status", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<ClienteDTO> getStatus(Principal principal) {
        return ResponseEntity.ok(clienteService.getStato(principal.getName()));
    }


    @PostMapping(value = "/connect", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<ClienteDTO> connect(Principal principal, @RequestParam String machineId) {
        return ResponseEntity.ok(clienteService.connettiMacchinetta(principal.getName(), machineId));
    }


    @PostMapping(value = "/disconnect", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<ClienteDTO> disconnect(Principal principal) {
        return ResponseEntity.ok(clienteService.disconnettiMacchinetta(principal.getName()));
    }


    @PostMapping(value = "/recharge", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<ClienteDTO> recharge(Principal principal, @RequestParam Double amount) {
        return ResponseEntity.ok(clienteService.ricarica(principal.getName(), amount));
    }
}