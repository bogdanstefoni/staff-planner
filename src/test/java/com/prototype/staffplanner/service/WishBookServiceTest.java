package com.prototype.staffplanner.service;

import com.prototype.staffplanner.dto.WishBookEntryResponse;
import com.prototype.staffplanner.dto.WishBookRequest;
import com.prototype.staffplanner.enums.ShiftType;
import com.prototype.staffplanner.model.Employee;
import com.prototype.staffplanner.model.WishBookEntry;
import com.prototype.staffplanner.repository.EmployeeRepository;
import com.prototype.staffplanner.repository.WishBookEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishBookServiceTest {

    @Mock
    private WishBookEntryRepository wishBookEntryRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private WishBookEntryService wishBookEntryService;

    private Employee testEmployee;
    private WishBookRequest testRequest;
    private WishBookEntry testEntry;

    @BeforeEach
    void setUp() {
        testEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .isAdmin(false)
                .build();

        testRequest = new WishBookRequest("John Doe", LocalDate.of(2025, 6, 15), ShiftType.EARLY_SHIFT);

        testEntry = WishBookEntry.builder()
                .id(1L)
                .employee(testEmployee)
                .date(LocalDate.of(2025, 6, 15))
                .shiftType(ShiftType.EARLY_SHIFT)
                .build();
    }

    @Test
    void addWishBookEntry_ExistingEmployee_Success() {
        // Arrange
        when(employeeRepository.findByName("John Doe")).thenReturn(Optional.of(testEmployee));
        when(wishBookEntryRepository.save(any(WishBookEntry.class))).thenReturn(testEntry);

        // Act
        WishBookEntryResponse result = wishBookEntryService.addWishBookEntry(testRequest);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.employeeName());
        assertEquals(LocalDate.of(2025, 6, 15), result.date());
        assertEquals(ShiftType.EARLY_SHIFT, result.shiftType());

        verify(employeeRepository).findByName("John Doe");
        verify(wishBookEntryRepository).save(any(WishBookEntry.class));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void addWishBookEntry_NewEmployee_CreatesEmployeeAndEntry() {
        // Arrange
        Employee newEmployee = Employee.builder()
                .id(2L)
                .name("Jane Smith")
                .isAdmin(false)
                .build();

        WishBookEntry newEntry = WishBookEntry.builder()
                .id(2L)
                .employee(newEmployee)
                .date(LocalDate.of(2025, 6, 15))
                .shiftType(ShiftType.LATE_SHIFT)
                .build();

        when(employeeRepository.findByName("Jane Smith")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(newEmployee);
        when(wishBookEntryRepository.save(any(WishBookEntry.class))).thenReturn(newEntry);

        WishBookRequest newRequest = new WishBookRequest("Jane Smith", LocalDate.of(2025, 6, 15), ShiftType.LATE_SHIFT);

        // Act
        WishBookEntryResponse result = wishBookEntryService.addWishBookEntry(newRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Jane Smith", result.employeeName());
        assertEquals(LocalDate.of(2025, 6, 15), result.date());
        assertEquals(ShiftType.LATE_SHIFT, result.shiftType());

        verify(employeeRepository).findByName("Jane Smith");
        verify(employeeRepository).save(any(Employee.class));
        verify(wishBookEntryRepository).save(any(WishBookEntry.class));
    }

    @Test
    void addWishBookEntry_DifferentShiftTypes_HandledCorrectly() {
        // Arrange
        when(employeeRepository.findByName("John Doe")).thenReturn(Optional.of(testEmployee));

        WishBookEntry lateShiftEntry = WishBookEntry.builder()
                .id(2L)
                .employee(testEmployee)
                .date(LocalDate.of(2025, 6, 15))
                .shiftType(ShiftType.LATE_SHIFT)
                .build();

        when(wishBookEntryRepository.save(any(WishBookEntry.class))).thenReturn(lateShiftEntry);

        WishBookRequest lateShiftRequest = new WishBookRequest("John Doe", LocalDate.of(2025, 6, 15), ShiftType.LATE_SHIFT);

        // Act
        WishBookEntryResponse result = wishBookEntryService.addWishBookEntry(lateShiftRequest);

        // Assert
        assertEquals(ShiftType.LATE_SHIFT, result.shiftType());
    }
}