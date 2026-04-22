package backend.controller;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import backend.repository.TicketCommentRepository;
import backend.repository.TicketRepository;
import backend.repository.UserRepository;
import backend.dto.AssignTicketRequest;
import backend.dto.CreateTicketRequest;
import backend.dto.TicketCommentRequest;
import backend.dto.UpdateTicketStatusRequest;
import backend.exception.TicketBadRequestException;
import backend.exception.TicketForbiddenException;
import backend.exception.TicketNotFoundException;
import backend.model.TicketAttachmentModel;
import backend.model.TicketCommentModel;
import backend.model.TicketModel;
import backend.model.TicketStatus;
import backend.model.UserModel;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/tickets")
public class TicketController {
    private final TicketRepository ticketRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public TicketController(
            TicketRepository ticketRepository,
            TicketCommentRepository ticketCommentRepository,
            UserRepository userRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.ticketCommentRepository = ticketCommentRepository;
        this.userRepository = userRepository;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public TicketModel createTicket(
        @AuthenticationPrincipal UserModel currentUser,
        @RequestPart("ticket") CreateTicketRequest request,
        @RequestPart(value = "images", required = false) List<MultipartFile> images 
    ) {
        if(currentUser == null) {
            throw new TicketForbiddenException("Authentication required");
        }

        if (request.getLocation() == null || request.getLocation().isBlank()
                || request.getDescription() == null || request.getDescription().isBlank()
                || request.getCategory() == null
                || request.getPriority() == null
                || request.getPreferredContact() == null || request.getPreferredContact().isBlank()) {
            throw new TicketBadRequestException("All required ticket fields must be provided");
        }

        if (images != null && images.size() > 3) {
            throw new TicketBadRequestException("Maximum 3 image attachments are allowed");
        }

        TicketModel ticket = new TicketModel();
        ticket.setCreatedBy(currentUser);
        ticket.setResourceName(request.getResourceName());
        ticket.setLocation(request.getLocation());
        ticket.setCategory(request.getCategory());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setPreferredContact(request.getPreferredContact());
        ticket.setStatus(TicketStatus.OPEN);

        if (images != null) {
            for (MultipartFile file : images) {
                if (file.isEmpty()) {
                    continue;
                }

                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new TicketBadRequestException("Only image files are allowed");
                }

                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir, "tickets");
                Path filePath = uploadPath.resolve(fileName);

                try {
                    Files.createDirectories(uploadPath);
                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new TicketBadRequestException("Failed to save image: " + file.getOriginalFilename());
                }

                TicketAttachmentModel attachment = new TicketAttachmentModel();
                attachment.setOriginalFileName(file.getOriginalFilename());
                attachment.setStoredFileName(fileName);
                attachment.setFileUrl("/uploads/tickets/" + fileName);

                ticket.addAttachment(attachment);
            }
        }

