package com.gabro.kaffe.controller;

import com.gabro.kaffe.dto.MacchinettaPollDTO;
import com.gabro.kaffe.dto.PurchaseRequestDTO;
import com.gabro.kaffe.entity.Ruolo;
import com.gabro.kaffe.service.MacchinettaService;
import com.gabro.kaffe.security.AuthHelper;
import com.gabro.kaffe.service.impl.MachineOperationalService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RestController
@RequestMapping("/api/macchinetta")
public class MacchinettaRestController {


    private final MacchinettaService macchinettaService;
    private final MachineOperationalService machineOps;
    private final AuthHelper authHelper;


    public MacchinettaRestController(MacchinettaService macchinettaService, MachineOperationalService machineOps, AuthHelper authHelper) {
        this.macchinettaService = macchinettaService;
        this.machineOps = machineOps;
        this.authHelper = authHelper;
    }


    @PostMapping("/auth")
    public ResponseEntity<String> autoLogin(@RequestParam String code, @RequestParam(required = false) String place, HttpServletRequest request) {

        machineOps.ensureMachineRegistration(code, place);

        authHelper.authenticateManually(code, Ruolo.MACCHINETTA.name(), request);

        return ResponseEntity.ok().build();
    }


    @GetMapping(value = "/poll", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<MacchinettaPollDTO> pollStatus(Principal principal) {
        String machineCode = principal.getName();
        return ResponseEntity.ok(macchinettaService.getPollStatus(machineCode));
    }


    @PostMapping("/purchase")
    public ResponseEntity<String> acquistaProdotto(@ModelAttribute PurchaseRequestDTO request, Principal principal) {
        request.setMachineCode(principal.getName());
        macchinettaService.erogaBevanda(request);
        return ResponseEntity.ok().build();
    }
}