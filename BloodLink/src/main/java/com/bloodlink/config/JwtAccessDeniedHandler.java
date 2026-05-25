package com.bloodlink.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JwtAccessDeniedHandler - Handle access denied (authorization) errors
 * 
 * Called when authenticated user lacks required permissions/roles for resource
 * Returns JSON error response instead of HTML error page (appropriate for REST API)
 * 
 * @author BloodLink Team
 */
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handle access denied
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param accessDeniedException Access denied exception
     * @throws IOException If I/O error occurs
     * @throws ServletException If servlet error occurs
     */
    @Override
    public void handle(HttpServletRequest request,
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) 
            throws IOException, ServletException {
        
        log.warn("Access denied for user: {} - {}", 
                request.getRemoteUser(), accessDeniedException.getMessage());
        
        // Set response type to JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        
        // Create error response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "FORBIDDEN");
        errorResponse.put("message", "You do not have permission to access this resource");
        errorResponse.put("status", HttpServletResponse.SC_FORBIDDEN);
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("path", request.getRequestURI());
        
        // Write error response
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
