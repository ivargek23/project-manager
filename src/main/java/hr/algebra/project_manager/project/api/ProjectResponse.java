package hr.algebra.project_manager.project.api;

import hr.algebra.project_manager.common.WorkStatus;

import java.time.LocalDate;

public record ProjectResponse (
        Long id,
        String name,
        String description,
        LocalDate startDate,
        LocalDate dueDate,
        WorkStatus status,
        String leadName,
        Long version
){
}
