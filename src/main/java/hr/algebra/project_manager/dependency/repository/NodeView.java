package hr.algebra.project_manager.dependency.repository;

import hr.algebra.project_manager.common.WorkStatus;

public record NodeView(Long id, String title, int estimateDays, WorkStatus workStatus) {
}
