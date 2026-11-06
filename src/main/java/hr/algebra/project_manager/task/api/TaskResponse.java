package hr.algebra.project_manager.task.api;

import hr.algebra.project_manager.common.WorkStatus;

import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String description,
        WorkStatus status,
        Integer estimateDays,
        LocalDate dueDate,
        Long assigneeId,
        String assigneeName,
        Long version
) {
}
