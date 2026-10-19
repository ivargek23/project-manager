package hr.algebra.project_manager.meeting.domain;

import hr.algebra.project_manager.common.BaseEntity;
import hr.algebra.project_manager.project.domain.Project;
import hr.algebra.project_manager.user.domain.AppUser;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingNote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "meeting_note_seq")
    @SequenceGenerator(name = "meeting_note_seq", sequenceName = "meeting_note_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by")
    private AppUser createdBy;

    @Column(nullable = false, columnDefinition = "text")
    private String rawText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoteStatus status = NoteStatus.PENDING;

    @Column(length = 500)
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant processedAt;

    @Version
    private Long version;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<TaskSuggestion> suggestions = new ArrayList<>();

    public MeetingNote(Project project, AppUser createdBy, String rawText) {
        this.project = project;
        this.createdBy = createdBy;
        this.rawText = rawText;
    }

    public void markProcessing() {
        this.status = NoteStatus.PROCESSING;
    }

    public void markDone(Instant at) {
        this.status = NoteStatus.DONE;
        this.processedAt = at;
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage, Instant at) {
        this.status = NoteStatus.FAILED;
        this.processedAt = at;
        this.errorMessage = errorMessage;
    }

    public TaskSuggestion addSuggestion(String title, String suggestedOwnerName, AppUser matchedUser,
                                        Integer estimateDays, String sourceQuote) {
        TaskSuggestion suggestion =
                new TaskSuggestion(this, title, suggestedOwnerName, matchedUser, estimateDays, sourceQuote);
        suggestions.add(suggestion);
        return suggestion;
    }
}
