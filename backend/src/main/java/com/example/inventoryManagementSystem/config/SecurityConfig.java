package com.example.inventoryManagementSystem.config;

import com.example.inventoryManagementSystem.controller.auth.JwtAuthFilter;
import com.example.inventoryManagementSystem.service.impl.UserDetailsServiceImpl;
import com.example.inventoryManagementSystem.util.JwtUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        return new JwtAuthFilter(jwtUtils, userDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthFilter jwtAuthFilter
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").hasAuthority("ADMIN")
                        .requestMatchers("/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers("/api/settings/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/cart").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/cart").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/cart/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/cart/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/cart/apply-discount").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/cart/checkout").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}