package com.prototype.staffplanner.controller;

import com.prototype.staffplanner.dto.WishBookEntryResponse;
import com.prototype.staffplanner.dto.WishBookRequest;
import com.prototype.staffplanner.service.WishBookEntryService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "Staff Planner API",
                version = "1.0",
                description = "API for managing staff schedules and wish book entries"
        ))
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishbook")
public class WishBookController {

    private final WishBookEntryService wishBookEntryService;

    @Operation(
            summary = "Add a new wish book entry",
            description = "Creates a new wish book entry for an employee. If the employee does not exist, it creates a new employee record."
    )
    @PostMapping("/entry")
    @ResponseStatus(HttpStatus.OK)
    public WishBookEntryResponse addWishBookEntry(@Valid @RequestBody WishBookRequest request) {
        return wishBookEntryService.addWishBookEntry(request);
    }
}
