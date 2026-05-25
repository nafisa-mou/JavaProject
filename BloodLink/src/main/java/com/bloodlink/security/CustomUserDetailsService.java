package com.bloodlink.security;

import com.bloodlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * CustomUserDetailsService - Load user details from database for Spring Security
 * 
 * Implements UserDetailsService interface to integrate with Spring Security
 * authentication framework. Loads user by email/username and provides authorities.
 * 
 * Features:
 * - Load user by email from database
 * - Map user roles to Spring Security authorities
 * - Handle not found scenarios
 * - Cache-friendly implementation
 * 
 * OOP Principle: Encapsulation - Database queries isolated
 * Design Pattern: Strategy pattern (implements UserDetailsService)
 * Best Practice: Lazy loading of user details
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username (email in our case)
     * 
     * Called by Spring Security during authentication
     * 
     * @param username Email of user to load
     * @return UserDetails object with user info and authorities
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for: {}", username);

        // Find user by email (email is used as username)
        com.bloodlink.entity.User user = userRepository.findByEmail(username)
            .orElseThrow(() -> {
                log.warn("User not found with email: {}", username);
                return new UsernameNotFoundException("User not found with email: " + username);
            });

        // Check if user account is active
        if (!user.isActive()) {
            log.warn("User account is inactive: {}", username);
            throw new UsernameNotFoundException("User account is inactive: " + username);
        }

        // Build UserDetails with authorities
        return User.builder()
            .username(user.getEmail())
            .password(user.getPassword())
            .authorities(getAuthorities(user.getUserRole()))
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .enabled(true)
            .build();
    }

    /**
     * Get authorities (roles) for user
     * 
     * Converts user role to Spring Security authorities
     * 
     * @param role User role (DONOR, PATIENT, ADMIN)
     * @return Collection of granted authorities
     */
    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        // Add ROLE_ prefix as required by Spring Security
        String authority = "ROLE_" + role.toUpperCase();
        return Collections.singletonList(new SimpleGrantedAuthority(authority));
    }

    /**
     * Load user by user ID (alternative method)
     * 
     * @param userId User ID
     * @return UserDetails object
     * @throws UsernameNotFoundException if user not found
     */
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        log.debug("Loading user details for ID: {}", userId);

        com.bloodlink.entity.User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.warn("User not found with ID: {}", userId);
                return new UsernameNotFoundException("User not found with ID: " + userId);
            });

        if (!user.isActive()) {
            log.warn("User account is inactive: {}", userId);
            throw new UsernameNotFoundException("User account is inactive");
        }

        return User.builder()
            .username(user.getEmail())
            .password(user.getPassword())
            .authorities(getAuthorities(user.getUserRole()))
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .enabled(true)
            .build();
    }
}
