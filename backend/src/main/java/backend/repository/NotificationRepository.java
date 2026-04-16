package backend.repository;

import backend.model.NotificationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationModel, Long> {

    List<NotificationModel> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    Optional<NotificationModel> findByIdAndRecipientId(Long id, Long recipientId);

    @Modifying
    @Transactional
    @Query("UPDATE NotificationModel n SET n.read = true WHERE n.recipient.id = :recipientId AND n.read = false")
    int markAllAsRead(@Param("recipientId") Long recipientId);
}
