package hr.algebra.project_manager.common.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String entity, Object id) {
        super(entity + " " + id + " not found");
    }
}
