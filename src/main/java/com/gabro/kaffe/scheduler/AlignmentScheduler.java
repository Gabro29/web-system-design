package com.gabro.kaffe.scheduler;

import com.gabro.kaffe.service.impl.MachineOperationalService;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;


@Component
public class AlignmentScheduler {

    private final MachineOperationalService machineOps;

    public AlignmentScheduler(MachineOperationalService machineOps) {
        this.machineOps = machineOps;
    }


    @Scheduled(fixedRate = 300000, initialDelay = 60000)
    public void runAntiEntropy() {
        try {
            machineOps.syncAllMachines();
        } catch (Exception e) {
            System.err.println("Errore durante l'auto-sync:" + e.getMessage());
        }
    }
}