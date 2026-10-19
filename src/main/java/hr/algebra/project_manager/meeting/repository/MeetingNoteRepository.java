package hr.algebra.project_manager.meeting.repository;

import hr.algebra.project_manager.meeting.domain.MeetingNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingNoteRepository extends JpaRepository<MeetingNote, Long> {
}
