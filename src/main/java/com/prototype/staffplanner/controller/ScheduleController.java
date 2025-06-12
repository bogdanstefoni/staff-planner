package com.prototype.staffplanner.controller;

import com.prototype.staffplanner.dto.ScheduleResponse;
import com.prototype.staffplanner.service.ScheduleService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "Staff Planner API",
                version = "1.0",
                description = "API for managing staff schedules and wish book entries"
        ))
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule")
public class ScheduleController {

    private final ScheduleService service;

    @Operation(
            summary = "Get schedule for a specific date",
            description = "Retrieves the schedule for a given date, including shift types and assigned employees."
    )
    @GetMapping("/{date}")
    @ResponseStatus(HttpStatus.OK)
    public ScheduleResponse getSchedule(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getScheduleForDate(date);
    }
}
