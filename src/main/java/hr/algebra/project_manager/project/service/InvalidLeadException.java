package hr.algebra.project_manager.project.service;

public class InvalidLeadException extends RuntimeException {
    public InvalidLeadException(Long userId) {
        super("User " + userId + " cannot lead a project");
    }
}
