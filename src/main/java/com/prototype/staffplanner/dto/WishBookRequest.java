package com.prototype.staffplanner.dto;

import com.prototype.staffplanner.enums.ShiftType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record WishBookRequest(

        @NotNull
        String employeeName,

        @NotNull
        LocalDate date,

        @NotNull
        ShiftType shiftType
) {}
