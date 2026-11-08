package hr.algebra.project_manager.dependency.service;

import hr.algebra.project_manager.TestcontainersConfiguration;
import hr.algebra.project_manager.security.AppUserPrincipal;
import hr.algebra.project_manager.user.domain.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class DependencyConcurrencyTest {

    private static final Long WEBSITE_REDESIGN = 1L;
    private static final AppUserPrincipal LEAD =
            new AppUserPrincipal(2L, "pperic", "hash", Role.PROJECT_LEAD, true);
    private static final String TEST_LINKS =
            "predecessor_id IN (111, 112) AND successor_id IN (111, 112)";

    @Autowired
    private DependencyService dependencyService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void removeTestLinks() {
        jdbcTemplate.update("DELETE FROM task_dependency WHERE " + TEST_LINKS);
    }

    @Test
    void oppositeLinksAddedAtTheSameTimeNeverBothSucceed() throws Exception {
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Future<String> first = pool.submit(() -> addAsLead(start, 111L, 112L));
            Future<String> second = pool.submit(() -> addAsLead(start, 112L, 111L));
            start.countDown();

            assertThat(List.of(first.get(10, SECONDS), second.get(10, SECONDS)))
                    .containsExactlyInAnyOrder("saved", "cycle");
        }

        Integer links = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM task_dependency WHERE " + TEST_LINKS, Integer.class);
        assertThat(links).isEqualTo(1);
    }

    private String addAsLead(CountDownLatch start, Long predecessorId, Long successorId) throws InterruptedException {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(LEAD, null, LEAD.getAuthorities()));
        try {
            start.await();
            dependencyService.addDependency(WEBSITE_REDESIGN, predecessorId, successorId, 0);
            return "saved";
        } catch (CyclicDependencyException e) {
            return "cycle";
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}