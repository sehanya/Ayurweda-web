package com.example.ayurlink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
                        // Public pages accessible to everyone (logged in or not)
                        .requestMatchers("/", "/home", "/treatments", "/about", "/contact","/treatment/**").permitAll()

                        // ✅ AUTH PAGES - Unauthenticated users only
                        .requestMatchers("/login", "/register", "/perform-login", "/error").permitAll()
                        // ✅ Allow error and logout pages
                        .requestMatchers("/error", "/logout","/access-denied").permitAll()

                        // ✅ Authenticated users can go to dashboard entry
                        .requestMatchers("/dashboard").authenticated()

                        // Static resources
                        .requestMatchers("/css/**", "/js/**", "/images/**","webjars/**").permitAll()


                        // ✅ Role-specific areas
                        .requestMatchers("/patient/**").hasRole("PATIENT")
                        .requestMatchers("/doctor/**").hasRole("DOCTOR")
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN","SUPER_ADMIN")
                        .requestMatchers("/superadmin/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/payment/**").hasAnyRole("PATIENT", "ADMIN", "SUPER_ADMIN")

                        // All other requests need authentication
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform-login")
                        .defaultSuccessUrl("/dashboard", true) // AuthController redirects by role
                        .failureUrl("/login?error=true")
                        .successHandler(new CustomAuthenticationSuccessHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/access-denied")  // ✅ Custom access denied page
        );

        return http.build();
    }
}