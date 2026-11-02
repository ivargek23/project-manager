package hr.algebra.project_manager.project.domain;

import hr.algebra.project_manager.common.WorkStatus;
import hr.algebra.project_manager.task.domain.Task;
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
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_seq")
    @SequenceGenerator(name = "project_seq", sequenceName = "project_seq", allocationSize = 50)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkStatus status = WorkStatus.TODO;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lead_id")
    private AppUser lead;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant createdAt;

    @Version
    @Setter(AccessLevel.NONE)
    private Long version;

    // Read side of Project -> Task.
    // No cascade: tasks are saved through TaskRepository, because a project can have many tasks, and we don't want to load all of them just to add one.
    // Deleting a project removes its tasks through ON DELETE CASCADE in the database.
    @OneToMany(mappedBy = "project")
    @Setter(AccessLevel.NONE)
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private List<ProjectMember> members = new ArrayList<>();

    public Project(String name, LocalDate startDate, LocalDate dueDate, AppUser lead) {
        this.name = name;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.lead = lead;
    }

    public ProjectMember addMember(AppUser user, int allocationPercent) {
        ProjectMember member = new ProjectMember(this, user, allocationPercent);
        members.add(member);
        return member;
    }

    public void removeMember(ProjectMember member) {
        members.remove(member);
    }
}
