package hr.algebra.project_manager.task.service;

import hr.algebra.project_manager.common.WorkStatus;

import java.time.LocalDate;

public record TaskCommand(String title,
                          String description,
                          Long assigneeId,
                          WorkStatus status,
                          Integer estimateDays,
                          LocalDate dueDate) {
}
