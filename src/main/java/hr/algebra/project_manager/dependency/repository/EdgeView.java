package hr.algebra.project_manager.dependency.repository;

import hr.algebra.project_manager.dependency.domain.DependencyType;

public record EdgeView(Long predecessorId, Long successorId, DependencyType dependencyType, int lagDays) {
}
