package com.prototype.staffplanner.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record PlanningRequest(

        @NotNull
        LocalDate date,

        @NotEmpty
        List<Long> wishBookEntryIds
) {
}
