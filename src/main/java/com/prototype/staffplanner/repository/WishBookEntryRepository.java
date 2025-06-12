package com.prototype.staffplanner.repository;

import com.prototype.staffplanner.model.WishBookEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WishBookEntryRepository extends JpaRepository<WishBookEntry, Long> {

    List<WishBookEntry> findByDate(LocalDate date);
    List<WishBookEntry> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
}
