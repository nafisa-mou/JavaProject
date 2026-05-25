package com.bloodlink.controller;

import com.bloodlink.dto.DonorDto;
import com.bloodlink.dto.AvailabilityRequest;
import com.bloodlink.entity.Donor;
import com.bloodlink.service.DonorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for DonorController
 * Tests donor endpoints: profile, availability, donations, search, etc.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DisplayName("DonorController Integration Tests")
class DonorControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private DonorService donorService;
    
    private List<DonorDto> donorList;
    private DonorDto donorDto;
    
    @BeforeEach
    void setUp() {
        donorDto = new DonorDto();
        donorDto.setId(1L);
        donorDto.setFullName("John Doe");
        donorDto.setEmail("john.donor@bloodlink.com");
        donorDto.setPhoneNumber("+1-555-0101");
        donorDto.setBloodGroup("O+");
        donorDto.setCity("New York");
        donorDto.setState("NY");
        donorDto.setLatitude(40.7128);
        donorDto.setLongitude(-74.0060);
        donorDto.setAvailable(true);
        donorDto.setTotalDonations(5);
        
        donorList = new ArrayList<>();
        donorList.add(donorDto);
    }
    
    @Test
    @DisplayName("Should get all donors without authentication")
    void testGetAllDonors() throws Exception {
        when(donorService.getAllDonors()).thenReturn(donorList);
        
        mockMvc.perform(get("/api/donors")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.count", is(1)))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].fullName", is("John Doe")))
            .andExpect(jsonPath("$.data[0].bloodGroup", is("O+")));
        
        verify(donorService, times(1)).getAllDonors();
    }
    
    @Test
    @DisplayName("Should get donor by ID")
    void testGetDonorById() throws Exception {
        when(donorService.getDonorById(1L)).thenReturn(donorDto);
        
        mockMvc.perform(get("/api/donors/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.id", is(1)))
            .andExpect(jsonPath("$.data.fullName", is("John Doe")));
        
        verify(donorService, times(1)).getDonorById(1L);
    }
    
    @Test
    @DisplayName("Should return 404 when donor not found")
    void testGetDonorNotFound() throws Exception {
        when(donorService.getDonorById(999L))
            .thenThrow(new com.bloodlink.exception.ResourceNotFoundException("Donor not found"));
        
        mockMvc.perform(get("/api/donors/999")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Should search donors by blood group")
    void testSearchByBloodGroup() throws Exception {
        when(donorService.searchByBloodGroup("O+")).thenReturn(donorList);
        
        mockMvc.perform(get("/api/donors/search?bg=O+")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].bloodGroup", is("O+")));
    }
    
    @Test
    @DisplayName("Should find nearby donors within radius")
    void testFindNearbyDonors() throws Exception {
        when(donorService.findNearbyDonors(40.7128, -74.0060, 50)).thenReturn(donorList);
        
        mockMvc.perform(get("/api/donors/nearby?lat=40.7128&lon=-74.0060&radius=50")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].city", is("New York")));
    }
    
    @Test
    @WithMockUser(roles = "DONOR")
    @DisplayName("Should update donor profile with DONOR role")
    void testUpdateDonorProfile() throws Exception {
        DonorDto updateDto = new DonorDto();
        updateDto.setPhoneNumber("+1-555-0102");
        updateDto.setCity("Boston");
        
        when(donorService.updateDonorProfile(1L, updateDto)).thenReturn(donorDto);
        
        mockMvc.perform(put("/api/donors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }
    
    @Test
    @DisplayName("Should return 401 when updating without authentication")
    void testUpdateDonorUnauthorized() throws Exception {
        DonorDto updateDto = new DonorDto();
        updateDto.setPhoneNumber("+1-555-0102");
        
        mockMvc.perform(put("/api/donors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "DONOR")
    @DisplayName("Should update donor availability")
    void testUpdateAvailability() throws Exception {
        AvailabilityRequest request = new AvailabilityRequest();
        request.setAvailable(false);
        
        when(donorService.updateAvailability(1L, false)).thenReturn(donorDto);
        
        mockMvc.perform(put("/api/donors/1/availability")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }
    
    @Test
    @DisplayName("Should get donor score")
    void testGetDonorScore() throws Exception {
        when(donorService.getDonorScore(1L)).thenReturn(85);
        
        mockMvc.perform(get("/api/donors/1/score")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data", is(85)));
    }
    
    @Test
    @DisplayName("Should check donor eligibility")
    void testCheckEligibility() throws Exception {
        when(donorService.isDonorEligibleForDonation(1L)).thenReturn(true);
        
        mockMvc.perform(get("/api/donors/1/eligible")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data", is(true)));
    }
}