        return ticketRepository.save(ticket);
    }

    @GetMapping
    public List<TicketModel> getAllTickets(@AuthenticationPrincipal UserModel currentUser) {
        if (currentUser == null) {
            throw new TicketForbiddenException("Authentication required");
        }

        if ("ADMIN".equals(currentUser.getRole())) {
            return ticketRepository.findAll();
        }

        if ("TECHNICIAN".equals(currentUser.getRole())) {
            return ticketRepository.findByAssignedTo(currentUser);
        }

        return ticketRepository.findByCreatedBy(currentUser);
    }

    @GetMapping("/{id}")
    public TicketModel getTicketById(@PathVariable Long id, @AuthenticationPrincipal UserModel currentUser) {
        TicketModel ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        if (!canViewTicket(currentUser, ticket)) {
            throw new TicketForbiddenException("You are not allowed to view this ticket");
        }

        return ticket;
    }

    @PatchMapping("/{id}/assign")
    public TicketModel assignTechnician(
            @PathVariable Long id,
            @RequestBody AssignTicketRequest request,
            @AuthenticationPrincipal UserModel currentUser
    ) {
        if (currentUser == null || (!"ADMIN".equals(currentUser.getRole()) && !"TECHNICIAN".equals(currentUser.getRole()))) {
            throw new TicketForbiddenException("Only admin or technician can assign tickets");
        }

        TicketModel ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        UserModel technician = userRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new TicketBadRequestException("Technician not found"));

        if (!"TECHNICIAN".equals(technician.getRole())) {
            throw new TicketBadRequestException("Assigned user must be a technician");
        }

        if (!technician.isApproved()) {
            throw new TicketBadRequestException("Technician must be approved first");
        }

        ticket.setAssignedTo(technician);
        return ticketRepository.save(ticket);
    }

    @PatchMapping("/{id}/status")
    public TicketModel updateTicketStatus(
            @PathVariable Long id,
            @RequestBody UpdateTicketStatusRequest request,
            @AuthenticationPrincipal UserModel currentUser
    ) {
        TicketModel ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        if (currentUser == null) {
            throw new TicketForbiddenException("Authentication required");
        }

        boolean isAdmin = "ADMIN".equals(currentUser.getRole());
        boolean isAssignedTechnician = ticket.getAssignedTo() != null
                && ticket.getAssignedTo().getId().equals(currentUser.getId());

        if (!isAdmin && !isAssignedTechnician) {
            throw new TicketForbiddenException("Only assigned technician or admin can update status");
        }

        TicketStatus newStatus = request.getStatus();
        if (newStatus == null) {
            throw new TicketBadRequestException("Status is required");
        }

        TicketStatus currentStatus = ticket.getStatus();

        if (newStatus == TicketStatus.REJECTED && !isAdmin) {
            throw new TicketForbiddenException("Only admin can reject a ticket");
        }

        boolean validTransition =
                (currentStatus == TicketStatus.OPEN && (newStatus == TicketStatus.IN_PROGRESS || newStatus == TicketStatus.REJECTED)) ||
                (currentStatus == TicketStatus.IN_PROGRESS && newStatus == TicketStatus.RESOLVED) ||
                (currentStatus == TicketStatus.RESOLVED && newStatus == TicketStatus.CLOSED) ||
                (currentStatus == newStatus);

        if (!validTransition) {
            throw new TicketBadRequestException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        if (newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.CLOSED) {
            if (request.getResolutionNotes() == null || request.getResolutionNotes().isBlank()) {
                throw new TicketBadRequestException("Resolution notes are required for RESOLVED or CLOSED");
            }
            ticket.setResolutionNotes(request.getResolutionNotes());
        }

        if (newStatus == TicketStatus.REJECTED) {
            if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
                throw new TicketBadRequestException("Rejection reason is required");
            }
            ticket.setRejectionReason(request.getRejectionReason());
        }

        ticket.setStatus(newStatus);
        return ticketRepository.save(ticket);
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketCommentModel addComment(
            @PathVariable Long id,
            @RequestBody TicketCommentRequest request,
            @AuthenticationPrincipal UserModel currentUser
    ) {
        TicketModel ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        if (!canViewTicket(currentUser, ticket)) {
            throw new TicketForbiddenException("You are not allowed to comment on this ticket");
        }

        if (request.getCommentText() == null || request.getCommentText().isBlank()) {
            throw new TicketBadRequestException("Comment text is required");
        }

        TicketCommentModel comment = new TicketCommentModel();
        comment.setTicket(ticket);
        comment.setAuthor(currentUser);
        comment.setCommentText(request.getCommentText());

        return ticketCommentRepository.save(comment);
    }

    @PutMapping("/{ticketId}/comments/{commentId}")
    public TicketCommentModel updateComment(
            @PathVariable Long ticketId,
            @PathVariable Long commentId,
            @RequestBody TicketCommentRequest request,
            @AuthenticationPrincipal UserModel currentUser
    ) {
        TicketModel ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        TicketCommentModel comment = ticketCommentRepository.findById(commentId)
                .orElseThrow(() -> new TicketBadRequestException("Comment not found"));

        if (!comment.getTicket().getId().equals(ticket.getId())) {
            throw new TicketBadRequestException("Comment does not belong to this ticket");
        }

        boolean isOwner = comment.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());

        if (!isOwner && !isAdmin) {
            throw new TicketForbiddenException("Only comment owner or admin can edit comment");
        }

        if (request.getCommentText() == null || request.getCommentText().isBlank()) {
            throw new TicketBadRequestException("Comment text is required");
        }

        comment.setCommentText(request.getCommentText());
        return ticketCommentRepository.save(comment);
    }

    @DeleteMapping("/{ticketId}/comments/{commentId}")
    public Map<String, String> deleteComment(
            @PathVariable Long ticketId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserModel currentUser
    ) {
        TicketModel ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        TicketCommentModel comment = ticketCommentRepository.findById(commentId)
                .orElseThrow(() -> new TicketBadRequestException("Comment not found"));

        if (!comment.getTicket().getId().equals(ticket.getId())) {
            throw new TicketBadRequestException("Comment does not belong to this ticket");
        }

        boolean isOwner = comment.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());

        if (!isOwner && !isAdmin) {
            throw new TicketForbiddenException("Only comment owner or admin can delete comment");
        }

        ticketCommentRepository.delete(comment);
        return Map.of("message", "Comment deleted successfully");
    }

    private boolean canViewTicket(UserModel currentUser, TicketModel ticket) {
        if (currentUser == null) return false;

        if ("ADMIN".equals(currentUser.getRole())) return true;

        if (ticket.getCreatedBy() != null && ticket.getCreatedBy().getId().equals(currentUser.getId())) {
            return true;
        }

        return ticket.getAssignedTo() != null
                && ticket.getAssignedTo().getId().equals(currentUser.getId());
    }
}
