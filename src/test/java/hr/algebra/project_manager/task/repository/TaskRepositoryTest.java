package hr.algebra.project_manager.task.repository;

import hr.algebra.project_manager.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class TaskRepositoryTest {

    @Autowired
    private TaskRepository tasks;

    @Test
    void taskIsFoundInItsOwnProject() {
        assertThat(tasks.findByIdAndProjectId(101L, 1L))
                .hasValueSatisfying(task -> assertThat(task.getTitle()).isEqualTo("Research & content audit"));
    }

    @Test
    void taskIsNotFoundThroughAnotherProject() {
        assertThat(tasks.findByIdAndProjectId(101L, 2L)).isEmpty();
    }
}