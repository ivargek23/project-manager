package hr.algebra.project_manager.task.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubtaskForm {

    @NotBlank(message = "{subtask.title.required}")
    @Size(max = 200, message = "{subtask.title.size}")
    private String title;
}
