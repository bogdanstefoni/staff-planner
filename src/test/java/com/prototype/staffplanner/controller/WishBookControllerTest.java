package com.prototype.staffplanner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prototype.staffplanner.dto.WishBookEntryResponse;
import com.prototype.staffplanner.dto.WishBookRequest;
import com.prototype.staffplanner.enums.ShiftType;
import com.prototype.staffplanner.service.WishBookEntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishBookController.class)
class WishBookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WishBookEntryService wishBookEntryService;

    @Autowired
    private ObjectMapper objectMapper;

    private WishBookRequest validRequest;
    private WishBookEntryResponse expectedResponse;

    @BeforeEach
    void setUp() {
        validRequest = new WishBookRequest("John Doe", LocalDate.of(2025, 6, 15), ShiftType.EARLY_SHIFT);
        expectedResponse = new WishBookEntryResponse("John Doe", LocalDate.of(2025, 6, 15), ShiftType.EARLY_SHIFT);
    }

    @Test
    void addWishBookEntry_ValidRequest_ReturnsOk() throws Exception {
        // Arrange
        when(wishBookEntryService.addWishBookEntry(any(WishBookRequest.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/wishbook/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeName").value("John Doe"))
                .andExpect(jsonPath("$.date").value("2025-06-15"))
                .andExpect(jsonPath("$.shiftType").value("EARLY_SHIFT"));
    }

    @Test
    void addWishBookEntry_NullEmployeeName_ReturnsBadRequest() throws Exception {
        // Arrange - null employee name
        WishBookRequest invalidRequest = new WishBookRequest(null, LocalDate.of(2025, 6, 15), ShiftType.EARLY_SHIFT);

        // Act & Assert
        mockMvc.perform(post("/api/wishbook/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addWishBookEntry_NullDate_ReturnsBadRequest() throws Exception {
        // Arrange - null date
        WishBookRequest invalidRequest = new WishBookRequest("John Doe", null, ShiftType.EARLY_SHIFT);

        // Act & Assert
        mockMvc.perform(post("/api/wishbook/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addWishBookEntry_NullShiftType_ReturnsBadRequest() throws Exception {
        // Arrange - null shift type
        WishBookRequest invalidRequest = new WishBookRequest("John Doe", LocalDate.of(2025, 6, 15), null);

        // Act & Assert
        mockMvc.perform(post("/api/wishbook/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addWishBookEntry_LateShift_ReturnsCorrectData() throws Exception {
        // Arrange
        WishBookRequest lateShiftRequest = new WishBookRequest("Jane Smith", LocalDate.of(2025, 6, 15), ShiftType.LATE_SHIFT);
        WishBookEntryResponse lateShiftResponse = new WishBookEntryResponse("Jane Smith", LocalDate.of(2025, 6, 15), ShiftType.LATE_SHIFT);

        when(wishBookEntryService.addWishBookEntry(any(WishBookRequest.class))).thenReturn(lateShiftResponse);

        // Act & Assert
        mockMvc.perform(post("/api/wishbook/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lateShiftRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeName").value("Jane Smith"))
                .andExpect(jsonPath("$.shiftType").value("LATE_SHIFT"));
    }

    @Test
    void addWishBookEntry_ServiceException_ReturnsInternalServerError() throws Exception {
        // Arrange
        when(wishBookEntryService.addWishBookEntry(any(WishBookRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/api/wishbook/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError());
    }
}