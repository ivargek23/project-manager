package hr.algebra.project_manager.analytics.domain;

import hr.algebra.project_manager.project.domain.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// One row per project is written daily and burndown chart reads these records.
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BurndownSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "burndown_snapshot_seq")
    @SequenceGenerator(name = "burndown_snapshot_seq", sequenceName = "burndown_snapshot_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false)
    private LocalDate snapshotDate;

    @Column(nullable = false)
    private int remainingDays;

    @Column(nullable = false)
    private int completedDays;

    public BurndownSnapshot(Project project, LocalDate snapshotDate, int remainingDays, int completedDays) {
        this.project = project;
        this.snapshotDate = snapshotDate;
        this.remainingDays = remainingDays;
        this.completedDays = completedDays;
    }
}
