package com.example.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/rooms", "/rooms/**", "/room-types", "/room-types/**","/").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/reception/**").hasAnyRole("ADMIN", "RECEPTIONIST")
                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                .requestMatchers("/booking/**").hasAnyRole("ADMIN", "RECEPTIONIST", "CUSTOMER")
                .anyRequest().authenticated()
            )


            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(roleBasedSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/error/403"));
            // CSRF protection is left enabled (Spring Security's default).
            // Thymeleaf's th:action on <form> tags automatically injects the
            // CSRF token, so no extra configuration is needed here.

        return http.build();
    }








     // Redirects each role to its own dashboard immediately after a successful login
    private AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) -> {
            String redirectUrl = authentication.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .findFirst()
                    .map(role -> switch (role) {
                        case "ROLE_ADMIN" -> "/admin/dashboard";
                        case "ROLE_RECEPTIONIST" -> "/reception/dashboard";
                        case "ROLE_CUSTOMER" -> "/customer/dashboard";
                        default -> "/";
                    })
                    .orElse("/");
            response.sendRedirect(redirectUrl);
        };
    }
}
