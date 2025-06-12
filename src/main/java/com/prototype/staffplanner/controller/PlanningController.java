package com.prototype.staffplanner.controller;

import com.prototype.staffplanner.dto.PlanningRequest;
import com.prototype.staffplanner.model.ScheduleEntry;
import com.prototype.staffplanner.service.PlanningService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "Staff Planner API",
                version = "1.0",
                description = "API for managing staff schedules and wish book entries"
        ))
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/planning")
public class PlanningController {

    private final PlanningService planningService;

    @Operation(
            summary = "Create a schedule plan",
            description = """
                    Creates a schedule plan based on the provided wish book entries.
                    The request must include wish book entry IDs and the date for the plan.
                    """
    )
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.OK)
    public List<ScheduleEntry> createPlan(@Valid @RequestBody PlanningRequest request) {
        return planningService.createPlan(request);
    }
}
