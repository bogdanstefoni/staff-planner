package com.prototype.staffplanner.repository;

import com.prototype.staffplanner.model.ScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, Long> {

    List<ScheduleEntry> findByDate(LocalDate date);
    List<ScheduleEntry> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    void deleteByDate(LocalDate date);
}
