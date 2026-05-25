package com.bloodlink.controller;

import com.bloodlink.dto.ChatDto;
import com.bloodlink.service.ChatService;
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
 * Integration tests for ChatController
 * Tests chat endpoints: get chats, start chat, archive, block, etc.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DisplayName("ChatController Integration Tests")
@WithMockUser(roles = "DONOR")
class ChatControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ChatService chatService;
    
    private ChatDto chatDto;
    private List<ChatDto> chatList;
    
    @BeforeEach
    void setUp() {
        chatDto = new ChatDto();
        chatDto.setId(1L);
        chatDto.setUserOneId(1L);
        chatDto.setUserTwoId(7L);
        chatDto.setStatus("ACTIVE");
        chatDto.setTotalMessages(5);
        chatDto.setUnreadMessages(1);
        
        chatList = new ArrayList<>();
        chatList.add(chatDto);
    }
    
    @Test
    @DisplayName("Should get all chats for authenticated user")
    void testGetAllChats() throws Exception {
        when(chatService.getUserChats(anyLong())).thenReturn(chatList);
        
        mockMvc.perform(get("/api/chats")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.count", is(1)))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].status", is("ACTIVE")));
    }
    
    @Test
    @DisplayName("Should get chat by ID")
    void testGetChatById() throws Exception {
        when(chatService.getChatById(1L)).thenReturn(chatDto);
        
        mockMvc.perform(get("/api/chats/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.id", is(1)))
            .andExpect(jsonPath("$.data.totalMessages", is(5)));
    }
    
    @Test
    @DisplayName("Should get chat between two users")
    void testGetChatBetweenUsers() throws Exception {
        when(chatService.getChatBetween(1L, 7L)).thenReturn(chatDto);
        
        mockMvc.perform(get("/api/chats/between?user1=1&user2=7")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.id", is(1)));
    }
    
    @Test
    @DisplayName("Should get chat statistics")
    void testGetStatistics() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalChats", 5);
        stats.put("activeChats", 4);
        stats.put("archivedChats", 1);
        stats.put("blockedChats", 0);
        stats.put("unreadChats", 2);
        stats.put("totalMessages", 50);
        
        when(chatService.getUserChatStatistics(anyLong())).thenReturn(stats);
        
        mockMvc.perform(get("/api/chats/statistics")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.totalChats", is(5)))
            .andExpect(jsonPath("$.data.unreadChats", is(2)));
    }
    
    @Test
    @DisplayName("Should start new chat between users")
    void testStartChat() throws Exception {
        Map<String, Long> startChatRequest = new HashMap<>();
        startChatRequest.put("userId1", 1L);
        startChatRequest.put("userId2", 7L);
        
        when(chatService.startOrReactivateChat(1L, 7L)).thenReturn(chatDto);
        
        mockMvc.perform(post("/api/chats/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(startChatRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.status", is("ACTIVE")));
    }
    
    @Test
    @DisplayName("Should archive chat")
    void testArchiveChat() throws Exception {
        ChatDto archivedChat = chatDto;
        archivedChat.setStatus("ARCHIVED");
        
        when(chatService.archiveChat(1L)).thenReturn(archivedChat);
        
        mockMvc.perform(put("/api/chats/1/archive")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.status", is("ARCHIVED")));
    }
    
    @Test
    @DisplayName("Should block chat")
    void testBlockChat() throws Exception {
        ChatDto blockedChat = chatDto;
        blockedChat.setStatus("BLOCKED");
        
        when(chatService.blockChat(1L)).thenReturn(blockedChat);
        
        mockMvc.perform(put("/api/chats/1/block")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.status", is("BLOCKED")));
    }
    
    @Test
    @DisplayName("Should check if users can chat")
    void testCanUsersChat() throws Exception {
        Map<String, Boolean> canChat = new HashMap<>();
        canChat.put("canChat", true);
        
        when(chatService.canUsersChat(1L, 7L)).thenReturn(true);
        
        mockMvc.perform(get("/api/chats/can-chat?user1=1&user2=7")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.canChat", is(true)));
    }
}
