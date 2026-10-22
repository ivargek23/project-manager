package hr.algebra.project_manager.dependency.domain;

import hr.algebra.project_manager.common.BaseEntity;
import hr.algebra.project_manager.task.domain.Task;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/** Many-to-many Task <-> Task with attributes (type, lag), mapped as its own entity. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskDependency extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_dependency_seq")
    @SequenceGenerator(name = "task_dependency_seq", sequenceName = "task_dependency_seq", allocationSize = 50)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "predecessor_id")
    private Task predecessor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "successor_id")
    private Task successor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DependencyType type = DependencyType.FINISH_TO_START;

    @Setter
    @Column(nullable = false)
    private int lagDays;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public TaskDependency(Task predecessor, Task successor, int lagDays) {
        Long predecessorProject = predecessor.getProject().getId();
        if (!predecessorProject.equals(successor.getProject().getId())) {
            throw new IllegalArgumentException("Both tasks must belong to the same project");
        }
        if (predecessor.getId() != null && predecessor.getId().equals(successor.getId())) {
            throw new IllegalArgumentException("A task can't depend on itself");
        }
        this.projectId = predecessorProject;
        this.predecessor = predecessor;
        this.successor = successor;
        this.lagDays = lagDays;
    }
}
