package hr.algebra.project_manager.task.mapper;

import hr.algebra.project_manager.task.api.TaskRequest;
import hr.algebra.project_manager.task.api.TaskResponse;
import hr.algebra.project_manager.task.domain.Task;
import hr.algebra.project_manager.task.service.TaskCommand;
import hr.algebra.project_manager.task.web.TaskForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TaskMapper {

    TaskCommand toCommand(TaskForm form);

    @Mapping(target = "assigneeId", source = "assignee.id")
    TaskForm toForm(Task task);

    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "assigneeName", source = "assignee.displayName")
    TaskResponse toTaskResponse(Task task);

    TaskCommand taskRequestToCommand(TaskRequest request);
}
