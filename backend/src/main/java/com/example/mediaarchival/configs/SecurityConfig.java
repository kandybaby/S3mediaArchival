package com.example.mediaarchival.configs;

import com.example.mediaarchival.filters.JwtValidationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the Spring Security framework.
 * This configuration sets up the authentication and authorization policies for the application.
 */
@Configuration
public class SecurityConfig {

  /**
   * Configures a password encoder.
   *
   * @return A PasswordEncoder instance.
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Configures the default security filter chain.
   *
   * @param http The HttpSecurity object to be configured.
   * @return A configured SecurityFilterChain.
   * @throws Exception If an error occurs during the configuration.
   */
  @Bean
  public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
    JwtValidationFilter jwtValidationFilter = new JwtValidationFilter();
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            exception ->
                exception.authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .authorizeHttpRequests(
            authorizeRequests ->
                authorizeRequests
                    // Permit all for these specific auth endpoints
                    .requestMatchers("/api/users/authenticate", "/api/users/refresh-token")
                    .permitAll()
                    // Secure all other API endpoints
                    .requestMatchers("/api/**")
                    .authenticated()
                    // Permit all other requests
                    .anyRequest()
                    .permitAll())
        .addFilterBefore(jwtValidationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
