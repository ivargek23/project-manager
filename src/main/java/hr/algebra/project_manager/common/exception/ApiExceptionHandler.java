package hr.algebra.project_manager.common.exception;

import hr.algebra.project_manager.project.service.InvalidLeadException;
import hr.algebra.project_manager.task.service.InvalidAssigneeException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import org.springframework.security.access.AccessDeniedException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Not found", ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleStaleVersion(OptimisticLockingFailureException ex) {
        return problem(HttpStatus.CONFLICT, "Old version",
                "The resource was changed by someone else since you started your changes. Please reload and try again.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "Forbidden", "You do not have permission to perform this action.");
    }

    @ExceptionHandler({InvalidLeadException.class, InvalidAssigneeException.class})
    public ProblemDetail handleInvalidReference(RuntimeException ex) {
        return problem(HttpStatus.UNPROCESSABLE_CONTENT, "Invalid reference", ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail problem = ex.getBody();
        problem.setTitle("Validation failed");

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        problem.setProperty("errors", errors);

        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @ExceptionHandler(InvalidSortException.class)
    public ProblemDetail handleInvalidSort(InvalidSortException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid sort", ex.getMessage());
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
