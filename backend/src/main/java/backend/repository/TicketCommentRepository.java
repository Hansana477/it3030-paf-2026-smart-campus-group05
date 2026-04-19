package backend.repository;

import backend.model.TicketCommentModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCommentRepository extends JpaRepository<TicketCommentModel, Long> {
}