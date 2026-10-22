package hr.algebra.project_manager.task.service;

import hr.algebra.project_manager.common.WorkStatus;

public record TaskRow(Long id,
                      String title,
                      String assigneeName,
                      int estimateDays,
                      WorkStatus status,
                      long dependencyCount,
                      int subtaskCount,
                      int subtasksDone) {
}
