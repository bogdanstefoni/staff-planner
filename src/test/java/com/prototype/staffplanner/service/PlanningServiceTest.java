package com.prototype.staffplanner.service;

import com.prototype.staffplanner.dto.PlanningRequest;
import com.prototype.staffplanner.enums.ShiftType;
import com.prototype.staffplanner.model.Employee;
import com.prototype.staffplanner.model.ScheduleEntry;
import com.prototype.staffplanner.model.WishBookEntry;
import com.prototype.staffplanner.repository.ScheduleEntryRepository;
import com.prototype.staffplanner.repository.WishBookEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanningServiceTest {

    @Mock
    private WishBookEntryRepository wishBookEntryRepository;

    @Mock
    private ScheduleEntryRepository scheduleEntryRepository;

    @InjectMocks
    private PlanningService planningService;

    private Employee employee1, employee2, employee3, employee4;
    private WishBookEntry wish1, wish2, wish3, wish4;
    private PlanningRequest validRequest;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2025, 6, 15);

        // Create employees
        employee1 = Employee.builder().id(1L).name("John Doe").build();
        employee2 = Employee.builder().id(2L).name("Jane Smith").build();
        employee3 = Employee.builder().id(3L).name("Bob Johnson").build();
        employee4 = Employee.builder().id(4L).name("Alice Wilson").build();

        // Create wish book entries - 2 for early, 2 for late shift
        wish1 = WishBookEntry.builder().id(1L).employee(employee1).date(testDate).shiftType(ShiftType.EARLY_SHIFT).build();
        wish2 = WishBookEntry.builder().id(2L).employee(employee2).date(testDate).shiftType(ShiftType.EARLY_SHIFT).build();
        wish3 = WishBookEntry.builder().id(3L).employee(employee3).date(testDate).shiftType(ShiftType.LATE_SHIFT).build();
        wish4 = WishBookEntry.builder().id(4L).employee(employee4).date(testDate).shiftType(ShiftType.LATE_SHIFT).build();

        validRequest = new PlanningRequest(testDate, Arrays.asList(1L, 2L, 3L, 4L));
    }

    @Test
    void createPlan_ValidRequest_Success() {
        // Arrange
        List<WishBookEntry> wishEntries = Arrays.asList(wish1, wish2, wish3, wish4);
        when(wishBookEntryRepository.findAllById(Arrays.asList(1L, 2L, 3L, 4L)))
                .thenReturn(wishEntries);

        List<ScheduleEntry> expectedScheduleEntries = Arrays.asList(
                ScheduleEntry.builder().employee(employee1).date(testDate).shiftType(ShiftType.EARLY_SHIFT).build(),
                ScheduleEntry.builder().employee(employee2).date(testDate).shiftType(ShiftType.EARLY_SHIFT).build(),
                ScheduleEntry.builder().employee(employee3).date(testDate).shiftType(ShiftType.LATE_SHIFT).build(),
                ScheduleEntry.builder().employee(employee4).date(testDate).shiftType(ShiftType.LATE_SHIFT).build()
        );

        when(scheduleEntryRepository.saveAll(any())).thenReturn(expectedScheduleEntries);

        // Act
        List<ScheduleEntry> result = planningService.createPlan(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.size());

        verify(scheduleEntryRepository).deleteByDate(testDate);
        verify(scheduleEntryRepository).saveAll(any());
    }

    @Test
    void createPlan_EmptyWishBookEntries_ThrowsException() {
        // Arrange
        when(wishBookEntryRepository.findAllById(any())).thenReturn(List.of());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> planningService.createPlan(validRequest)
        );

        assertEquals("No wish book entries found for the provided IDs.", exception.getMessage());
        verify(scheduleEntryRepository, never()).deleteByDate(any());
        verify(scheduleEntryRepository, never()).saveAll(any());
    }

    @Test
    void createPlan_WrongDate_ThrowsException() {
        // Arrange
        WishBookEntry wrongDateWish = WishBookEntry.builder()
                .id(1L)
                .employee(employee1)
                .date(LocalDate.of(2025, 6, 16))
                .shiftType(ShiftType.EARLY_SHIFT)
                .build();

        when(wishBookEntryRepository.findAllById(Arrays.asList(1L, 2L, 3L, 4L)))
                .thenReturn(Arrays.asList(wrongDateWish, wish2, wish3, wish4));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> planningService.createPlan(validRequest)
        );

        assertEquals("All wish book entries must have the same date.", exception.getMessage());
        verify(scheduleEntryRepository, never()).deleteByDate(any());
        verify(scheduleEntryRepository, never()).saveAll(any());
    }

    @Test
    void createPlan_TooFewEmployeesInShift_ThrowsException() {
        // Arrange - Only 1 employee for early shift
        List<WishBookEntry> wishEntries = Arrays.asList(wish1, wish3, wish4);
        when(wishBookEntryRepository.findAllById(any())).thenReturn(wishEntries);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> planningService.createPlan(validRequest)
        );

        assertTrue(exception.getMessage().contains("Exactly 2 employees are required for each shift type"));
        assertTrue(exception.getMessage().contains("EARLY_SHIFT"));
    }

    @Test
    void createPlan_TooManyEmployeesInShift_ThrowsException() {
        // Arrange - 3 employees for early shift
        Employee extraEmployee = Employee.builder().id(5L).name("Extra Person").build();
        WishBookEntry extraWish = WishBookEntry.builder()
                .id(5L)
                .employee(extraEmployee)
                .date(testDate)
                .shiftType(ShiftType.EARLY_SHIFT)
                .build();

        List<WishBookEntry> wishEntries = Arrays.asList(wish1, wish2, extraWish, wish3, wish4);
        when(wishBookEntryRepository.findAllById(any())).thenReturn(wishEntries);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> planningService.createPlan(validRequest)
        );

        assertTrue(exception.getMessage().contains("Exactly 2 employees are required for each shift type"));
        assertTrue(exception.getMessage().contains("EARLY_SHIFT"));
    }

    @Test
    void createPlan_EmployeeInBothShifts_ThrowsException() {
        // Arrange - employee1 in both early and late shift
        WishBookEntry duplicateWish = WishBookEntry.builder()
                .id(5L)
                .employee(employee1)
                .date(testDate)
                .shiftType(ShiftType.LATE_SHIFT)
                .build();

        List<WishBookEntry> wishEntries = Arrays.asList(wish1, wish2, duplicateWish, wish4);
        when(wishBookEntryRepository.findAllById(any())).thenReturn(wishEntries);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> planningService.createPlan(validRequest)
        );

        assertEquals("An employee cannot be assigned to more than one shift type.",
                exception.getMessage());
    }

    @Test
    void createPlan_MissingShiftType_ThrowsException() {
        // Arrange - No late shift employees
        List<WishBookEntry> wishEntries = Arrays.asList(wish1, wish2);
        when(wishBookEntryRepository.findAllById(any())).thenReturn(wishEntries);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> planningService.createPlan(validRequest)
        );

        assertTrue(exception.getMessage().contains("Exactly 2 employees are required for each shift type"));
        assertTrue(exception.getMessage().contains("LATE_SHIFT"));
    }
}