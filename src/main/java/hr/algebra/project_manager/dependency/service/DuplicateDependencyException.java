package hr.algebra.project_manager.dependency.service;

public class DuplicateDependencyException extends RuntimeException {
    public DuplicateDependencyException(Long predecessorId, Long successorId) {
        super("Task " + successorId + " already depends on task " + predecessorId);
    }
}