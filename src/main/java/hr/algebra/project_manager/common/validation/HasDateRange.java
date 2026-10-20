package hr.algebra.project_manager.common.validation;

import java.time.LocalDate;

public interface HasDateRange {
    LocalDate getStartDate();
    LocalDate getDueDate();
}
