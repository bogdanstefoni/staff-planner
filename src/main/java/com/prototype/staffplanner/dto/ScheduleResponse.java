package com.prototype.staffplanner.dto;

import com.prototype.staffplanner.enums.ShiftType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleResponse {

    LocalDate date;
    List<ShiftInfo> shifts;

    @Getter
    public static class ShiftInfo {
        private final ShiftType shiftType;
        private final String timeRange;
        private final List<String> employeeNames;

        public ShiftInfo(ShiftType shiftType, List<String> employeeNames) {
            this.shiftType = shiftType;
            this.timeRange = shiftType.getTimeRange();
            this.employeeNames = employeeNames;
        }
    }
}
