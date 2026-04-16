package backend.repository;

import backend.model.TicketModel;
import backend.model.TicketStatus;
import backend.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<TicketModel, Long> {
    List<TicketModel> findByCreatedBy(UserModel createdBy);
    List<TicketModel> findByAssignedTo(UserModel assignedTo);
    List<TicketModel> findByStatus(TicketStatus status);
}