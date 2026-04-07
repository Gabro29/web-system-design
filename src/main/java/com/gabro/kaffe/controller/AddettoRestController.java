package com.gabro.kaffe.controller;

import com.gabro.kaffe.dto.AddettoDTO;
import com.gabro.kaffe.dto.xml.MachineListXmlDTO;
import com.gabro.kaffe.entity.StatoMacchinetta;
import com.gabro.kaffe.service.AddettoService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import java.security.Principal;


@RestController
@RequestMapping("/api/addetto")
public class AddettoRestController {


    private final AddettoService addettoService;


    public AddettoRestController(AddettoService addettoService) {
        this.addettoService = addettoService;
    }


    @GetMapping(value = "/me", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<AddettoDTO> getSessionData(Principal principal) {
        return ResponseEntity.ok(addettoService.getStato(principal.getName()));
    }


    @PostMapping(value = "/status")
    public ResponseEntity<?> setStatus(@RequestParam String machineId, @RequestParam StatoMacchinetta status) {
        addettoService.setStatus(machineId, status);
        return ResponseEntity.ok().build();
    }


    @PostMapping(value = "/restore-supplies", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> restoreSupplies(@RequestParam String machineId) {
        addettoService.restoreSupplies(machineId);
        return ResponseEntity.ok().build();
    }


    @GetMapping(value = "/machinesxml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<MachineListXmlDTO> getMachineList() {
        return ResponseEntity.ok(addettoService.buildMachineStatusXml());
    }
}