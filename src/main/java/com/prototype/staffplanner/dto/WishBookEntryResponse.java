package com.prototype.staffplanner.dto;

import com.prototype.staffplanner.enums.ShiftType;

import java.time.LocalDate;

public record WishBookEntryResponse(
        String employeeName,
        LocalDate date,
        ShiftType shiftType
) {
}
