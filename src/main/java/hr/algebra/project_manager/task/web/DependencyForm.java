package hr.algebra.project_manager.task.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DependencyForm {

    @NotNull(message = "{dependency.predecessor.required}")
    private Long predecessorId;

    @NotNull(message = "{dependency.lagDays.required}")
    @Min(value = 0, message = "{dependency.lagDays.range}")
    @Max(value = 100, message = "{dependency.lagDays.range}")
    private Integer lagDays = 0;
}