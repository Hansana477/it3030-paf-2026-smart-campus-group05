package backend.repository;

import backend.model.TicketAttachmentModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketAttachmentRepository extends JpaRepository<TicketAttachmentModel, Long> {
}