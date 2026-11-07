package hr.algebra.project_manager.dependency.service;

public class SelfDependencyException extends RuntimeException {
    public SelfDependencyException() {
        super("A task can't depend on itself");
    }
}