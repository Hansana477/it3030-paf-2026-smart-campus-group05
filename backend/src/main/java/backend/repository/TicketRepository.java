package backend.repository;

import backend.model.TicketModel;
import backend.model.TicketStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends MongoRepository<TicketModel, String> {
    List<TicketModel> findByCreatedByUserId(String createdByUserId);
    List<TicketModel> findByAssignedToUserId(String assignedToUserId);
    List<TicketModel> findByStatus(TicketStatus status);
}