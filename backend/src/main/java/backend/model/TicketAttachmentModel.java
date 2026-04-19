package backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "ticket_attachments")
public class TicketAttachmentModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, unique = true)
    private String storedFileName;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    @JsonBackReference
    private TicketModel ticket;

    public Long getId() { return id; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getStoredFileName() { return storedFileName; }
    public void setStoredFileName(String storedFileName) { this.storedFileName = storedFileName; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public TicketModel getTicket() { return ticket; }
    public void setTicket(TicketModel ticket) { this.ticket = ticket; }
}