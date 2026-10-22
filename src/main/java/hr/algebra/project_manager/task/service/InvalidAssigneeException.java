package hr.algebra.project_manager.task.service;

public class InvalidAssigneeException extends RuntimeException {
    public InvalidAssigneeException(Long userId) {
        super("User " + userId + " cannot be assigned to task");
    }
}
