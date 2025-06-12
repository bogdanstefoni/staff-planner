package com.prototype.staffplanner.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prototype.staffplanner.dto.PlanningRequest;
import com.prototype.staffplanner.dto.WishBookRequest;
import com.prototype.staffplanner.enums.ShiftType;
import com.prototype.staffplanner.model.WishBookEntry;
import com.prototype.staffplanner.repository.EmployeeRepository;
import com.prototype.staffplanner.repository.WishBookEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class StaffPlannerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WishBookEntryRepository wishBookEntryRepository;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        wishBookEntryRepository.deleteAll();
        employeeRepository.deleteAll();
    }

    @Test
    void fullWorkflow_CreateWishesCreatePlanViewSchedule_Success() throws Exception {
        LocalDate testDate = LocalDate.of(2025, 6, 15);

        // Step 1: Create wish book entries
        WishBookRequest wish1 = new WishBookRequest("John Doe", testDate, ShiftType.EARLY_SHIFT);
        WishBookRequest wish2 = new WishBookRequest("Jane Smith", testDate, ShiftType.EARLY_SHIFT);
        WishBookRequest wish3 = new WishBookRequest("Bob Johnson", testDate, ShiftType.LATE_SHIFT);
        WishBookRequest wish4 = new WishBookRequest("Alice Wilson", testDate, ShiftType.LATE_SHIFT);

        // Add all wish book entries
        mockMvc.perform(post("/api/wishbook/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wish1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/wishbook/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wish2)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/wishbook/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wish3)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/wishbook/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wish4)))
                .andExpect(status().isOk());

        // Step 2: Verify wish book entries were created
        List<WishBookEntry> wishEntries = wishBookEntryRepository.findByDate(testDate);
        assert wishEntries.size() == 4;

        // Step 3: Create planning request with wish book entry IDs
        List<Long> wishBookIds = wishEntries.stream().map(WishBookEntry::getId).toList();
        PlanningRequest planningRequest = new PlanningRequest(testDate, wishBookIds);

        // Step 4: Create plan
        mockMvc.perform(post("/api/planning/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(planningRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));

        // Step 5: View schedule
        mockMvc.perform(get("/api/schedule/" + testDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(testDate.toString()))
                .andExpect(jsonPath("$.shifts").isArray())
                .andExpect(jsonPath("$.shifts.length()").value(2))
                .andExpect(jsonPath("$.shifts[?(@.shiftType=='EARLY_SHIFT')].employeeNames.length()").value(2))
                .andExpect(jsonPath("$.shifts[?(@.shiftType=='LATE_SHIFT')].employeeNames.length()").value(2));
    }

    @Test
    void createPlan_InsufficientEmployees_ReturnsBadRequest() throws Exception {
        LocalDate testDate = LocalDate.of(2025, 6, 16);

        // Create only 3 wish book entries (insufficient for proper planning)
        WishBookRequest wish1 = new WishBookRequest("John Doe", testDate, ShiftType.EARLY_SHIFT);
        WishBookRequest wish2 = new WishBookRequest("Jane Smith", testDate, ShiftType.EARLY_SHIFT);
        WishBookRequest wish3 = new WishBookRequest("Bob Johnson", testDate, ShiftType.LATE_SHIFT);

        // Add wish book entries
        mockMvc.perform(post("/api/wishbook/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wish1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/wishbook/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wish2)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/wishbook/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wish3)))
                .andExpect(status().isOk());

        // Get wish book entry IDs
        List<WishBookEntry> wishEntries = wishBookEntryRepository.findByDate(testDate);
        List<Long> wishBookIds = wishEntries.stream().map(WishBookEntry::getId).toList();
        PlanningRequest planningRequest = new PlanningRequest(testDate, wishBookIds);

        // Attempt to create plan - should fail with bad request due to business logic
        mockMvc.perform(post("/api/planning/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(planningRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWishBookEntry_DuplicateEntry_ReturnsConflict() throws Exception {
        LocalDate testDate = LocalDate.of(2025, 6, 17);
        WishBookRequest wish = new WishBookRequest("John Doe", testDate, ShiftType.EARLY_SHIFT);

        // First request should succeed
        mockMvc.perform(post("/api/wishbook/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wish)))
                .andExpect(status().isOk());

        // Second identical request should fail due to unique constraint
        mockMvc.perform(post("/api/wishbook/entry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wish)))
                .andExpect(status().isConflict());
    }

    @Test
    void getSchedule_NonExistentDate_ReturnsEmptySchedule() throws Exception {
        LocalDate nonExistentDate = LocalDate.of(2025, 6, 20);

        mockMvc.perform(get("/api/schedule/" + nonExistentDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(nonExistentDate.toString()))
                .andExpect(jsonPath("$.shifts").isArray())
                .andExpect(jsonPath("$.shifts.length()").value(2))
                .andExpect(jsonPath("$.shifts[0].employeeNames").isEmpty())
                .andExpect(jsonPath("$.shifts[1].employeeNames").isEmpty());
    }
}