package com.prototype.staffplanner.controller;

import com.prototype.staffplanner.dto.ScheduleResponse;
import com.prototype.staffplanner.enums.ShiftType;
import com.prototype.staffplanner.service.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@WebMvcTest(ScheduleController.class)
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScheduleService scheduleService;

    private ScheduleResponse expectedResponse;

    @BeforeEach
    void setUp() {
        LocalDate testDate = LocalDate.of(2025, 6, 15);

        ScheduleResponse.ShiftInfo earlyShift = new ScheduleResponse.ShiftInfo(
                ShiftType.EARLY_SHIFT, Arrays.asList("John Doe", "Jane Smith"));
        ScheduleResponse.ShiftInfo lateShift = new ScheduleResponse.ShiftInfo(
                ShiftType.LATE_SHIFT, Arrays.asList("Bob Johnson", "Alice Wilson"));

        expectedResponse = new ScheduleResponse(testDate, Arrays.asList(earlyShift, lateShift));
    }

    @Test
    void getSchedule_ValidDate_ReturnsSchedule() throws Exception {
        // Arrange
        when(scheduleService.getScheduleForDate(any(LocalDate.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/schedule/2025-06-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2025-06-15"))
                .andExpect(jsonPath("$.shifts").isArray())
                .andExpect(jsonPath("$.shifts.length()").value(2))
                .andExpect(jsonPath("$.shifts[0].shiftType").value("EARLY_SHIFT"))
                .andExpect(jsonPath("$.shifts[0].timeRange").value("07:00 - 15:30"))
                .andExpect(jsonPath("$.shifts[0].employeeNames").isArray())
                .andExpect(jsonPath("$.shifts[0].employeeNames.length()").value(2))
                .andExpect(jsonPath("$.shifts[0].employeeNames[0]").value("John Doe"))
                .andExpect(jsonPath("$.shifts[0].employeeNames[1]").value("Jane Smith"));
    }

    @Test
    void getSchedule_EmptySchedule_ReturnsEmptyShifts() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2025, 6, 16);
        ScheduleResponse.ShiftInfo emptyEarlyShift = new ScheduleResponse.ShiftInfo(ShiftType.EARLY_SHIFT, List.of());
        ScheduleResponse.ShiftInfo emptyLateShift = new ScheduleResponse.ShiftInfo(ShiftType.LATE_SHIFT, List.of());
        ScheduleResponse emptyResponse = new ScheduleResponse(testDate, Arrays.asList(emptyEarlyShift, emptyLateShift));

        when(scheduleService.getScheduleForDate(any(LocalDate.class))).thenReturn(emptyResponse);

        // Act & Assert
        mockMvc.perform(get("/api/schedule/2025-06-16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2025-06-16"))
                .andExpect(jsonPath("$.shifts").isArray())
                .andExpect(jsonPath("$.shifts.length()").value(2))
                .andExpect(jsonPath("$.shifts[0].employeeNames").isEmpty())
                .andExpect(jsonPath("$.shifts[1].employeeNames").isEmpty());
    }

    @Test
    void getSchedule_InvalidDateFormat_ReturnsInternalServerError() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/schedule/invalid-date"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getSchedule_FutureDate_ReturnsSchedule() throws Exception {
        // Arrange
        LocalDate futureDate = LocalDate.of(2025, 12, 25);
        ScheduleResponse.ShiftInfo earlyShift = new ScheduleResponse.ShiftInfo(ShiftType.EARLY_SHIFT, List.of("Employee1"));
        ScheduleResponse.ShiftInfo lateShift = new ScheduleResponse.ShiftInfo(ShiftType.LATE_SHIFT, List.of("Employee2"));
        ScheduleResponse futureResponse = new ScheduleResponse(futureDate, Arrays.asList(earlyShift, lateShift));

        when(scheduleService.getScheduleForDate(any(LocalDate.class))).thenReturn(futureResponse);

        // Act & Assert
        mockMvc.perform(get("/api/schedule/2025-12-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2025-12-25"));
    }
}