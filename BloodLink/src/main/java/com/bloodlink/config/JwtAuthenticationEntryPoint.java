package com.bloodlink.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JwtAuthenticationEntryPoint - Handle unauthorized access attempts
 * 
 * Called when unauthenticated user attempts to access protected resource
 * Returns JSON error response instead of HTML login page (appropriate for REST API)
 * 
 * @author BloodLink Team
 */
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handle unauthorized access
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param authException Authentication exception
     * @throws IOException If I/O error occurs
     * @throws ServletException If servlet error occurs
     */
    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) 
            throws IOException, ServletException {
        
        log.error("Unauthorized access attempt: {} - {}", 
                 request.getRequestURI(), authException.getMessage());
        
        // Set response type to JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // Create error response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "UNAUTHORIZED");
        errorResponse.put("message", authException.getMessage());
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("path", request.getRequestURI());
        
        // Write error response
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
