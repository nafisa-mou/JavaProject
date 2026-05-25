package com.bloodlink.controller;

import com.bloodlink.dto.AuthRequest;
import com.bloodlink.dto.AuthResponse;
import com.bloodlink.entity.User;
import com.bloodlink.entity.Donor;
import com.bloodlink.exception.UserNotFoundException;
import com.bloodlink.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController
 * Tests authentication endpoints: login, register, token refresh, password management
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private AuthService authService;
    
    private AuthRequest authRequest;
    private AuthResponse authResponse;
    private Donor donor;
    
    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest();
        authRequest.setEmail("john.donor@bloodlink.com");
        authRequest.setPassword("SecurePass123!");
        
        donor = new Donor();
        donor.setId(1L);
        donor.setEmail("john.donor@bloodlink.com");
        donor.setFullName("John Doe");
        donor.setBloodGroup("O+");
        donor.setActive(true);
        
        authResponse = new AuthResponse();
        authResponse.setSuccess(true);
        authResponse.setToken("eyJhbGciOiJIUzUxMiJ9...");
        authResponse.setRefreshToken("eyJhbGciOiJIUzUxMiJ9...");
        authResponse.setUserId(1L);
        authResponse.setEmail("john.donor@bloodlink.com");
        authResponse.setUserRole("DONOR");
        authResponse.setExpiresIn(86400000L);
    }
    
    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() throws Exception {
        when(authService.login("john.donor@bloodlink.com", "SecurePass123!"))
            .thenReturn(authResponse);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.token", notNullValue()))
            .andExpect(jsonPath("$.refreshToken", notNullValue()))
            .andExpect(jsonPath("$.userId", is(1)))
            .andExpect(jsonPath("$.userRole", is("DONOR")));
        
        verify(authService, times(1)).login(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Should return 400 for invalid email format")
    void testLoginWithInvalidEmail() throws Exception {
        authRequest.setEmail("invalid-email");
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should return 404 for non-existent user")
    void testLoginUserNotFound() throws Exception {
        when(authService.login(anyString(), anyString()))
            .thenThrow(new UserNotFoundException("User not found"));
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success", is(false)))
            .andExpect(jsonPath("$.error", notNullValue()));
    }
    
    @Test
    @DisplayName("Should return 400 for missing email")
    void testLoginMissingEmail() throws Exception {
        authRequest.setEmail(null);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should return 400 for missing password")
    void testLoginMissingPassword() throws Exception {
        authRequest.setPassword(null);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should register donor successfully")
    void testRegisterDonorSuccess() throws Exception {
        AuthRequest registerRequest = new AuthRequest();
        registerRequest.setEmail("newdonor@bloodlink.com");
        registerRequest.setPassword("SecurePass123!");
        registerRequest.setFullName("Jane Doe");
        registerRequest.setPhoneNumber("+1-555-0001");
        registerRequest.setGender("FEMALE");
        registerRequest.setAge(28);
        registerRequest.setCity("New York");
        registerRequest.setState("NY");
        registerRequest.setLatitude(40.7128);
        registerRequest.setLongitude(-74.0060);
        registerRequest.setBloodGroup("A+");
        
        AuthResponse registerResponse = new AuthResponse();
        registerResponse.setSuccess(true);
        registerResponse.setToken("eyJhbGciOiJIUzUxMiJ9...");
        registerResponse.setUserId(2L);
        registerResponse.setUserRole("DONOR");
        
        when(authService.registerDonor(any())).thenReturn(registerResponse);
        
        mockMvc.perform(post("/api/auth/register-donor")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.userId", is(2)))
            .andExpect(jsonPath("$.userRole", is("DONOR")));
        
        verify(authService, times(1)).registerDonor(any());
    }
    
    @Test
    @DisplayName("Should refresh token successfully")
    void testRefreshTokenSuccess() throws Exception {
        Map<String, String> refreshRequest = new HashMap<>();
        refreshRequest.put("refreshToken", "eyJhbGciOiJIUzUxMiJ9...");
        
        AuthResponse refreshResponse = new AuthResponse();
        refreshResponse.setSuccess(true);
        refreshResponse.setToken("eyJhbGciOiJIUzUxMiJ9.new...");
        refreshResponse.setRefreshToken("eyJhbGciOiJIUzUxMiJ9.new...");
        
        when(authService.refreshToken(anyString())).thenReturn(refreshResponse);
        
        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.token", notNullValue()));
    }
    
    @Test
    @DisplayName("Should return 400 for invalid refresh token")
    void testRefreshTokenInvalid() throws Exception {
        Map<String, String> refreshRequest = new HashMap<>();
        refreshRequest.put("refreshToken", "invalid-token");
        
        when(authService.refreshToken(anyString()))
            .thenThrow(new IllegalArgumentException("Invalid refresh token"));
        
        mockMvc.perform(post("/api/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
            .andExpect(status().isBadRequest());
    }
}

import java.util.HashMap;
import java.util.Map;
