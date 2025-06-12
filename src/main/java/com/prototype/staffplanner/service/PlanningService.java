package com.prototype.staffplanner.service;

import com.prototype.staffplanner.dto.PlanningRequest;
import com.prototype.staffplanner.enums.ShiftType;
import com.prototype.staffplanner.model.ScheduleEntry;
import com.prototype.staffplanner.model.WishBookEntry;
import com.prototype.staffplanner.repository.ScheduleEntryRepository;
import com.prototype.staffplanner.repository.WishBookEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanningService {

    private final WishBookEntryRepository wishBookEntryRepository;
    private final ScheduleEntryRepository scheduleEntryRepository;

    /**
     * Creates a schedule plan based on the provided wish book entries.
     * @param request the planning request containing wish book entry IDs and the date for the plan
     * @return a list of created ScheduleEntry objects
     */
    @Transactional
    public List<ScheduleEntry> createPlan(PlanningRequest request) {
        var wishEntries = wishBookEntryRepository.findAllById(request.wishBookEntryIds());

        if (wishEntries.isEmpty()) {
            throw new IllegalArgumentException("No wish book entries found for the provided IDs.");
        }

        // Validate the wish entries
        validateWishEntries(wishEntries, request);

        // Clear existing schedule entries for the date
        scheduleEntryRepository.deleteByDate(request.date());

        var scheduleEntries = wishEntries.stream()
                .map(entry -> ScheduleEntry.builder()
                        .employee(entry.getEmployee())
                        .date(request.date())
                        .shiftType(entry.getShiftType())
                        .build())
                .toList();

        return scheduleEntryRepository.saveAll(scheduleEntries);
    }

    private void validateWishEntries(List<WishBookEntry> wishEntries, PlanningRequest request) {
        // Validate that all wish book entries have the same date
        boolean allSameDate = wishEntries.stream()
                .allMatch(entry -> entry.getDate().equals(request.date()));
        if (!allSameDate) {
            throw new IllegalArgumentException("All wish book entries must have the same date.");
        }

        Map<ShiftType, List<WishBookEntry>> entriesByShiftType = wishEntries.stream()
                .collect(Collectors.groupingBy(WishBookEntry::getShiftType));

        // Validate that we have exactly 2 employees per shift type
        for (ShiftType shiftType : ShiftType.values()) {
            List<WishBookEntry> entries = entriesByShiftType.get(shiftType);
            if (entries == null || entries.size() != 2) {
                throw new IllegalArgumentException("Exactly 2 employees are required for each shift type: " + shiftType);
            }
        }

        // Validate that no employee is assigned to more than one shift type
        var employeeIds = wishEntries.stream()
                .map(entry -> entry.getEmployee().getId())
                .collect(Collectors.toSet());

        long uniqueEmployeeCount = employeeIds.size();
        if (uniqueEmployeeCount != wishEntries.size()) {
            throw new IllegalArgumentException("An employee cannot be assigned to more than one shift type.");
        }
    }
}
