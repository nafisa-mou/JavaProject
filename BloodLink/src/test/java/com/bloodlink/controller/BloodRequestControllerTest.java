package com.bloodlink.controller;

import com.bloodlink.dto.BloodRequestDto;
import com.bloodlink.service.BloodRequestService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BloodRequestController
 * Tests blood request endpoints: create, accept, decline, complete, search, etc.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DisplayName("BloodRequestController Integration Tests")
class BloodRequestControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private BloodRequestService bloodRequestService;
    
    private BloodRequestDto bloodRequestDto;
    private List<BloodRequestDto> requestList;
    
    @BeforeEach
    void setUp() {
        bloodRequestDto = new BloodRequestDto();
        bloodRequestDto.setId(1L);
        bloodRequestDto.setPatientId(7L);
        bloodRequestDto.setBloodGroup("O+");
        bloodRequestDto.setUnitsNeeded(2);
        bloodRequestDto.setEmergencyLevel("CRITICAL");
        bloodRequestDto.setStatus("PENDING");
        
        requestList = new ArrayList<>();
        requestList.add(bloodRequestDto);
    }
    
    @Test
    @DisplayName("Should get all pending blood requests")
    void testGetPendingRequests() throws Exception {
        when(bloodRequestService.getAllPendingRequests()).thenReturn(requestList);
        
        mockMvc.perform(get("/api/requests")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].status", is("PENDING")))
            .andExpect(jsonPath("$.data[0].bloodGroup", is("O+")));
    }
    
    @Test
    @DisplayName("Should get blood request by ID")
    void testGetRequestById() throws Exception {
        when(bloodRequestService.getRequestById(1L)).thenReturn(bloodRequestDto);
        
        mockMvc.perform(get("/api/requests/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.id", is(1)))
            .andExpect(jsonPath("$.data.status", is("PENDING")));
    }
    
    @Test
    @DisplayName("Should get critical blood requests")
    void testGetCriticalRequests() throws Exception {
        when(bloodRequestService.getCriticalRequests()).thenReturn(requestList);
        
        mockMvc.perform(get("/api/requests/critical")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].emergencyLevel", is("CRITICAL")));
    }
    
    @Test
    @DisplayName("Should get expired blood requests")
    void testGetExpiredRequests() throws Exception {
        when(bloodRequestService.getExpiredRequests()).thenReturn(new ArrayList<>());
        
        mockMvc.perform(get("/api/requests/expired")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data", hasSize(0)));
    }
    
    @Test
    @DisplayName("Should get statistics for blood requests")
    void testGetStatistics() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPending", 5);
        stats.put("totalAccepted", 3);
        stats.put("totalCompleted", 20);
        stats.put("totalDeclined", 2);
        stats.put("totalExpired", 1);
        stats.put("totalCritical", 2);
        
        when(bloodRequestService.getStatistics()).thenReturn(stats);
        
        mockMvc.perform(get("/api/requests/statistics")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.totalPending", is(5)))
            .andExpect(jsonPath("$.data.totalCritical", is(2)));
    }
    
    @Test
    @WithMockUser(roles = "DONOR")
    @DisplayName("Should accept blood request by donor")
    void testAcceptRequest() throws Exception {
        BloodRequestDto acceptedRequest = bloodRequestDto;
        acceptedRequest.setStatus("ACCEPTED");
        acceptedRequest.setDonorId(1L);
        
        when(bloodRequestService.acceptRequest(1L, 1L)).thenReturn(acceptedRequest);
        
        mockMvc.perform(post("/api/requests/1/accept")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.status", is("ACCEPTED")));
    }
    
    @Test
    @WithMockUser(roles = "DONOR")
    @DisplayName("Should decline blood request by donor")
    void testDeclineRequest() throws Exception {
        BloodRequestDto declinedRequest = bloodRequestDto;
        declinedRequest.setStatus("DECLINED");
        
        Map<String, String> declineReason = new HashMap<>();
        declineReason.put("reason", "Not available on that date");
        
        when(bloodRequestService.declineRequest(1L, "Not available on that date"))
            .thenReturn(declinedRequest);
        
        mockMvc.perform(post("/api/requests/1/decline")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(declineReason)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.status", is("DECLINED")));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should complete blood request as admin")
    void testCompleteRequest() throws Exception {
        BloodRequestDto completedRequest = bloodRequestDto;
        completedRequest.setStatus("COMPLETED");
        completedRequest.setUnitsReceived(2);
        
        when(bloodRequestService.completeRequest(1L)).thenReturn(completedRequest);
        
        mockMvc.perform(post("/api/requests/1/complete")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.status", is("COMPLETED")));
    }
    
    @Test
    @DisplayName("Should get suitable donors for blood request")
    void testFindSuitableDonors() throws Exception {
        List<Map<String, Object>> donorList = new ArrayList<>();
        Map<String, Object> donor = new HashMap<>();
        donor.put("id", 1L);
        donor.put("name", "John Doe");
        donor.put("distance", 2.5);
        donorList.add(donor);
        
        when(bloodRequestService.findSuitableDonors(1L)).thenReturn(donorList);
        
        mockMvc.perform(get("/api/requests/1/donors")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data", hasSize(1)));
    }
    
    @Test
    @DisplayName("Should calculate priority score for request")
    void testGetPriorityScore() throws Exception {
        when(bloodRequestService.calculatePriorityScore(1L)).thenReturn(85);
        
        mockMvc.perform(get("/api/requests/1/priority")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data", is(85)));
    }
}
