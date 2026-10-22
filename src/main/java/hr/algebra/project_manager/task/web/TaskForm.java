package hr.algebra.project_manager.task.web;

import hr.algebra.project_manager.common.WorkStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class TaskForm {

    @NotBlank(message = "{task.title.required}")
    @Size(max = 200, message = "{task.title.size}")
    private String title;

    @Size(max = 2000, message = "{task.description.size}")
    private String description;

    private Long assigneeId;

    @NotNull(message = "{task.status.required}")
    private WorkStatus status = WorkStatus.TODO;

    @NotNull(message = "{task.estimateDays.required}")
    @Min(value = 1, message = "{task.estimateDays.range}")
    @Max(value = 100, message = "{task.estimateDays.range}")
    private Integer estimateDays;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;

    private Long version;
}
