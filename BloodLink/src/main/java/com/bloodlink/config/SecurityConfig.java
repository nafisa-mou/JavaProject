package com.bloodlink.config;

import com.bloodlink.security.CustomUserDetailsService;
import com.bloodlink.security.JwtAuthenticationFilter;
import com.bloodlink.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * SecurityConfig - Spring Security configuration for BloodLink
 * 
 * Features:
 * - JWT stateless authentication (no sessions)
 * - Role-based access control (@PreAuthorize)
 * - Method-level security
 * - Password encoding with BCrypt
 * - CORS configuration for frontend
 * - Public/protected endpoint definitions
 * - HTTP security policies
 * 
 * Authentication Flow:
 * 1. Client sends login request with email/password
 * 2. AuthService validates credentials via DaoAuthenticationProvider
 * 3. JwtTokenProvider generates JWT token
 * 4. Client stores token in localStorage
 * 5. Client sends token in Authorization header for subsequent requests
 * 6. JwtAuthenticationFilter validates token
 * 7. User authenticated in SecurityContext
 * 8. @PreAuthorize checks user role for endpoint access
 * 
 * OOP Principle: Encapsulation - Security config isolated
 * Design Pattern: Strategy pattern (multiple authentication providers)
 * Best Practice: Stateless JWT authentication for REST APIs
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Password encoder bean - Use BCrypt for password hashing
     * 
     * @return BCryptPasswordEncoder with strength 12
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Authentication provider - DAO-based with custom user details service
     * 
     * @return DaoAuthenticationProvider configured with user details service
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication manager bean
     * 
     * @param config AuthenticationConfiguration
     * @return AuthenticationManager
     * @throws Exception if configuration error
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * JWT authentication filter bean
     * 
     * @return JwtAuthenticationFilter
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService);
    }

    /**
     * CORS configuration - Allow frontend origins to access API
     * 
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allowed origins
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",   // React dev server
            "http://localhost:8080",   // Another dev instance
            "http://localhost:4200"    // Angular dev server
        ));
        
        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "X-Requested-With"
        ));
        
        // Exposed headers (for client to read)
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type"
        ));
        
        // Credentials
        configuration.setAllowCredentials(true);
        
        // Max age for preflight cache (12 hours)
        configuration.setMaxAge(43200L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * HTTP security configuration - Define endpoint access rules
     * 
     * Security Rules:
     * - Public endpoints: Auth endpoints, health check
     * - Protected endpoints: All others require authentication
     * - Admin endpoints: Require ADMIN role
     * - Donor endpoints: Require DONOR role
     * - Patient endpoints: Require PATIENT role
     * 
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception if configuration error
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless API (JWT used instead)
            .csrf().disable()
            
            // Enable CORS
            .cors().and()
            
            // Use stateless session (no server-side sessions)
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            
            // Exception handling
            .exceptionHandling()
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                .accessDeniedHandler(new JwtAccessDeniedHandler())
            .and()
            
            // Configure endpoint access
            .authorizeHttpRequests(authz -> authz
                
                // PUBLIC ENDPOINTS - No authentication required
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register-donor").permitAll()
                .requestMatchers("/api/auth/register-patient").permitAll()
                .requestMatchers("/api/auth/refresh-token").permitAll()
                .requestMatchers("/api/auth/forgot-password").permitAll()
                .requestMatchers("/api/auth/reset-password").permitAll()
                .requestMatchers("/api/auth/health").permitAll()
                
                // SWAGGER ENDPOINTS
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                
                // ACTUATOR ENDPOINTS (Health checks)
                .requestMatchers("/actuator/**").permitAll()
                
                // WebSocket endpoints
                .requestMatchers("/ws/**").permitAll()
                
                // DONOR ENDPOINTS - Require DONOR role
                .requestMatchers(HttpMethod.GET, "/api/donors").hasAnyRole("DONOR", "PATIENT", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/donors/**").hasAnyRole("DONOR", "PATIENT", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/donors/**").hasAnyRole("DONOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/donors/**").hasAnyRole("ADMIN")
                
                // PATIENT ENDPOINTS - Require PATIENT role
                .requestMatchers(HttpMethod.GET, "/api/patients").hasAnyRole("PATIENT", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/patients/**").hasAnyRole("PATIENT", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/patients/**").hasAnyRole("PATIENT", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/patients/**").hasAnyRole("PATIENT", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/patients/**").hasAnyRole("PATIENT", "ADMIN")
                
                // BLOOD REQUEST ENDPOINTS
                .requestMatchers(HttpMethod.GET, "/api/requests").hasAnyRole("DONOR", "PATIENT", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/requests/**").hasAnyRole("DONOR", "PATIENT", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/requests/**").hasAnyRole("DONOR", "PATIENT", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/requests/**").hasAnyRole("DONOR", "PATIENT", "ADMIN")
                
                // CHAT ENDPOINTS - Require authentication
                .requestMatchers("/api/chats/**").authenticated()
                .requestMatchers("/api/messages/**").authenticated()
                
                // NOTIFICATION ENDPOINTS - Require authentication
                .requestMatchers("/api/notifications/**").authenticated()
                
                // SEARCH ENDPOINTS
                .requestMatchers(HttpMethod.GET, "/api/search/**").hasAnyRole("DONOR", "PATIENT", "ADMIN")
                
                // ADMIN ENDPOINTS - Require ADMIN role
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // Add JWT authentication filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            
            // Set authentication provider
            .authenticationProvider(authenticationProvider());
        
        return http.build();
    }
}
