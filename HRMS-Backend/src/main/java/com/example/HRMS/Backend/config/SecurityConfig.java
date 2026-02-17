package com.example.HRMS.Backend.config;

import com.example.HRMS.Backend.security.JwtAuthenticationFilter;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    private static final String[] COMMON_ROLES = { "ROLE_HR", "ROLE_MANAGER", "ROLE_EMPLOYEE", "ROLE_ADMIN" };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {

        http.cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth ->
             auth.requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/swagger-ui/**").permitAll()
            .requestMatchers("/uploads/**").permitAll()
            .requestMatchers("/v3/api-docs/**").permitAll()
            .requestMatchers("/api/admin/**")
                     .hasAnyAuthority("ROLE_ADMIN")
            .requestMatchers("/api/hr/**")
                     .hasAnyAuthority("ROLE_HR", "ROLE_ADMIN")
            .requestMatchers("/api/org-chart/**", "/api/gameType/**", "/api/travel/**",
                    "/api/game/**", "/api/job/**", "/api/post/**" , "/api/user/**",
                    "/api/notification/**")
            .hasAnyAuthority(COMMON_ROLES)
            .requestMatchers("/api/manager/**")
                     .hasAnyAuthority("ROLE_MANAGER", "ROLE_ADMIN")
            .requestMatchers("/api/employee/**")
                     .hasAnyAuthority("ROLE_EMPLOYEE", "ROLE_ADMIN")
            .anyRequest().authenticated())
            .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
