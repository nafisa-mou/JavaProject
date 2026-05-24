package com.bloodlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * BloodLink Application - Main Entry Point
 * 
 * This is the main Spring Boot application class that initializes the entire application.
 * 
 * Demonstrates:
 * - Spring Boot auto-configuration
 * - CORS configuration for frontend
 * - Component scanning
 */
@SpringBootApplication
public class BloodLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(BloodLinkApplication.class, args);
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║     🏥 BloodLink - Blood Donor Connection Platform 🏥     ║");
        System.out.println("║                                                           ║");
        System.out.println("║            Application started successfully!              ║");
        System.out.println("║         Visit: http://localhost:8080 to get started       ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
    }

    /**
     * Configure CORS for frontend-backend communication
     * Allows requests from frontend applications
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(
                            "http://localhost:3000",    // React dev server
                            "http://localhost:8080",    // Main application
                            "http://localhost:4200"     // Angular dev server
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
                
                // WebSocket CORS
                registry.addMapping("/ws/**")
                        .allowedOrigins(
                            "http://localhost:3000",
                            "http://localhost:8080",
                            "http://localhost:4200"
                        )
                        .allowedMethods("GET", "POST")
                        .allowCredentials(true);
            }
        };
    }
}
