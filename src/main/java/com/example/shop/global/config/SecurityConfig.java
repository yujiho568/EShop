package com.example.shop.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf((csrf) -> csrf.disable());
        http.authorizeHttpRequests((authorize) ->
                authorize.requestMatchers("/login", "/register", "/main.css", "/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated()
        );
        http.formLogin((formLogin) -> formLogin.loginPage("/login")
                .defaultSuccessUrl("/list")
                .failureUrl("/fail")
        );
        http.logout(logout -> logout.logoutUrl("/logout"));
        http.csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository())
                .ignoringRequestMatchers("/login")
        );
        return http.build();
    }
}
