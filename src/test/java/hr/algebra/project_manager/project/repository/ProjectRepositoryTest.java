package hr.algebra.project_manager.project.repository;

import hr.algebra.project_manager.TestcontainersConfiguration;
import hr.algebra.project_manager.common.WorkStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ProjectRepositoryTest {

    private static final PageRequest FIRST_PAGE = PageRequest.of(0, 5, Sort.by("name"));

    @Autowired
    private ProjectRepository projects;

    @Test
    void emptySearchReturnsAllProjectsPagedAndSorted() {
        Page<ProjectSummary> page = projects.search("", null, FIRST_PAGE);

        assertThat(page.getTotalElements()).isEqualTo(8);
        assertThat(page.getContent()).hasSize(5);
        assertThat(page.getContent().getFirst().name()).isEqualTo("Customer Portal Update");
    }

    @Test
    void searchByTextIgnoresCaseAndCountsTasks() {
        Page<ProjectSummary> page = projects.search("WEB", null, FIRST_PAGE);

        assertThat(page.getContent()).singleElement().satisfies(project -> {
            assertThat(project.name()).isEqualTo("Website Redesign");
            assertThat(project.leadName()).isEqualTo("Pero Peric");
            assertThat(project.taskCount()).isEqualTo(20L);
        });
    }

    @Test
    void searchByStatusReturnsOnlyMatchingProjects() {
        Page<ProjectSummary> page = projects.search("", WorkStatus.DONE, FIRST_PAGE);

        assertThat(page.getContent())
                .extracting(ProjectSummary::name)
                .containsExactly("HR System Integration", "Security Audit Q4");
    }
}
