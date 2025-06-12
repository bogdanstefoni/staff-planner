package com.prototype.staffplanner.service;

import com.prototype.staffplanner.dto.ScheduleResponse;
import com.prototype.staffplanner.enums.ShiftType;
import com.prototype.staffplanner.model.ScheduleEntry;
import com.prototype.staffplanner.repository.ScheduleEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleEntryRepository scheduleEntryRepository;

    /**
     * Retrieves the schedule for a specific date.
     * @param date the date for which to retrieve the schedule
     * @return a ScheduleResponse containing the schedule entries grouped by shift type
     */
    public ScheduleResponse getScheduleForDate(LocalDate date) {
        // Fetch schedule entries for the given date
        var scheduleEntries = scheduleEntryRepository.findByDate(date);

        Map<ShiftType, List<ScheduleEntry>> entriesByShift = scheduleEntries.stream()
                .collect(Collectors.groupingBy(ScheduleEntry::getShiftType));

        // Create ShiftInfo objects for each ShiftType
        var shiftInfos = Arrays.stream(ShiftType.values())
                .map(shiftType -> {
                    var employeeNames = entriesByShift.getOrDefault(shiftType, List.of())
                            .stream()
                            .map(entry -> entry.getEmployee().getName())
                            .toList();
                    return new ScheduleResponse.ShiftInfo(shiftType, employeeNames);
                })
                .toList();

        return new ScheduleResponse(date, shiftInfos);
    }
}
