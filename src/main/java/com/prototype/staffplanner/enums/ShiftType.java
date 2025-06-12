package com.prototype.staffplanner.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ShiftType {
    EARLY_SHIFT("07:00 - 15:30"),
    LATE_SHIFT("11:30 - 20:00");

    private final String timeRange;
}
