package org.group6.childrenelectricvehiclerentalmanagement.Utility.Config;

import jakarta.servlet.http.Cookie;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Filters.JwtAuthenticationFilter;
import org.group6.childrenelectricvehiclerentalmanagement.Utility.Filters.RatelimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CookieEncoder cookieEncoder(){
        return new CookieEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, RatelimitFilter ratelimitFilter, JwtAuthenticationFilter jwtAuthenticationFilter)  {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(ratelimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, RatelimitFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/vehicle/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/sessions/start").permitAll()
                        .requestMatchers(HttpMethod.POST, "/sessions/current/end").permitAll()
                        .requestMatchers(HttpMethod.GET, "/sessions/current").permitAll()
                        .requestMatchers(HttpMethod.GET, "/sessions").permitAll()
                        .requestMatchers(HttpMethod.GET, "/sessions/{id}").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/sessions/{id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/sessions/{id}/cancel").permitAll()
                        .requestMatchers(HttpMethod.POST, "/sessions/current/vehicles").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/sessions/current/vehicles/{vehicle_id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                );
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
