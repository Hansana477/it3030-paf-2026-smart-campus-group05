package backend.service;

import backend.model.BookingModel;
import backend.model.UserModel;
import jakarta.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final String mailFrom;
    private final String mailFromName;
    private final String mailHost;

    public EmailNotificationService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String mailFrom,
            @Value("${app.mail.from-name:Smart Campus Team}") String mailFromName,
            @Value("${spring.mail.host:}") String mailHost
    ) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.mailFromName = mailFromName;
        this.mailHost = mailHost;
    }

    public void sendRegistrationEmail(UserModel user) {
        if (user == null || !StringUtils.hasText(user.getEmail())) {
            return;
        }

        String subject = "Smart Campus registration successful";
        String approvalLine = user.isApproved()
                ? "Your account is active and ready to use."
                : "Your account is waiting for admin approval before full access is granted.";

        String body = String.format(
                "Hello %s,%n%n"
                        + "Your Smart Campus account has been created successfully.%n"
                        + "Role: %s%n"
                        + "%s%n%n"
                        + "You can now sign in using your registered email address.%n%n"
                        + "Smart Campus Team",
                safeName(user),
                user.getRole(),
                approvalLine
        );

        sendEmail(user.getEmail(), subject, body);
    }

    public void sendTechnicianApprovedEmail(UserModel user) {
        if (user == null || !StringUtils.hasText(user.getEmail())) {
            return;
        }

        String body = String.format(
                "Hello %s,%n%n"
                        + "Your technician account has been approved by the admin.%n"
                        + "You can now sign in and access the technician dashboard.%n%n"
                        + "Smart Campus Team",
                safeName(user)
        );

        sendEmail(user.getEmail(), "Smart Campus technician approval", body);
    }

    public void sendPasswordResetEmail(UserModel user, String resetCode) {
        if (user == null || !StringUtils.hasText(user.getEmail()) || !StringUtils.hasText(resetCode)) {
            return;
        }

        String body = String.format(
                "Hello %s,%n%n"
                        + "We received a password reset request for your Smart Campus account.%n"
                        + "Your reset code is: %s%n"
                        + "This code will expire in 15 minutes.%n%n"
                        + "If you did not request this, you can ignore this email.%n%n"
                        + "Smart Campus Team",
                safeName(user),
                resetCode
        );

        sendEmail(user.getEmail(), "Smart Campus password reset code", body);
    }

    public boolean sendLoginOtpEmail(UserModel user, String otpCode) {
        if (user == null || !StringUtils.hasText(user.getEmail()) || !StringUtils.hasText(otpCode)) {
            return false;
        }

        String body = String.format(
                "Hello %s,%n%n"
                        + "Use the following login verification code to complete your Smart Campus sign-in:%n"
                        + "%s%n%n"
                        + "This code will expire in 10 minutes.%n%n"
                        + "If you did not try to sign in, you can ignore this email.%n%n"
                        + "Smart Campus Team",
                safeName(user),
                otpCode
        );

        return sendEmail(user.getEmail(), "Smart Campus login verification code", body);
    }

    public boolean sendBookingApprovedEmail(BookingModel booking) {
        if (booking == null || !StringUtils.hasText(booking.getRequesterEmail())) {
            return false;
        }

        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=220x220&data="
                + urlEncode(booking.getQrPayload());

        String body = String.format(
                "<p>Hello %s,</p>"
                        + "<p>Your booking request has been approved.</p>"
                        + "<p><strong>Resource:</strong> %s<br/>"
                        + "<strong>Date:</strong> %s<br/>"
                        + "<strong>Time:</strong> %s - %s<br/>"
                        + "<strong>Seats:</strong> %s</p>"
                        + "<p><strong>Verification code:</strong> %s</p>"
                        + "<p>Please show this QR code when verifying your booking:</p>"
                        + "<p><img src=\"%s\" alt=\"Booking verification QR\" width=\"220\" height=\"220\" /></p>"
                        + "<p>Smart Campus Team</p>",
                escapeHtml(booking.getRequesterName()),
                escapeHtml(booking.getResourceName()),
                escapeHtml(booking.getDate()),
                escapeHtml(booking.getStartTime()),
                escapeHtml(booking.getEndTime()),
                escapeHtml(booking.getSeatNumbers() == null || booking.getSeatNumbers().isEmpty()
                        ? "Resource booking"
                        : String.join(", ", booking.getSeatNumbers())),
                escapeHtml(booking.getVerificationCode()),
                qrUrl
        );

        return sendEmail(booking.getRequesterEmail(), "Smart Campus booking approved", body, true);
    }

    private boolean sendEmail(String to, String subject, String body) {
        return sendEmail(to, subject, body, false);
    }

    private boolean sendEmail(String to, String subject, String body, boolean html) {
        if (!StringUtils.hasText(mailHost)) {
            logger.info("Skipping email to {} because SMTP host is not configured.", to);
            return false;
        }

        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(new InternetAddress(mailFrom, mailFromName));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, html);
            mailSender.send(message);
            logger.info("Sent email to {} with subject '{}'.", to, subject);
            return true;
        } catch (Exception ex) {
            logger.warn("Failed to send email to {}: {}", to, ex.getMessage());
            return false;
        }
    }

    private String safeName(UserModel user) {
        return StringUtils.hasText(user.getFullName()) ? user.getFullName() : "User";
    }

    private String urlEncode(String value) {
        return java.net.URLEncoder.encode(value == null ? "" : value, java.nio.charset.StandardCharsets.UTF_8);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
