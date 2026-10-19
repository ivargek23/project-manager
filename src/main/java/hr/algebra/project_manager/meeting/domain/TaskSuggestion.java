package hr.algebra.project_manager.meeting.domain;

import hr.algebra.project_manager.common.BaseEntity;
import hr.algebra.project_manager.task.domain.Task;
import hr.algebra.project_manager.user.domain.AppUser;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A task proposed by the model. It is not a Task: a Task is only created
 * when the project lead accepts the suggestion.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskSuggestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "task_suggestion_seq")
    @SequenceGenerator(name = "task_suggestion_seq", sequenceName = "task_suggestion_seq", allocationSize = 50)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id")
    @Setter(AccessLevel.NONE)
    private MeetingNote note;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 100)
    @Setter(AccessLevel.NONE)
    private String suggestedOwnerName;

    /** Null when no project member matches the returned name. The lead picks one before accepting. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_user_id")
    private AppUser matchedUser;

    private Integer estimateDays;

    @Column(length = 1000)
    @Setter(AccessLevel.NONE)
    private String sourceQuote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter(AccessLevel.NONE)
    private SuggestionStatus status = SuggestionStatus.PENDING;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_task_id")
    @Setter(AccessLevel.NONE)
    private Task createdTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by")
    @Setter(AccessLevel.NONE)
    private AppUser decidedBy;

    @Setter(AccessLevel.NONE)
    private Instant decidedAt;

    @Version
    @Setter(AccessLevel.NONE)
    private Long version;

    TaskSuggestion(MeetingNote note, String title, String suggestedOwnerName, AppUser matchedUser,
                   Integer estimateDays, String sourceQuote) {
        this.note = note;
        this.title = title;
        this.suggestedOwnerName = suggestedOwnerName;
        this.matchedUser = matchedUser;
        this.estimateDays = estimateDays;
        this.sourceQuote = sourceQuote;
    }

    public void accept(Task createdTask, AppUser decidedBy, Instant at) {
        requirePending();
        this.status = SuggestionStatus.ACCEPTED;
        this.createdTask = createdTask;
        this.decidedBy = decidedBy;
        this.decidedAt = at;
    }

    public void reject(AppUser decidedBy, Instant at) {
        requirePending();
        this.status = SuggestionStatus.REJECTED;
        this.decidedBy = decidedBy;
        this.decidedAt = at;
    }

    private void requirePending() {
        if (status != SuggestionStatus.PENDING) {
            throw new IllegalStateException("Suggestion " + id + " was already " + status);
        }
    }
}
