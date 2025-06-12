package com.prototype.staffplanner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prototype.staffplanner.dto.PlanningRequest;
import com.prototype.staffplanner.enums.ShiftType;
import com.prototype.staffplanner.model.Employee;
import com.prototype.staffplanner.model.ScheduleEntry;
import com.prototype.staffplanner.service.PlanningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanningController.class)
class PlanningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlanningService planningService;

    @Autowired
    private ObjectMapper objectMapper;

    private PlanningRequest validRequest;
    private List<ScheduleEntry> expectedSchedule;

    @BeforeEach
    void setUp() {
        validRequest = new PlanningRequest(LocalDate.of(2025, 6, 15), Arrays.asList(1L, 2L, 3L, 4L));

        Employee emp1 = Employee.builder().id(1L).name("John Doe").build();
        Employee emp2 = Employee.builder().id(2L).name("Jane Smith").build();
        Employee emp3 = Employee.builder().id(3L).name("Bob Johnson").build();
        Employee emp4 = Employee.builder().id(4L).name("Alice Wilson").build();

        expectedSchedule = Arrays.asList(
                ScheduleEntry.builder().id(1L).employee(emp1).date(LocalDate.of(2025, 6, 15)).shiftType(ShiftType.EARLY_SHIFT).build(),
                ScheduleEntry.builder().id(2L).employee(emp2).date(LocalDate.of(2025, 6, 15)).shiftType(ShiftType.EARLY_SHIFT).build(),
                ScheduleEntry.builder().id(3L).employee(emp3).date(LocalDate.of(2025, 6, 15)).shiftType(ShiftType.LATE_SHIFT).build(),
                ScheduleEntry.builder().id(4L).employee(emp4).date(LocalDate.of(2025, 6, 15)).shiftType(ShiftType.LATE_SHIFT).build()
        );
    }

    @Test
    void createPlan_ValidRequest_ReturnsOk() throws Exception {
        // Arrange
        when(planningService.createPlan(any(PlanningRequest.class))).thenReturn(expectedSchedule);

        // Act & Assert
        mockMvc.perform(post("/api/planning/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].employee.name").value("John Doe"))
                .andExpect(jsonPath("$[0].shiftType").value("EARLY_SHIFT"));
    }

    @Test
    void createPlan_NullDate_ReturnsBadRequest() throws Exception {
        // Arrange - null date
        PlanningRequest invalidRequest = new PlanningRequest(null, Arrays.asList(1L, 2L, 3L, 4L));

        // Act & Assert
        mockMvc.perform(post("/api/planning/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPlan_EmptyWishBookEntryIds_ReturnsBadRequest() throws Exception {
        // Arrange - empty wish book entry IDs
        PlanningRequest invalidRequest = new PlanningRequest(LocalDate.of(2025, 6, 15), List.of());

        // Act & Assert
        mockMvc.perform(post("/api/planning/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPlan_NullWishBookEntryIds_ReturnsBadRequest() throws Exception {
        // Arrange - null wish book entry IDs
        PlanningRequest invalidRequest = new PlanningRequest(LocalDate.of(2025, 6, 15), null);

        // Act & Assert
        mockMvc.perform(post("/api/planning/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPlan_BusinessLogicException_ReturnsInternalServerError() throws Exception {
        // Arrange
        when(planningService.createPlan(any(PlanningRequest.class)))
                .thenThrow(new IllegalArgumentException("Exactly 2 employees are required for each shift type"));

        // Act & Assert
        mockMvc.perform(post("/api/planning/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPlan_UnexpectedException_ReturnsInternalServerError() throws Exception {
        // Arrange
        when(planningService.createPlan(any(PlanningRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(post("/api/planning/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError());
    }
}