package com.gabro.kaffe.security;

import com.gabro.kaffe.dto.AddettoDTO;
import com.gabro.kaffe.dto.ClienteDTO;
import com.gabro.kaffe.dto.GestoreDTO;
import com.gabro.kaffe.dto.MacchinettaLoginDTO;
import com.gabro.kaffe.entity.Utente;
import com.gabro.kaffe.entity.Macchinetta;
import com.gabro.kaffe.repository.MacchinettaRepository;
import com.gabro.kaffe.repository.UtenteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.util.Optional;


@Configuration
@EnableWebSecurity
public class SecurityConfig {


    private final UtenteRepository utenteRepository;
    private final MacchinettaRepository macchinettaRepository;


    public SecurityConfig(UtenteRepository utenteRepository, MacchinettaRepository macchinettaRepository) {
        this.utenteRepository = utenteRepository;
        this.macchinettaRepository = macchinettaRepository;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/data/**", "/images/**").permitAll()

                        .requestMatchers("/", "/access-denied", "/customer", "/gestore", "/addetto", "/macchinetta").permitAll()

                        .requestMatchers("/api/cliente/register").permitAll()
                        .requestMatchers("/api/logout").permitAll()
                        .requestMatchers("/api/macchinetta/auth").permitAll()

                        .requestMatchers("/api/cliente/**").hasRole("CLIENTE")
                        .requestMatchers("/api/gestore/**").hasRole("GESTORE")
                        .requestMatchers("/api/addetto/**").hasRole("ADDETTO")
                        .requestMatchers("/api/macchinetta/**").hasRole("MACCHINETTA")

                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                response.setContentType("application/xml");
                                response.getWriter().write("<message>Non autorizzato: sessione scaduta o mancante</message>");
                            } else {
                                response.sendRedirect("/access-denied");
                            }
                        })

                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(HttpStatus.FORBIDDEN.value());
                                response.setContentType("application/xml");
                                response.getWriter().write("<message>Accesso Negato: permessi insufficienti, eventualmente ricarica la pagina</message>");
                            } else {
                                response.sendRedirect("/access-denied");
                            }
                        })
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/api/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(ajaxAuthenticationSuccessHandler())
                        .failureHandler(ajaxAuthenticationFailureHandler())
                )

                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .addLogoutHandler(customLogoutHandler())
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.OK.value());
                        })
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                );

        return http.build();
    }


    private AuthenticationSuccessHandler ajaxAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {

            String realRole = authentication.getAuthorities().iterator().next().getAuthority();
            String requiredRoleParam = request.getParameter("requiredRole");

            if (requiredRoleParam != null) {
                String expectedRole = "ROLE_" + requiredRoleParam;
                if (!realRole.equals(expectedRole)) {
                    request.getSession().invalidate();
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("application/xml");
                    response.getWriter().write("<message>Ruolo non autorizzato per questo portale</message>");
                    return;
                }
            }

            response.setStatus(HttpStatus.OK.value());
            response.setContentType("application/xml");
            String email = authentication.getName();
            String simpleName = email.contains("@") ? email.split("@")[0] : email;

            ObjectMapper xmlMapper = new XmlMapper();
            Object responseDto;

            if ("ROLE_CLIENTE".equals(realRole)) {
                Utente utente = utenteRepository.findByEmail(email).orElseThrow();
                String currentMachineId = null;
                Optional<Macchinetta> m = macchinettaRepository.findByConnectedUser(utente);
                if (m.isPresent()) {
                    currentMachineId = m.get().getCode();
                }
                responseDto = new ClienteDTO(email, simpleName, currentMachineId, utente.getCredito());

            } else if ("ROLE_GESTORE".equals(realRole)) {
                responseDto = new GestoreDTO(email, simpleName);

            } else if ("ROLE_ADDETTO".equals(realRole)) {
                responseDto = new AddettoDTO(email, simpleName);
            } else if ("ROLE_MACCHINETTA".equals(realRole)) {
                responseDto = new MacchinettaLoginDTO("OK");
            } else {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                response.getWriter().write("<error>Ruolo utente non supportato</error>");
                return;
            }
            xmlMapper.writeValue(response.getWriter(), responseDto);
        };
    }


    private AuthenticationFailureHandler ajaxAuthenticationFailureHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/xml");
            response.getWriter().write("<message>Credenziali non valide</message>");
        };
    }


    private LogoutHandler customLogoutHandler() {
        return (request, response, authentication) -> {
            if (authentication != null && authentication.getName() != null) {
                String email = authentication.getName();
                utenteRepository.findByEmail(email).ifPresent(utente -> {
                    if ("CLIENTE".equals(utente.getRuolo().name())) {
                        macchinettaRepository.findByConnectedUser(utente).ifPresent(macchinetta -> {
                            macchinetta.setConnectedUser(null);
                            macchinetta.setLastUserInteraction(null);
                            macchinettaRepository.save(macchinetta);
                        });
                    }
                });
            }
        };
    }
}
