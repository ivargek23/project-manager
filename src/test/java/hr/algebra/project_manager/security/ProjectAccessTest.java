package hr.algebra.project_manager.security;

import hr.algebra.project_manager.project.repository.ProjectMemberRepository;
import hr.algebra.project_manager.project.repository.ProjectRepository;
import hr.algebra.project_manager.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProjectAccessTest {

    @Mock
    ProjectRepository projectRepository;
    @Mock
    ProjectMemberRepository memberRepository;
    @Mock
    TaskRepository taskRepository;
    @InjectMocks
    ProjectAccess access;

}
