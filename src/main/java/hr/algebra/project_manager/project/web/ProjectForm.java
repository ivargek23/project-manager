package hr.algebra.project_manager.project.web;

import hr.algebra.project_manager.common.WorkStatus;
import hr.algebra.project_manager.common.validation.HasDateRange;
import hr.algebra.project_manager.common.validation.ValidDateRange;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@ValidDateRange
public class ProjectForm implements HasDateRange {

    @NotBlank(message = "{project.name.required}")
    @Size(max = 150, message = "{project.name.size}")
    private String name;

    @Size(max = 2000, message = "{project.description.size}")
    private String description;

    @NotNull(message = "{project.startDate.required}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @NotNull(message = "{project.dueDate.required}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;

    @NotNull(message = "{project.lead.required}")
    private Long leadId;

    @NotNull(message = "{project.status.required}")
    private WorkStatus status = WorkStatus.TODO;

    private Long version;
}
