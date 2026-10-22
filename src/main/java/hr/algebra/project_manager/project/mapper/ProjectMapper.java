package hr.algebra.project_manager.project.mapper;

import hr.algebra.project_manager.project.domain.Project;
import hr.algebra.project_manager.project.service.ProjectCommand;
import hr.algebra.project_manager.project.web.ProjectForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProjectMapper {

    ProjectCommand toCommand(ProjectForm form);

    @Mapping(target = "leadId", source = "lead.id")
    ProjectForm toForm(Project project);
}
