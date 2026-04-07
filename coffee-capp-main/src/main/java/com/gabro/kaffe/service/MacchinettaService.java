package com.gabro.kaffe.service;

import com.gabro.kaffe.dto.MacchinettaPollDTO;
import com.gabro.kaffe.dto.MacchinettaStatusDTO;
import com.gabro.kaffe.dto.PurchaseRequestDTO;

public interface MacchinettaService {
    MacchinettaStatusDTO getMacchinettaStatus(String code);
    MacchinettaPollDTO getPollStatus(String code);
    void erogaBevanda(PurchaseRequestDTO request);
}