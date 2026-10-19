package hr.algebra.project_manager.project.service;

import hr.algebra.project_manager.common.WorkStatus;

import java.time.LocalDate;

public record ProjectCommand(String name, String description, LocalDate startDate,
                             LocalDate dueDate, Long leadId, WorkStatus status) {
}
