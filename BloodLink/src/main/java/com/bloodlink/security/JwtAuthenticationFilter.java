package com.bloodlink.security;

import com.bloodlink.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JwtAuthenticationFilter - Validate JWT tokens in request headers
 * 
 * Executes once per request to:
 * 1. Extract JWT token from Authorization header
 * 2. Validate token signature and expiration
 * 3. Load user details from token
 * 4. Set authentication in SecurityContext
 * 5. Allow request to proceed to controller
 * 
 * Token Format: "Bearer eyJhbGciOiJIUzUxMiJ9..."
 * 
 * Features:
 * - Stateless authentication (no session required)
 * - Token refresh capability
 * - Graceful handling of invalid/expired tokens
 * - Detailed error logging
 * 
 * OOP Principle: Encapsulation - Token validation isolated
 * Design Pattern: Filter chain pattern
 * Best Practice: JWT stateless authentication
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Filter incoming requests for JWT token validation
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Filter chain
     * @throws ServletException If servlet error occurs
     * @throws IOException If I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        try {
            // Extract JWT token from request
            String jwt = getJwtFromRequest(request);

            // Validate token
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                
                // Extract user email from token
                String email = tokenProvider.getEmailFromToken(jwt);
                
                log.debug("JWT token validated for user: {}", email);

                // Load user details
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                // Create authentication object
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                
                // Set request details
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("User authenticated in security context: {}", email);

            } else if (StringUtils.hasText(jwt)) {
                log.warn("Invalid or expired JWT token detected");
            }

        } catch (Exception e) {
            log.error("Error during JWT authentication: {}", e.getMessage());
            // Continue with filter chain (non-authenticated request)
        }

        // Continue with filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     * 
     * Expected format: "Bearer <token>"
     * 
     * @param request HTTP request
     * @return JWT token, or null if not present
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }
        
        return null;
    }
}
