package com.gabro.kaffe.security;

import com.gabro.kaffe.entity.Utente;
import com.gabro.kaffe.repository.UtenteRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtenteRepository utenteRepository;

    public CustomUserDetailsService(UtenteRepository utenteRepository) {
        this.utenteRepository = utenteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utente appUser = utenteRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato con email: " + email));

        String roleName = "ROLE_" + appUser.getRuolo().name();

        return new User(
                appUser.getEmail(),
                appUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(roleName))
        );
    }
}