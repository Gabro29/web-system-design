package com.gabro.kaffe.controller;

import com.gabro.kaffe.dto.GestoreDTO;
import com.gabro.kaffe.dto.xml.AddettoListXmlDTO;
import com.gabro.kaffe.dto.xml.MachineListXmlDTO;
import com.gabro.kaffe.entity.StatoMacchinetta;
import com.gabro.kaffe.service.GestoreService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import java.security.Principal;


@RestController
@RequestMapping("/api/gestore")
public class GestoreRestController {


    private final GestoreService gestoreService;


    public GestoreRestController(GestoreService gestoreService) {
        this.gestoreService = gestoreService;
    }


    @GetMapping(value = "/me", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<GestoreDTO> getSessionData(Principal principal) {
        return ResponseEntity.ok(gestoreService.getStato(principal.getName()));
    }


    @GetMapping(value = "/machinesxml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<MachineListXmlDTO> getMachineList() {
        return ResponseEntity.ok(gestoreService.buildMachineStatusXml());
    }


    @GetMapping(value = "/addettixml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<AddettoListXmlDTO> getAddettiList() {
        return ResponseEntity.ok(gestoreService.buildAddettiXml());
    }


    @PostMapping(value = "/machines/status")
    public ResponseEntity<?> setMachineStatus(@RequestParam String machineId, @RequestParam StatoMacchinetta status) {
        gestoreService.setMachineStatus(machineId, status);
        return ResponseEntity.ok().build();
    }


    @PostMapping(value = "/machines/delete")
    public ResponseEntity<?> removeMachine(@RequestParam String machineId) {
        gestoreService.removeMachine(machineId);
        return ResponseEntity.ok().build();
    }


    @PostMapping(value = "/machines/add")
    public ResponseEntity<?> addMachine(@RequestParam String machineId, @RequestParam String machinePlace) {
        gestoreService.addMachine(machineId, machinePlace);
        return ResponseEntity.ok().build();
    }


    @PostMapping(value = "/addetto/delete")
    public ResponseEntity<?> removeAddetto(@RequestParam String email) {
        gestoreService.removeAddetto(email);
        return ResponseEntity.ok().build();
    }


    @PostMapping(value = "/addetto/add")
    public ResponseEntity<?> addAddetto(@RequestParam String email, @RequestParam String password) {
        gestoreService.addAddetto(email, password);
        return ResponseEntity.ok().build();
    }
}