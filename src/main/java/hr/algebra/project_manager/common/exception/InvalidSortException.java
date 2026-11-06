package hr.algebra.project_manager.common.exception;

import java.util.Set;

public class InvalidSortException extends RuntimeException {
    public InvalidSortException(String property, Set<String> allowed) {
        super("Cannot sort by '" + property + "'. Allowed properties to sort by: " + allowed);
    }
}
