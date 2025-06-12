package com.prototype.staffplanner.service;

import com.prototype.staffplanner.dto.WishBookEntryResponse;
import com.prototype.staffplanner.dto.WishBookRequest;
import com.prototype.staffplanner.model.Employee;
import com.prototype.staffplanner.model.WishBookEntry;
import com.prototype.staffplanner.repository.EmployeeRepository;
import com.prototype.staffplanner.repository.WishBookEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishBookEntryService {

    private final WishBookEntryRepository wishBookEntryRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Adds a new wish book entry for an employee.
     * If the employee does not exist, it creates a new employee record.
     *
     * @param request the wish book request containing employee name, date, and shift type
     * @return the saved WishBookEntry
     */
    @Transactional
    public WishBookEntryResponse addWishBookEntry(WishBookRequest request) {
        // Check if the employee exists, if not, create a new one
        var employee = employeeRepository.findByName(request.employeeName())
                .orElseGet(() -> employeeRepository.save(
                        Employee.builder()
                                .name(request.employeeName())
                                .build()
                ));

        var entry = WishBookEntry.builder()
                .employee(employee)
                .date(request.date())
                .shiftType(request.shiftType())
                .build();

        entry = wishBookEntryRepository.save(entry);

        return new WishBookEntryResponse(entry.getEmployee().getName(),
                entry.getDate(),
                entry.getShiftType());
    }

}
