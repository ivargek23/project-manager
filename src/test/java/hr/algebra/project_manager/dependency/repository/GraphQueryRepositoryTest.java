package hr.algebra.project_manager.dependency.repository;

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
class GraphQueryRepositoryTest {

    private static final Long WEBSITE_REDESIGN = 1L;
    private static final Long MOBILE_APP = 2L;

    @Autowired
    private GraphQueryRepository graph;

    @Test
    void endOfALongChainIsReachableFromItsStart() {
        // 101 -> 102 -> 104 -> 105 -> 106 -> 107 -> 108, so adding 108 -> 101 would close a loop
        assertThat(graph.isReachable(WEBSITE_REDESIGN, 101L, 108L)).isTrue();
    }

    @Test
    void startOfAChainIsNotReachableFromItsEnd() {
        // nothing leads back from 108 to 101, so adding 101 -> 108 is safe
        assertThat(graph.isReachable(WEBSITE_REDESIGN, 108L, 101L)).isFalse();
    }

    @Test
    void unconnectedTasksAreNotReachable() {
        assertThat(graph.isReachable(WEBSITE_REDESIGN, 110L, 109L)).isFalse();
    }

    @Test
    void searchStaysInsideTheGivenProject() {
        assertThat(graph.isReachable(MOBILE_APP, 101L, 108L)).isFalse();
    }

    @Test
    void excludedDependencyIsIgnoredDuringTheSearch() {
        assertThat(graph.isReachable(WEBSITE_REDESIGN, 101L, 102L)).isTrue();
        assertThat(graph.isReachable(WEBSITE_REDESIGN, 101L, 102L, 1L)).isFalse();
    }

    @Test
    void pathTitlesFollowTheDependenciesInOrder() {
        assertThat(graph.findPathTitles(WEBSITE_REDESIGN, 101L, 104L))
                .containsExactly("Research & content audit", "Information architecture", "Visual design system");
    }

    @Test
    void pathTitlesAreEmptyWhenThereIsNoPath() {
        assertThat(graph.findPathTitles(WEBSITE_REDESIGN, 108L, 101L)).isEmpty();
    }
}