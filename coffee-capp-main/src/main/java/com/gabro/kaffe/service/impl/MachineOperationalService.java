package com.gabro.kaffe.service.impl;

import com.gabro.kaffe.dto.xml.*;
import com.gabro.kaffe.entity.*;
import com.gabro.kaffe.repository.*;
import com.gabro.kaffe.exception.CustomLogicException;
import com.gabro.kaffe.exception.ResourceNotFoundException;
import com.gabro.kaffe.util.Costanti;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class MachineOperationalService {

    private final MacchinettaRepository macchinettaRepository;
    private final ProdottoRepository prodottoRepository;
    private final UtenteRepository utenteRepository;
    private final StatisticaErogazioneRepository statisticaRepository;

    @Value("${app.jakarta.sync-url}")
    private String jakartaSyncUrl;

    @Value("${app.geocoding.url}")
    private String nominatimApiUrl;


    public MachineOperationalService(MacchinettaRepository macchinettaRepository, ProdottoRepository prodottoRepository, UtenteRepository utenteRepository, StatisticaErogazioneRepository statisticaRepository) {
        this.macchinettaRepository = macchinettaRepository;
        this.prodottoRepository = prodottoRepository;
        this.utenteRepository = utenteRepository;
        this.statisticaRepository = statisticaRepository;
    }


    @Transactional
    public void ensureMachineRegistration(String code, String place) {
        if (code == null || !code.matches("^[A-Z]{2}[0-9]{3}[A-Z]{2}$")) {
            throw new CustomLogicException("Formato code invalido (es: AA111AA)");
        }

        if (!macchinettaRepository.existsById(code)) {
            if (place == null || place.trim().isEmpty() || "--".equals(place)) {
                throw new CustomLogicException("Richiesta nuova macchina: manca place");
            }
            createMachine(code, place);
        }
    }


    public MachineListXmlDTO generateMachinesXml() {
        List<Macchinetta> macchinette = macchinettaRepository.findAll();
        List<Prodotto> tuttiIProdotti = prodottoRepository.findAll();
        List<MachineXmlDTO> xmlList = macchinette.stream()
                .map(m -> mapToXmlDTO(m, tuttiIProdotti))
                .collect(Collectors.toList());
        return new MachineListXmlDTO(xmlList);
    }


    public AddettoListXmlDTO generateAddettiXml() {
        List<Utente> addetti = utenteRepository.findByRuolo(Ruolo.ADDETTO);
        List<AddettoXmlDTO> dtoList = addetti.stream()
                .map(u -> new AddettoXmlDTO(u.getEmail()))
                .collect(Collectors.toList());
        return new AddettoListXmlDTO(dtoList);
    }


    @Transactional
    public void changeStatus(String machineCode, StatoMacchinetta newStatus) {
        Macchinetta m = findByCode(machineCode);
        m.setStatus(newStatus);
        if (newStatus != StatoMacchinetta.ACTIVE) {
            m.setConnectedUser(null);
        }
        macchinettaRepository.save(m);
        syncWithJakarta("SAVE", m.getCode());
    }


    public void syncAllMachines() {
        List<Macchinetta> allMachines = macchinettaRepository.findAll();
        for (Macchinetta m : allMachines) {
            syncWithJakarta("SAVE", m.getCode());
        }
    }


    @Transactional
    public void refillSupplies(String machineCode) {
        Macchinetta m = findByCode(machineCode);
        for (Fornitura f : m.getForniture()) {
            f.setLivello(f.getCapacitaMassima());
        }
        m.getMessaggi().clear();
        m.setStatus(StatoMacchinetta.ACTIVE);
        macchinettaRepository.save(m);
        syncWithJakarta("SAVE", m.getCode());
    }


    @Transactional
    public void deleteMachine(String code) {
        Macchinetta macchinetta = macchinettaRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("Macchinetta " + code + " non trovata."));
        if (macchinetta.getStatus() != StatoMacchinetta.MAINTENANCE) {
            throw new CustomLogicException("Impossibile eliminare la macchinetta. Deve essere in MAINTENANCE per essere rimossa.");
        }
        macchinettaRepository.deleteById(code);
        syncWithJakarta("DELETE", code);
    }


    @Transactional
    public void createMachine(String code, String place) {
        if (macchinettaRepository.existsById(code)) {
            throw new CustomLogicException("Esiste già una macchinetta con ID: " + code);
        }
        Macchinetta m = new Macchinetta();
        m.setCode(code);
        m.setLuogo(place);
        m.setStatus(StatoMacchinetta.MAINTENANCE);
        List<Fornitura> fornitureIniziali = createDefaultSupplies(m);
        m.setForniture(fornitureIniziali);
        macchinettaRepository.save(m);
        syncWithJakarta("SAVE", m.getCode());
    }


    private List<Fornitura> createDefaultSupplies(Macchinetta nuovaMacchinetta) {
        List<Fornitura> nuoveForniture = new ArrayList<>();

        Optional<Macchinetta> templateMachine = macchinettaRepository.findAll(PageRequest.of(0, 1))
                .stream()
                .findFirst();

        assert templateMachine.isPresent();
        List<Fornitura> fornitureTemplate = templateMachine.get().getForniture();

        for (Fornitura f : fornitureTemplate) {
            nuoveForniture.add(new Fornitura(
                    null,
                    f.getNome(),
                    f.getCapacitaMassima(),
                    f.getCapacitaMassima(),
                    f.getSogliaAttenzione(),
                    f.getUnita(),
                    nuovaMacchinetta
            ));
        }
        return nuoveForniture;
    }


    @Transactional
    public void deleteAddetto(String email) {
        Utente u = utenteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Addetto non trovato: " + email));
        if (u.getRuolo() != Ruolo.ADDETTO) {
            throw new CustomLogicException("L'utente selezionato non è un addetto.");
        }
        utenteRepository.delete(u);
    }


    @Transactional
    public void createAddetto(String email, String password) {
        if (utenteRepository.existsByEmail(email)) {
            throw new CustomLogicException("Esiste già un addetto con email: " + email);
        }
        Utente nuovoAddetto = new Utente();
        nuovoAddetto.setEmail(email);
        nuovoAddetto.setPassword(password);
        nuovoAddetto.setRuolo(Ruolo.ADDETTO);
        nuovoAddetto.setCredito(BigDecimal.ZERO);
        utenteRepository.save(nuovoAddetto);
    }


    public Macchinetta findByCode(String code) {
        return macchinettaRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Macchinetta non trovata"));
    }


    private MachineXmlDTO mapToXmlDTO(Macchinetta m, List<Prodotto> prodottiDisponibili) {
        MachineXmlDTO dto = new MachineXmlDTO();
        dto.setMachineId(m.getCode());
        dto.setRequestTime(LocalDateTime.now());
        dto.setMachinePlace(m.getLuogo());


        boolean bicchieriEsauriti = m.getForniture().stream()
                .anyMatch(f -> f.getNome().equalsIgnoreCase(Costanti.FORNITURA_BICCHIERI) && f.getLivello() <= 0);

        if (m.getStatus() == StatoMacchinetta.ACTIVE && bicchieriEsauriti) {
            dto.setMachineStatus(StatoMacchinetta.ERROR);
        } else {
            dto.setMachineStatus(m.getStatus());
        }

        dto.setConnectedUser(m.getConnectedUser() != null ? m.getConnectedUser().getEmail() : null);


        List<SupplyXmlDTO> supplies = m.getForniture().stream()
                .map(f -> {
                    int capacita = f.getCapacitaMassima();
                    return new SupplyXmlDTO(
                            f.getNome(),
                            f.getLivello(),
                            capacita,
                            f.getUnita(),
                            f.getSogliaAttenzione()
                    );
                })
                .collect(Collectors.toList());
        dto.setSupplyList(new SupplyListXmlWrapper(supplies));


        List<MessageXmlDTO> messages = m.getMessaggi().stream()
                .map(msg -> new MessageXmlDTO(
                        msg.getTipo(),
                        msg.getTesto(),
                        msg.getTimestamp()
                ))
                .collect(Collectors.toList());
        dto.setMessageList(new MessageListXmlWrapper(messages));

        List<StatisticaErogazione> statsMacchinetta = statisticaRepository.findByMacchinetta(m);

        List<ProductXmlDTO> productXmlList = prodottiDisponibili.stream()
                .map(p -> {
                    int realTimesServed = statsMacchinetta.stream()
                            .filter(s -> s.getProdotto().getId().equals(p.getId()))
                            .findFirst()
                            .map(StatisticaErogazione::getConteggio)
                            .orElse(0);

                    return new ProductXmlDTO(
                            p.getCategoria(),
                            p.getNome(),
                            p.getPrezzo(),
                            p.isAvailable(),
                            realTimesServed
                    );
                })
                .collect(Collectors.toList());

        dto.setProductList(new ProductListXmlWrapper(productXmlList));

        return dto;
    }


    private void syncWithJakarta(String action, String macchinettaCode) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("action", action);
            map.add("code", macchinettaCode);

            if ("SAVE".equals(action)) {
                Macchinetta m = findByCode(macchinettaCode);
                map.add("status", m.getStatus().name());

                double[] coords = getDynamicCoordinates(m.getLuogo());
                map.add("lat", String.valueOf(coords[0]));
                map.add("lng", String.valueOf(coords[1]));
            }

            restTemplate.postForObject(jakartaSyncUrl, map, String.class);
            // String response = restTemplate.postForObject(jakartaSyncUrl, map, String.class);
            // System.out.println("Sync Jakarta [" + action + " " + macchinettaCode + "]: " + response);

        } catch (Exception e) {
            System.err.println("ATTENZIONE: Jakarta non raggiungibile. Sync fallita: " + e.getMessage());
        }
    }


    private double[] getDynamicCoordinates(String query) {
        try {
            if (query == null || query.isEmpty()) return new double[]{0.0, 0.0};

            String cleanQuery = query.split("-")[0].trim();
            String search = cleanQuery + ", Viale delle Scienze, Palermo";
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "KaffeApp-StudentProject");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = nominatimApiUrl + search.replace(" ", "+");

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            if (root.isArray() && !root.isEmpty()) {
                JsonNode firstResult = root.get(0);
                double lat = firstResult.get("lat").asDouble();
                double lon = firstResult.get("lon").asDouble();
                // System.out.println("Coordinate trovate per '" + query + "': " + lat + ", " + lon);
                return new double[]{lat, lon};
            }

        } catch (Exception e) {
            System.err.println("Errore Geocoding: " + e.getMessage());
        }
        return new double[]{38.10400, 13.34800};
    }
}