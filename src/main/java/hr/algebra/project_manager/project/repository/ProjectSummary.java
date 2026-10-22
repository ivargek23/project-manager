package hr.algebra.project_manager.project.repository;

import hr.algebra.project_manager.common.WorkStatus;

import java.time.LocalDate;

public record ProjectSummary(Long id, String name, String leadName, LocalDate dueDate,
                             WorkStatus status, Long taskCount) {
}
