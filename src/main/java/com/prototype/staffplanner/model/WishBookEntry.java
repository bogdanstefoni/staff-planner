package com.prototype.staffplanner.model;

import com.prototype.staffplanner.enums.ShiftType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "wish_book_entries", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "date", "shift_type"})
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WishBookEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private ShiftType shiftType;
}
