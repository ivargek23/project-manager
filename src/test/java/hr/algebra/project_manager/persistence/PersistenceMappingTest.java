package hr.algebra.project_manager.persistence;

import hr.algebra.project_manager.TestcontainersConfiguration;
import hr.algebra.project_manager.dependency.domain.DependencyType;
import hr.algebra.project_manager.dependency.domain.TaskDependency;
import hr.algebra.project_manager.dependency.repository.EdgeView;
import hr.algebra.project_manager.dependency.repository.GraphQueryRepository;
import hr.algebra.project_manager.project.repository.ProjectRepository;
import hr.algebra.project_manager.task.domain.Subtask;
import hr.algebra.project_manager.task.domain.Task;
import hr.algebra.project_manager.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class PersistenceMappingTest {
    private static final long WEBSITE_REDESIGN = 1L;

    @Autowired
    private TaskRepository tasks;

    @Autowired
    private GraphQueryRepository graph;

    @Autowired
    private ProjectRepository projects;

    @Test
    void graphReadsReturnTheSeededWebsiteRedesignGraph() {
        assertThat(graph.findNodes(WEBSITE_REDESIGN)).hasSize(20);
        assertThat(graph.findEdges(WEBSITE_REDESIGN))
                .hasSize(17)
                .contains(new EdgeView(105L, 106L, DependencyType.FINISH_TO_START, 0));
    }

    @Test
    void newTaskAndItsSubtasksGetIdsAfterTheSeedData() {
        Task task = new Task(projects.getReferenceById(WEBSITE_REDESIGN), "Write release notes", 1);
        task.addSubtask("Draft");
        task.addSubtask("Review");

        Task saved = tasks.saveAndFlush(task);

        assertThat(saved.getId()).isGreaterThan(314L);
        assertThat(saved.getSubtasks()).extracting(Subtask::getId).doesNotContainNull();
    }

    @Test
    void dependencyBetweenTwoProjectsIsRejected() {
        Task websiteTask = tasks.findById(101L).orElseThrow();
        Task mobileTask = tasks.findById(201L).orElseThrow();

        assertThatThrownBy(() -> new TaskDependency(websiteTask, mobileTask, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
