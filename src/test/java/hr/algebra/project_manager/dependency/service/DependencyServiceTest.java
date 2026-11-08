package hr.algebra.project_manager.dependency.service;

import hr.algebra.project_manager.dependency.domain.TaskDependency;
import hr.algebra.project_manager.dependency.repository.GraphQueryRepository;
import hr.algebra.project_manager.dependency.repository.TaskDependencyRepository;
import hr.algebra.project_manager.project.domain.Project;
import hr.algebra.project_manager.project.repository.ProjectRepository;
import hr.algebra.project_manager.task.domain.Task;
import hr.algebra.project_manager.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DependencyServiceTest {

    private static final Long PROJECT_ID = 1L;

    @Mock private ProjectRepository projectRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private TaskDependencyRepository dependencyRepository;
    @Mock private GraphQueryRepository graphQueryRepository;

    @InjectMocks
    private DependencyService dependencyService;

    private Project project;

    @BeforeEach
    void setUp() {
        project = mock(Project.class);
        lenient().when(project.getId()).thenReturn(PROJECT_ID);
        lenient().when(projectRepository.findByIdForUpdate(PROJECT_ID)).thenReturn(Optional.of(project));
    }

    private Task task(Long id) {
        Task task = mock(Task.class);
        lenient().when(task.getId()).thenReturn(id);
        lenient().when(task.getProject()).thenReturn(project);
        lenient().when(taskRepository.findByIdAndProjectId(id, PROJECT_ID)).thenReturn(Optional.of(task));
        return task;
    }

    private TaskDependency existingDependency(Long id, Long predecessorId, Long successorId) {
        TaskDependency dependency = mock(TaskDependency.class);
        Task predecessor = task(predecessorId);
        Task successor = task(successorId);
        lenient().when(dependency.getPredecessor()).thenReturn(predecessor);
        lenient().when(dependency.getSuccessor()).thenReturn(successor);
        when(dependencyRepository.findByIdAndProjectId(id, PROJECT_ID)).thenReturn(Optional.of(dependency));
        return dependency;
    }

    @Test
    void addLocksTheProjectBeforeCheckingForACycleAndThenSaves() {
        task(101L);
        task(102L);
        when(dependencyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        dependencyService.addDependency(PROJECT_ID, 101L, 102L, 0);

        InOrder order = inOrder(projectRepository, graphQueryRepository, dependencyRepository, project);
        order.verify(projectRepository).findByIdForUpdate(PROJECT_ID);
        order.verify(graphQueryRepository).isReachable(PROJECT_ID, 102L, 101L, -1L);
        order.verify(dependencyRepository).save(any(TaskDependency.class));
        order.verify(project).bumpScheduleVersion();
    }

    @Test
    void addRejectsACycleAndReportsTheWholeLoop() {
        task(101L);
        task(103L);
        when(graphQueryRepository.isReachable(PROJECT_ID, 101L, 103L, -1L)).thenReturn(true);
        when(graphQueryRepository.findPathTitles(PROJECT_ID, 101L, 103L, -1L))
                .thenReturn(List.of("Design", "Build", "Test"));

        assertThatThrownBy(() -> dependencyService.addDependency(PROJECT_ID, 103L, 101L, 0))
                .isInstanceOfSatisfying(CyclicDependencyException.class, ex ->
                        org.assertj.core.api.Assertions.assertThat(ex.getCycle())
                                .containsExactly("Test", "Design", "Build", "Test"));

        verify(dependencyRepository, never()).save(any());
        verify(project, never()).bumpScheduleVersion();
    }

    @Test
    void addRejectsADuplicateWithoutRunningTheCycleCheck() {
        task(101L);
        task(102L);
        when(dependencyRepository.existsByPredecessorIdAndSuccessorId(101L, 102L)).thenReturn(true);

        assertThatThrownBy(() -> dependencyService.addDependency(PROJECT_ID, 101L, 102L, 0))
                .isInstanceOf(DuplicateDependencyException.class);

        verifyNoInteractions(graphQueryRepository);
        verify(dependencyRepository, never()).save(any());
    }

    @Test
    void addRejectsATaskDependingOnItself() {
        assertThatThrownBy(() -> dependencyService.addDependency(PROJECT_ID, 101L, 101L, 0))
                .isInstanceOf(SelfDependencyException.class);

        verifyNoInteractions(graphQueryRepository);
        verify(dependencyRepository, never()).save(any());
    }

    @Test
    void lagOnlyEditSkipsTheCycleCheckButStillBumpsTheScheduleVersion() {
        TaskDependency dependency = existingDependency(7L, 101L, 102L);

        dependencyService.updateDependency(PROJECT_ID, 7L, 101L, 102L, 3);

        verify(dependency).setLagDays(3);
        verify(dependency, never()).relink(any(), any());
        verifyNoInteractions(graphQueryRepository);
        verify(project).bumpScheduleVersion();
    }

    @Test
    void editThatChangesTheTasksChecksForACycleWithoutCountingItself() {
        TaskDependency dependency = existingDependency(7L, 101L, 102L);
        task(103L);

        dependencyService.updateDependency(PROJECT_ID, 7L, 103L, 102L, 0);

        verify(graphQueryRepository).isReachable(PROJECT_ID, 102L, 103L, 7L);
        verify(dependency).relink(any(Task.class), any(Task.class));
        verify(project).bumpScheduleVersion();
    }

    @Test
    void deleteBumpsTheScheduleVersionWithoutACycleCheck() {
        TaskDependency dependency = existingDependency(7L, 101L, 102L);

        dependencyService.deleteDependency(PROJECT_ID, 7L);

        verify(projectRepository).findByIdForUpdate(PROJECT_ID);
        verify(dependencyRepository).delete(dependency);
        verifyNoInteractions(graphQueryRepository);
        verify(project).bumpScheduleVersion();
    }
}