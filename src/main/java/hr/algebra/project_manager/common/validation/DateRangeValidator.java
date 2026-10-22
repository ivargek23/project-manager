package hr.algebra.project_manager.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, HasDateRange> {
    @Override
    public boolean isValid(HasDateRange form, ConstraintValidatorContext context) {

        if (form.getStartDate() == null || form.getDueDate() == null) {
            return true;
        }

        if (!form.getDueDate().isBefore(form.getStartDate())) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addPropertyNode("dueDate").addConstraintViolation();
        return false;
    }
}
