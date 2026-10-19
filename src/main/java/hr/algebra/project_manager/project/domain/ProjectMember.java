package hr.algebra.project_manager.project.domain;

import hr.algebra.project_manager.user.domain.AppUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

// Many-to-many Project <-> AppUser with an attribute (allocation), mapped as its own entity
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_member_seq")
    @SequenceGenerator(name = "project_member_seq", sequenceName = "project_member_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Setter
    @Column(nullable = false)
    private int allocationPercent;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant joinedAt;

    ProjectMember(Project project, AppUser user, int allocationPercent) {
        this.project = project;
        this.user = user;
        this.allocationPercent = allocationPercent;
    }
}
