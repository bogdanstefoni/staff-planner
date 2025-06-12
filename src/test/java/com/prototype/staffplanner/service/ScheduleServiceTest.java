package com.prototype.staffplanner.service;

import com.prototype.staffplanner.dto.ScheduleResponse;
import com.prototype.staffplanner.enums.ShiftType;
import com.prototype.staffplanner.model.Employee;
import com.prototype.staffplanner.model.ScheduleEntry;
import com.prototype.staffplanner.repository.ScheduleEntryRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleEntryRepository scheduleEntryRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private LocalDate testDate;
    private Employee employee1, employee2, employee3, employee4;
    private ScheduleEntry entry1, entry2, entry3, entry4;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2025, 6, 15);

        employee1 = Employee.builder().id(1L).name("John Doe").build();
        employee2 = Employee.builder().id(2L).name("Jane Smith").build();
        employee3 = Employee.builder().id(3L).name("Bob Johnson").build();
        employee4 = Employee.builder().id(4L).name("Alice Wilson").build();

        entry1 = ScheduleEntry.builder().id(1L).employee(employee1).date(testDate).shiftType(ShiftType.EARLY_SHIFT).build();
        entry2 = ScheduleEntry.builder().id(2L).employee(employee2).date(testDate).shiftType(ShiftType.EARLY_SHIFT).build();
        entry3 = ScheduleEntry.builder().id(3L).employee(employee3).date(testDate).shiftType(ShiftType.LATE_SHIFT).build();
        entry4 = ScheduleEntry.builder().id(4L).employee(employee4).date(testDate).shiftType(ShiftType.LATE_SHIFT).build();
    }

    @Test
    void getScheduleForDate_FullSchedule_ReturnsCompleteSchedule() {
        // Arrange
        List<ScheduleEntry> entries = Arrays.asList(entry1, entry2, entry3, entry4);
        when(scheduleEntryRepository.findByDate(testDate)).thenReturn(entries);

        // Act
        ScheduleResponse result = scheduleService.getScheduleForDate(testDate);

        // Assert
        assertNotNull(result);
        assertEquals(testDate, result.getDate());
        assertEquals(2, result.getShifts().size());

        // Check early shift
        ScheduleResponse.ShiftInfo earlyShift = result.getShifts().stream()
                .filter(shift -> shift.getShiftType() == ShiftType.EARLY_SHIFT)
                .findFirst()
                .orElseThrow();
        assertEquals(2, earlyShift.getEmployeeNames().size());
        assertTrue(earlyShift.getEmployeeNames().contains("John Doe"));
        assertTrue(earlyShift.getEmployeeNames().contains("Jane Smith"));
        assertEquals("07:00 - 15:30", earlyShift.getTimeRange());

        // Check late shift
        ScheduleResponse.ShiftInfo lateShift = result.getShifts().stream()
                .filter(shift -> shift.getShiftType() == ShiftType.LATE_SHIFT)
                .findFirst()
                .orElseThrow();
        assertEquals(2, lateShift.getEmployeeNames().size());
        assertTrue(lateShift.getEmployeeNames().contains("Bob Johnson"));
        assertTrue(lateShift.getEmployeeNames().contains("Alice Wilson"));
        assertEquals("11:30 - 20:00", lateShift.getTimeRange());
    }

    @Test
    void getScheduleForDate_EmptySchedule_ReturnsEmptyShifts() {
        // Arrange
        when(scheduleEntryRepository.findByDate(testDate)).thenReturn(List.of());

        // Act
        ScheduleResponse result = scheduleService.getScheduleForDate(testDate);

        // Assert
        assertNotNull(result);
        assertEquals(testDate, result.getDate());
        assertEquals(2, result.getShifts().size());

        // Both shifts should be empty
        for (ScheduleResponse.ShiftInfo shift : result.getShifts()) {
            assertTrue(shift.getEmployeeNames().isEmpty());
        }
    }

    @Test
    void getScheduleForDate_PartialSchedule_ReturnsPartialData() {
        // Arrange - Only early shift has employees
        List<ScheduleEntry> entries = Arrays.asList(entry1, entry2);
        when(scheduleEntryRepository.findByDate(testDate)).thenReturn(entries);

        // Act
        ScheduleResponse result = scheduleService.getScheduleForDate(testDate);

        // Assert
        assertNotNull(result);
        assertEquals(testDate, result.getDate());

        ScheduleResponse.ShiftInfo earlyShift = result.getShifts().stream()
                .filter(shift -> shift.getShiftType() == ShiftType.EARLY_SHIFT)
                .findFirst()
                .orElseThrow();
        assertEquals(2, earlyShift.getEmployeeNames().size());

        ScheduleResponse.ShiftInfo lateShift = result.getShifts().stream()
                .filter(shift -> shift.getShiftType() == ShiftType.LATE_SHIFT)
                .findFirst()
                .orElseThrow();
        assertTrue(lateShift.getEmployeeNames().isEmpty());
    }

    @Test
    void getScheduleForDate_SingleEmployeePerShift_ReturnsCorrectData() {
        // Arrange - Only one employee per shift
        List<ScheduleEntry> entries = Arrays.asList(entry1, entry3);
        when(scheduleEntryRepository.findByDate(testDate)).thenReturn(entries);

        // Act
        ScheduleResponse result = scheduleService.getScheduleForDate(testDate);

        // Assert
        ScheduleResponse.ShiftInfo earlyShift = result.getShifts().stream()
                .filter(shift -> shift.getShiftType() == ShiftType.EARLY_SHIFT)
                .findFirst()
                .orElseThrow();
        assertEquals(1, earlyShift.getEmployeeNames().size());
        assertEquals("John Doe", earlyShift.getEmployeeNames().getFirst());

        ScheduleResponse.ShiftInfo lateShift = result.getShifts().stream()
                .filter(shift -> shift.getShiftType() == ShiftType.LATE_SHIFT)
                .findFirst()
                .orElseThrow();
        assertEquals(1, lateShift.getEmployeeNames().size());
        assertEquals("Bob Johnson", lateShift.getEmployeeNames().getFirst());
    }
}