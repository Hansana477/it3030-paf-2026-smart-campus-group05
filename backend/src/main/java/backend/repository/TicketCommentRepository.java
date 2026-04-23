package backend.repository;

import backend.model.TicketCommentModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TicketCommentRepository extends MongoRepository<TicketCommentModel, Long> {
}