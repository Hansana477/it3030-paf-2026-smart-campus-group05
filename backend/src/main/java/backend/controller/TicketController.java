package backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

import backend.dto.AssignTicketRequest;
import backend.dto.CreateTicketRequest;
import backend.dto.ReopenTicketRequest;
import backend.dto.TicketCommentRequest;
import backend.dto.UpdateTicketStatusRequest;
import backend.exception.TicketBadRequestException;
import backend.exception.TicketForbiddenException;
import backend.exception.TicketNotFoundException;
import backend.model.*;
import backend.repository.ResourceRepository;
import backend.repository.TicketRepository;
import backend.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@RequestMapping("/tickets")
public class TicketController {

    private static final Path TICKET_IMAGE_UPLOAD_DIR = Path.of("uploads", "ticket-images");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_GIF_VALUE,
            "image/webp"
    );

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    public TicketController(
            TicketRepository ticketRepository,
            UserRepository userRepository,
            ResourceRepository resourceRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public TicketModel createTicket(
            @RequestPart("ticket") CreateTicketRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserModel currentUser,
            HttpServletRequest httpRequest
    ) throws IOException {
        requireAuthentication(currentUser);

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

        if (request.getResourceId() != null && !request.getResourceId().isBlank()
                && !resourceRepository.existsById(request.getResourceId())) {
            throw new TicketBadRequestException("Selected resource does not exist");
        }

        boolean duplicateExists = ticketRepository.findAll().stream().anyMatch(ticket ->
                Objects.equals(safe(ticket.getResourceId()), safe(request.getResourceId()))
                        && Objects.equals(safe(ticket.getLocation()), safe(request.getLocation()))
                        && ticket.getCategory() == request.getCategory()
                        && (ticket.getStatus() == TicketStatus.OPEN
                        || ticket.getStatus() == TicketStatus.IN_PROGRESS
                        || ticket.getStatus() == TicketStatus.REOPENED)
        );

        if (duplicateExists) {
            throw new TicketBadRequestException("A similar active ticket already exists for this resource/location");
        }

        TicketModel ticket = new TicketModel();
        ticket.setCreatedByUserId(currentUser.getId());
        ticket.setCreatedByUserName(currentUser.getFullName());
        ticket.setResourceId(blankToNull(request.getResourceId()));
        ticket.setResourceName(blankToNull(request.getResourceName()));
        ticket.setLocation(request.getLocation().trim());
        ticket.setCategory(request.getCategory());
        ticket.setPriority(request.getPriority());
        ticket.setDescription(request.getDescription().trim());
        ticket.setPreferredContact(request.getPreferredContact().trim());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setDueDate(calculateDueDate(request.getPriority()));
        ticket.setOverdue(false);

        if (images != null) {
            Files.createDirectories(TICKET_IMAGE_UPLOAD_DIR);

            for (MultipartFile image : images) {
                if (image == null || image.isEmpty()) {
                    continue;
                }

                String contentType = image.getContentType();
                if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
                    throw new TicketBadRequestException("Only JPG, PNG, GIF, and WEBP images are allowed");
                }

                String extension = getExtension(image.getOriginalFilename(), contentType);
                String storedFileName = UUID.randomUUID() + extension;
                Path target = TICKET_IMAGE_UPLOAD_DIR.resolve(storedFileName).normalize();
                Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

                TicketAttachmentModel attachment = new TicketAttachmentModel();
                attachment.setId(UUID.randomUUID().toString());
                attachment.setOriginalFileName(image.getOriginalFilename());
                attachment.setStoredFileName(storedFileName);
                attachment.setContentType(contentType);
                attachment.setFileSize(image.getSize());

                String fileUrl = httpRequest.getScheme() + "://" + httpRequest.getServerName() + ":" + httpRequest.getServerPort()
                        + "/uploads/ticket-images/" + storedFileName;
                attachment.setFileUrl(fileUrl);

                ticket.getAttachments().add(attachment);
            }
        }

        ticket.applyDefaults();
        return ticketRepository.save(ticket);
    }

    @GetMapping
    public List<TicketModel> getTickets(@AuthenticationPrincipal UserModel currentUser) {
        requireAuthentication(currentUser);

        List<TicketModel> tickets;
        if ("ADMIN".equals(currentUser.getRole())) {
            tickets = ticketRepository.findAll();
        } else if ("TECHNICIAN".equals(currentUser.getRole())) {
            tickets = ticketRepository.findByAssignedToUserId(currentUser.getId());
        } else {
            tickets = ticketRepository.findByCreatedByUserId(currentUser.getId());
        }

        tickets.forEach(this::refreshOverdueStatusIfNeeded);
        return tickets;
    }

    @GetMapping("/{id}")
    public TicketModel getTicketById(@PathVariable String id, @AuthenticationPrincipal UserModel currentUser) {
        requireAuthentication(currentUser);

        TicketModel ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        if (!canViewTicket(currentUser, ticket)) {
            throw new TicketForbiddenException("You do not have permission to view this ticket");
        }

        refreshOverdueStatusIfNeeded(ticket);
        return ticket;
    }

    @PatchMapping("/{id}/assign")
    public TicketModel assignTicket(
            @PathVariable String id,
            @RequestBody AssignTicketRequest request,
            @AuthenticationPrincipal UserModel currentUser
    ) {
        requireAuthentication(currentUser);

        if (!"ADMIN".equals(currentUser.getRole()) && !"TECHNICIAN".equals(currentUser.getRole())) {
            throw new TicketForbiddenException("Only admin or technician can assign tickets");
        }

        if (request.getTechnicianId() == null || request.getTechnicianId().isBlank()) {
            throw new TicketBadRequestException("Technician id is required");
        }

        TicketModel ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        UserModel technician = userRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new TicketBadRequestException("Technician not found"));

        if (!"TECHNICIAN".equals(technician.getRole())) {
            throw new TicketBadRequestException("Assigned user must be a technician");
        }

        if (!technician.isApproved()) {
            throw new TicketBadRequestException("Technician account is not approved yet");
        }

        ticket.setAssignedToUserId(technician.getId());
        ticket.setAssignedToUserName(technician.getFullName());
        ticket.touch();
        return ticketRepository.save(ticket);
    }

    @PatchMapping("/{id}/status")
    public TicketModel updateTicketStatus(
            @PathVariable String id,
            @RequestBody UpdateTicketStatusRequest request,
            @AuthenticationPrincipal UserModel currentUser
    ) {
        requireAuthentication(currentUser);

        TicketModel ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        boolean isAdmin = "ADMIN".equals(currentUser.getRole());
        boolean isAssignedTechnician = ticket.getAssignedToUserId() != null
                && ticket.getAssignedToUserId().equals(currentUser.getId());

        if (!isAdmin && !isAssignedTechnician) {
            throw new TicketForbiddenException("Only assigned technician or admin can update status");
        }

        if (request.getStatus() == null) {
            throw new TicketBadRequestException("Status is required");
        }

        TicketStatus currentStatus = ticket.getStatus();
        TicketStatus newStatus = request.getStatus();

        boolean validTransition =
                (currentStatus == TicketStatus.OPEN && (newStatus == TicketStatus.IN_PROGRESS || newStatus == TicketStatus.REJECTED)) ||
                (currentStatus == TicketStatus.REOPENED && newStatus == TicketStatus.IN_PROGRESS) ||
                (currentStatus == TicketStatus.IN_PROGRESS && newStatus == TicketStatus.RESOLVED) ||
                (currentStatus == TicketStatus.RESOLVED && newStatus == TicketStatus.CLOSED) ||
                currentStatus == newStatus;

        if (!validTransition) {
            throw new TicketBadRequestException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        if (newStatus == TicketStatus.REJECTED) {
            if (!isAdmin) {
                throw new TicketForbiddenException("Only admin can reject tickets");
            }
            if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
                throw new TicketBadRequestException("Rejection reason is required");
            }
            ticket.setRejectionReason(request.getRejectionReason().trim());
        }

        if (newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.CLOSED) {
            if (request.getResolutionNotes() == null || request.getResolutionNotes().isBlank()) {
                throw new TicketBadRequestException("Resolution notes are required");
            }
            ticket.setResolutionNotes(request.getResolutionNotes().trim());
        }

        ticket.setStatus(newStatus);
        ticket.touch();
        refreshOverdueStatusIfNeeded(ticket);
        return ticketRepository.save(ticket);
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketModel addComment(
            @PathVariable String id,
            @RequestBody TicketCommentRequest request,
            @AuthenticationPrincipal UserModel currentUser
    ) {
        requireAuthentication(currentUser);

        TicketModel ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        if (!canViewTicket(currentUser, ticket)) {
            throw new TicketForbiddenException("You do not have permission to comment on this ticket");
        }

        if (request.getCommentText() == null || request.getCommentText().isBlank()) {
            throw new TicketBadRequestException("Comment text is required");
        }

        TicketCommentModel comment = new TicketCommentModel();
        comment.setId(UUID.randomUUID().toString());
        comment.setAuthorId(currentUser.getId());
        comment.setAuthorName(currentUser.getFullName());
        comment.setCommentText(request.getCommentText().trim());

        ticket.getComments().add(comment);
        ticket.touch();
        return ticketRepository.save(ticket);
    }

    @PutMapping("/{ticketId}/comments/{commentId}")
    public TicketModel updateComment(
            @PathVariable String ticketId,
            @PathVariable String commentId,
            @RequestBody TicketCommentRequest request,
            @AuthenticationPrincipal UserModel currentUser
    ) {
        requireAuthentication(currentUser);

        TicketModel ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        TicketCommentModel comment = ticket.getComments().stream()
                .filter(existing -> commentId.equals(existing.getId()))
                .findFirst()
                .orElseThrow(() -> new TicketBadRequestException("Comment not found"));

        boolean isOwner = currentUser.getId().equals(comment.getAuthorId());
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());

        if (!isOwner && !isAdmin) {
            throw new TicketForbiddenException("Only comment owner or admin can edit comment");
        }

        if (request.getCommentText() == null || request.getCommentText().isBlank()) {
            throw new TicketBadRequestException("Comment text is required");
        }

        comment.setCommentText(request.getCommentText().trim());
        ticket.touch();
        return ticketRepository.save(ticket);
    }

    @DeleteMapping("/{ticketId}/comments/{commentId}")
    public Map<String, String> deleteComment(
            @PathVariable String ticketId,
            @PathVariable String commentId,
            @AuthenticationPrincipal UserModel currentUser
    ) {
        requireAuthentication(currentUser);

        TicketModel ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        TicketCommentModel comment = ticket.getComments().stream()
                .filter(existing -> commentId.equals(existing.getId()))
                .findFirst()
                .orElseThrow(() -> new TicketBadRequestException("Comment not found"));

        boolean isOwner = currentUser.getId().equals(comment.getAuthorId());
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());

        if (!isOwner && !isAdmin) {
            throw new TicketForbiddenException("Only comment owner or admin can delete comment");
        }

        ticket.getComments().removeIf(existing -> commentId.equals(existing.getId()));
        ticket.touch();
        ticketRepository.save(ticket);

        return Map.of("message", "Comment deleted successfully");
    }

    @PatchMapping("/{id}/reopen")
    public TicketModel reopenTicket(
            @PathVariable String id,
            @RequestBody ReopenTicketRequest request,
            @AuthenticationPrincipal UserModel currentUser
    ) {
        requireAuthentication(currentUser);

        TicketModel ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        boolean isOwner = ticket.getCreatedByUserId() != null && ticket.getCreatedByUserId().equals(currentUser.getId());
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());

        if (!isOwner && !isAdmin) {
            throw new TicketForbiddenException("Only the ticket owner or admin can reopen a ticket");
        }

        if (ticket.getStatus() != TicketStatus.RESOLVED && ticket.getStatus() != TicketStatus.CLOSED) {
            throw new TicketBadRequestException("Only RESOLVED or CLOSED tickets can be reopened");
        }

        if (request.getReopenReason() == null || request.getReopenReason().isBlank()) {
            throw new TicketBadRequestException("Reopen reason is required");
        }

        ticket.setStatus(TicketStatus.REOPENED);
        ticket.setReopenReason(request.getReopenReason().trim());
        ticket.touch();
        refreshOverdueStatusIfNeeded(ticket);

        return ticketRepository.save(ticket);
    }

    private void requireAuthentication(UserModel currentUser) {
        if (currentUser == null) {
            throw new TicketForbiddenException("Authentication required");
        }
    }

    private boolean canViewTicket(UserModel currentUser, TicketModel ticket) {
        if (currentUser == null) {
            return false;
        }

        if ("ADMIN".equals(currentUser.getRole())) {
            return true;
        }

        if (ticket.getCreatedByUserId() != null && ticket.getCreatedByUserId().equals(currentUser.getId())) {
            return true;
        }

        return ticket.getAssignedToUserId() != null && ticket.getAssignedToUserId().equals(currentUser.getId());
    }

    private void refreshOverdueStatusIfNeeded(TicketModel ticket) {
        boolean shouldBeOverdue = ticket.getDueDate() != null
                && LocalDateTime.now().isAfter(ticket.getDueDate())
                && ticket.getStatus() != TicketStatus.RESOLVED
                && ticket.getStatus() != TicketStatus.CLOSED
                && ticket.getStatus() != TicketStatus.REJECTED;

        if (ticket.isOverdue() != shouldBeOverdue) {
            ticket.setOverdue(shouldBeOverdue);
            ticket.touch();
            ticketRepository.save(ticket);
        }
    }

    private LocalDateTime calculateDueDate(TicketPriority priority) {
        LocalDateTime now = LocalDateTime.now();
        return switch (priority) {
            case LOW -> now.plusDays(5);
            case MEDIUM -> now.plusDays(3);
            case HIGH -> now.plusDays(2);
            case CRITICAL -> now.plusDays(1);
        };
    }

    private String getExtension(String originalFilename, String contentType) {
        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < originalFilename.length() - 1) {
                String extension = originalFilename.substring(dotIndex).toLowerCase();
                if (extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
                    return extension;
                }
            }
        }

        return switch (contentType) {
            case MediaType.IMAGE_PNG_VALUE -> ".png";
            case MediaType.IMAGE_GIF_VALUE -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
