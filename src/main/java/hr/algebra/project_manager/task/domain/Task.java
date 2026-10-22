package hr.algebra.project_manager.task.domain;

import hr.algebra.project_manager.common.WorkStatus;
import hr.algebra.project_manager.project.domain.Project;
import hr.algebra.project_manager.user.domain.AppUser;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_seq")
    @SequenceGenerator(name = "task_seq", sequenceName = "task_seq", allocationSize = 50)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    @Setter(AccessLevel.NONE)
    private Project project;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter(AccessLevel.NONE)
    private WorkStatus status = WorkStatus.TODO;

    @Column(nullable = false)
    private int estimateDays;

    private LocalDate startDate;

    private LocalDate dueDate;

    @Setter(AccessLevel.NONE)
    private Instant completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private AppUser assignee;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    @Version
    @Setter(AccessLevel.NONE)
    private Long version;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    @Setter(AccessLevel.NONE)
    private List<Subtask> subtasks = new ArrayList<>();

    public Task(Project project, String title, int estimateDays) {
        this.project = project;
        this.title = title;
        this.estimateDays = estimateDays;
    }

    public void setStatus(WorkStatus status) {
        if (status == WorkStatus.DONE && this.status != WorkStatus.DONE) {
            this.completedAt = Instant.now();
        } else if (status != WorkStatus.DONE) {
            this.completedAt = null;
        }
        this.status = status;
    }

    public Subtask addSubtask(String title) {
        Subtask subtask = new Subtask(this, title, subtasks.size());
        subtasks.add(subtask);
        return subtask;
    }

    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask);
    }
}
