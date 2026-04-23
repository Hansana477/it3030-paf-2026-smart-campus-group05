package backend.repository;

import backend.model.TicketAttachmentModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TicketAttachmentRepository extends MongoRepository<TicketAttachmentModel, Long> {
}