package com.gabro.kaffe.service.impl;

import com.gabro.kaffe.dto.MacchinettaPollDTO;
import com.gabro.kaffe.dto.MacchinettaStatusDTO;
import com.gabro.kaffe.dto.ProdottoDTO;
import com.gabro.kaffe.dto.PurchaseRequestDTO;

import com.gabro.kaffe.entity.*;
import com.gabro.kaffe.exception.*;
import com.gabro.kaffe.repository.*;

import com.gabro.kaffe.service.MacchinettaService;

import com.gabro.kaffe.util.Costanti;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class MacchinettaServiceImpl implements MacchinettaService {


    private final MacchinettaRepository macchinettaRepository;
    private final ProdottoRepository prodottoRepository;
    private final UtenteRepository utenteRepository;
    private final StatisticaErogazioneRepository statisticaRepository;


    public MacchinettaServiceImpl(MacchinettaRepository macchinettaRepository, ProdottoRepository prodottoRepository, UtenteRepository utenteRepository, StatisticaErogazioneRepository statisticaRepository) {
        this.macchinettaRepository = macchinettaRepository;
        this.prodottoRepository = prodottoRepository;
        this.utenteRepository = utenteRepository;
        this.statisticaRepository = statisticaRepository;
    }


    @Override
    public MacchinettaStatusDTO getMacchinettaStatus(String code) {
        if (code == null || code.trim().isEmpty()) {
            return new MacchinettaStatusDTO(null, false, "Codice mancante nella URL", null);
        }

        Macchinetta macchinetta = macchinettaRepository.findByCode(code).orElse(null);

        List<Prodotto> prodottiDisponibili = prodottoRepository.findByAvailableTrue();
        List<ProdottoDTO> prodottiDTO = prodottiDisponibili.stream()
                .map(p -> new ProdottoDTO(p.getId(), p.getNome(), p.getPrezzo(), p.getImagePath()))
                .collect(Collectors.toList());

        if (macchinetta == null || macchinetta.getStatus() != StatoMacchinetta.ACTIVE) {
            String motivo = (macchinetta == null) ? "Inizializzazione..." :
                    switch (macchinetta.getStatus()) {
                        case MAINTENANCE -> "Macchinetta in manutenzione";
                        case ERROR -> "Macchinetta fuori servizio";
                        case OFFLINE -> "Macchinetta non raggiungibile";
                        default -> "Macchinetta non disponibile";
                    };
            return new MacchinettaStatusDTO(code, false, motivo, prodottiDTO);
        }

        return new MacchinettaStatusDTO(code, true, null, prodottiDTO);
    }


    @Override
    @Transactional
    public MacchinettaPollDTO getPollStatus(String code) {
        Macchinetta macchinetta = findMacchinettaByCode(code);
        handleUserTimeout(macchinetta);
        return mapToPollDTO(macchinetta);
    }


    private void handleUserTimeout(Macchinetta macchinetta) {
        if (macchinetta.getConnectedUser() == null) {
            return;
        }

        LocalDateTime lastInteraction = macchinetta.getLastUserInteraction();
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(Costanti.TIMEOUT_SESSIONE_MINUTI);

        if (lastInteraction == null || lastInteraction.isBefore(timeoutThreshold)) {
            macchinetta.setConnectedUser(null);
            macchinetta.setLastUserInteraction(null);
            macchinettaRepository.save(macchinetta);
        }
    }


    private MacchinettaPollDTO mapToPollDTO(Macchinetta macchinetta) {
        MacchinettaPollDTO dto = new MacchinettaPollDTO();
        dto.setMachineCode(macchinetta.getCode());

        Utente connectedUser = macchinetta.getConnectedUser();
        boolean isConnected = (connectedUser != null);

        dto.setUserConnected(isConnected);

        dto.setUserCredit(isConnected ? connectedUser.getCredito() : BigDecimal.ZERO);

        String username = isConnected ? extractUsername(connectedUser.getEmail()) : null;
        dto.setUserName(username);

        dto.setStatus(macchinetta.getStatus());

        return dto;
    }


    @Override
    @Transactional(noRollbackFor = { MacchinettaOutServiceException.class })
    public void erogaBevanda(PurchaseRequestDTO request) {

        // Controlli

        validatePurchaseRequest(request);

        Macchinetta macchinetta = findMacchinettaByCode(request.getMachineCode());

        Utente utente = requireConnectedUser(macchinetta);

        Prodotto prodotto = prodottoRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Prodotto non trovato"));

        ensureProductAvailable(prodotto);
        ensureSufficientCredit(utente, prodotto);
        assertBicchieriOrMarkOutOfService(macchinetta);

        List<Ingrediente> listaIngredienti = prodotto.getIngredienti();
        if (listaIngredienti != null) {
            for (Ingrediente ing : listaIngredienti) {
                if (ing == null) continue;
                assertDisponibilitaIngrediente(macchinetta, ing.getNomeFornitura(), ing.getQuantita());
            }
        }

        int zuccheroDaScalare = gestisciLogicaZucchero(macchinetta, prodotto, request.getSugarLevel());

        // Controlli superati

        macchinetta.setLastUserInteraction(LocalDateTime.now());

        decrementaFornitura(macchinetta, Costanti.FORNITURA_BICCHIERI, Costanti.PESO_BICCHIERE_GRAMMI);

        if (zuccheroDaScalare > 0) {
            decrementaFornitura(macchinetta, Costanti.FORNITURA_ZUCCHERO, zuccheroDaScalare);
        }

        if (listaIngredienti != null) {
            for (Ingrediente ing : listaIngredienti) {
                if (ing == null) continue;
                decrementaFornitura(macchinetta, ing.getNomeFornitura(), ing.getQuantita());
            }
        }

        utente.setCredito(utente.getCredito().subtract(prodotto.getPrezzo()));
        aggiornaStatistiche(macchinetta, prodotto);

        utenteRepository.save(utente);
        macchinettaRepository.save(macchinetta);
    }


    private void validatePurchaseRequest(PurchaseRequestDTO request) {
        if (request == null) {
            throw new CustomLogicException("Richiesta mancante.");
        }
        if (request.getMachineCode() == null || request.getMachineCode().trim().isEmpty()) {
            throw new CustomLogicException("Codice Macchinetta mancante.");
        }
        if (request.getProductId() == null) {
            throw new CustomLogicException("Prodotto mancante.");
        }
        if (request.getSugarLevel() < 0 || request.getSugarLevel() > 3) {
            throw new CustomLogicException("Livello zucchero non valido.");
        }
    }


    private Utente requireConnectedUser(Macchinetta macchinetta) {
        Utente utente = macchinetta.getConnectedUser();
        if (utente == null) {
            throw new CustomLogicException("Nessun utente connesso.");
        }
        return utente;
    }


    private void ensureProductAvailable(Prodotto prodotto) {
        if (!prodotto.isAvailable()) {
            throw new CustomLogicException("Il prodotto selezionato non è disponibile al momento.");
        }
    }


    private void ensureSufficientCredit(Utente utente, Prodotto prodotto) {
        if (utente.getCredito() == null) {
            throw new CustomLogicException("Credito utente non disponibile.");
        }
        if (utente.getCredito().compareTo(prodotto.getPrezzo()) < 0) {
            throw new CustomLogicException("Credito insufficiente.");
        }
    }


    private int computeZuccheroQty(int sugarLevel) {
        return Math.max(0, sugarLevel) * Costanti.GRAMMI_ZUCCHERO_PER_LIVELLO;
    }


    private boolean isZuccheroAllowed(Prodotto p) {
        if (p.getCategoria() == null) return true;
        return !"Bevanda Fredda".equalsIgnoreCase(p.getCategoria());
    }


    private void assertBicchieriOrMarkOutOfService(Macchinetta macchinetta) {
        Fornitura bicchieri = findFornitura(macchinetta, Costanti.FORNITURA_BICCHIERI);

        if (bicchieri == null || bicchieri.getLivello() == null || bicchieri.getLivello() <= 0) {
            macchinetta.setStatus(StatoMacchinetta.ERROR);

            Messaggio guasto = new Messaggio();
            guasto.setTesto(Costanti.MSG_BICCHIERI_FINITI);
            guasto.setTipo(TipoMessaggio.ERROR);
            guasto.setTimestamp(LocalDateTime.now());
            guasto.setMacchinetta(macchinetta);

            macchinetta.getMessaggi().add(guasto);

            macchinettaRepository.save(macchinetta);

            throw new MacchinettaOutServiceException("Macchinetta Fuori Servizio: Bicchieri esauriti");
        }
    }


    private void assertDisponibilitaIngrediente(Macchinetta macchinetta, String nomeFornitura, int quantita) {
        if (quantita <= 0) return;

        Fornitura f = findFornitura(macchinetta, nomeFornitura);
        if (f == null || f.getLivello() == null || f.getLivello() < quantita) {

            String msgTesto = Costanti.MSG_FORNITURA_TERMINATA + nomeFornitura;
            addMessage(macchinetta, TipoMessaggio.ERROR, msgTesto);
            macchinettaRepository.save(macchinetta);

            throw new CustomLogicException("Bevanda non disponibile: " + nomeFornitura + " esaurito.");
        }
    }


    private int gestisciLogicaZucchero(Macchinetta macchinetta, Prodotto prodotto, int livelloRichiesto) {
        if (!isZuccheroAllowed(prodotto)) {
            return 0;
        }

        int zuccheroRichiesto = computeZuccheroQty(livelloRichiesto);
        if (zuccheroRichiesto <= 0) {
            return 0;
        }

        Fornitura fornituraZucchero = findFornitura(macchinetta, Costanti.FORNITURA_ZUCCHERO);
        if (fornituraZucchero != null && fornituraZucchero.getLivello() >= zuccheroRichiesto) {
            return zuccheroRichiesto;
        }

        String msgWarning = Costanti.FORNITURA_ZUCCHERO + " terminato (Erogazione senza zucchero)";
        boolean avvisoGiaPresente = macchinetta.getMessaggi().stream()
                .anyMatch(msg -> msg.getTipo() == TipoMessaggio.WARNING && msg.getTesto().equals(msgWarning));

        if (!avvisoGiaPresente) {
            addMessage(macchinetta, TipoMessaggio.WARNING, msgWarning);
        }

        return 0;
    }


    private void decrementaFornitura(Macchinetta macchinetta, String nomeFornitura, int quantita) {
        if (quantita <= 0) return;

        Fornitura f = findFornitura(macchinetta, nomeFornitura);
        assert f != null;
        f.setLivello(f.getLivello() - quantita);
        if (f.getLivello() <= f.getSogliaAttenzione()) {
            boolean warningGiaPresente = macchinetta.getMessaggi().stream()
                    .anyMatch(msg -> msg.getTipo() == TipoMessaggio.WARNING && msg.getTesto().contains(nomeFornitura + Costanti.MSG_FORNITURA_IN_ESAURIMENTO));

            if (!warningGiaPresente) {
                addMessage(macchinetta, TipoMessaggio.WARNING, nomeFornitura + Costanti.MSG_FORNITURA_IN_ESAURIMENTO);
            }
        }
    }


    public void aggiornaStatistiche(Macchinetta m, Prodotto p) {

        Optional<StatisticaErogazione> statOpt = statisticaRepository.findByMacchinettaAndProdotto(m, p);

        if (statOpt.isPresent()) {
            StatisticaErogazione stat = statOpt.get();
            stat.setConteggio(stat.getConteggio() + 1);
            statisticaRepository.save(stat);
        } else {
            StatisticaErogazione nuovaStat = new StatisticaErogazione(m, p, 1);
            statisticaRepository.save(nuovaStat);
        }
    }


    private void addMessage(Macchinetta m, TipoMessaggio tipo, String testo) {
        Messaggio msg = new Messaggio();
        msg.setMacchinetta(m);
        msg.setTipo(tipo);
        msg.setTesto(testo);
        msg.setTimestamp(LocalDateTime.now());

        m.getMessaggi().add(msg);
    }


    private Fornitura findFornitura(Macchinetta macchinetta, String nomeFornitura) {
        if (macchinetta.getForniture() == null) return null;

        return macchinetta.getForniture().stream()
                .filter(f -> f != null && f.getNome() != null && f.getNome().equalsIgnoreCase(nomeFornitura))
                .findFirst()
                .orElse(null);
    }


    private String extractUsername(String email) {
        if (email == null || !email.contains("@")) return "Utente";
        return email.split("@")[0];
    }


    private Macchinetta findMacchinettaByCode(String code) {
        return macchinettaRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Macchinetta non trovata (Codice: " + code + ")"));
    }
}