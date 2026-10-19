package hr.algebra.project_manager.task.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subtask {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subtask_seq")
    @SequenceGenerator(name = "subtask_seq", sequenceName = "subtask_seq", allocationSize = 50)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id")
    @Setter(AccessLevel.NONE)
    private Task task;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private boolean done;

    @Column(nullable = false)
    private int position;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    @Version
    @Setter(AccessLevel.NONE)
    private Long version;

    Subtask(Task task, String title, int position) {
        this.task = task;
        this.title = title;
        this.position = position;
    }
}
