package hr.algebra.project_manager.dependency.service;

import java.util.List;

public class CyclicDependencyException extends RuntimeException {

    private final List<String> cycle;

    public CyclicDependencyException(List<String> cycle) {
        super("This dependency would create a cycle: " + String.join(" → ", cycle));
        this.cycle = List.copyOf(cycle);
    }

    public List<String> getCycle() {
        return cycle;
    }
}



