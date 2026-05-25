package com.bloodlink.service;

import com.bloodlink.entity.BloodRequest;
import com.bloodlink.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * EmailService - Sends transactional emails to users
 * 
 * Features:
 * - Password reset notifications
 * - Blood request alerts
 * - Donation confirmations
 * - Welcome emails
 * - HTML email templates via Thymeleaf
 * 
 * OOP: Encapsulation - Email logic isolated in separate service
 * SRP: Single responsibility - Only handles email operations
 * DIP: Depends on JavaMailSender abstraction
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from:noreply@bloodlink.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:3000}")
    private String baseUrl;

    /**
     * Send welcome email to new donor
     */
    public void sendWelcomeDonorEmail(User donor) {
        log.info("Sending welcome email to donor: {}", donor.getEmail());

        try {
            Context context = new Context();
            context.setVariable("firstName", donor.getFirstName());
            context.setVariable("baseUrl", baseUrl);
            context.setVariable("year", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("emails/welcome-donor", context);

            sendHtmlEmail(
                donor.getEmail(),
                "Welcome to BloodLink - Save Lives Today!",
                htmlContent
            );

            log.info("Welcome email sent successfully to: {}", donor.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", donor.getEmail(), e);
        }
    }

    /**
     * Send welcome email to new patient
     */
    public void sendWelcomePatientEmail(User patient) {
        log.info("Sending welcome email to patient: {}", patient.getEmail());

        try {
            Context context = new Context();
            context.setVariable("firstName", patient.getFirstName());
            context.setVariable("baseUrl", baseUrl);
            context.setVariable("year", LocalDateTime.now().getYear());

            String htmlContent = templateEngine.process("emails/welcome-patient", context);

            sendHtmlEmail(
                patient.getEmail(),
                "Welcome to BloodLink - Get the Blood You Need",
                htmlContent
            );

            log.info("Welcome email sent successfully to: {}", patient.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", patient.getEmail(), e);
        }
    }

    /**
     * Send password reset email with reset link
     */
    public void sendPasswordResetEmail(User user, String resetToken) {
        log.info("Sending password reset email to: {}", user.getEmail());

        try {
            String resetLink = baseUrl + "/reset-password?token=" + resetToken;

            Context context = new Context();
            context.setVariable("firstName", user.getFirstName());
            context.setVariable("resetLink", resetLink);
            context.setVariable("expiryMinutes", 30);
            context.setVariable("baseUrl", baseUrl);

            String htmlContent = templateEngine.process("emails/password-reset", context);

            sendHtmlEmail(
                user.getEmail(),
                "Reset Your BloodLink Password",
                htmlContent
            );

            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
        }
    }

    /**
     * Send blood request alert to matching donors
     */
    public void sendBloodRequestAlert(BloodRequest request, String donorEmail, String donorName) {
        log.info("Sending blood request alert to donor: {}", donorEmail);

        try {
            Context context = new Context();
            context.setVariable("donorName", donorName);
            context.setVariable("patientName", request.getPatient().getFirstName() + " " + request.getPatient().getLastName());
            context.setVariable("bloodGroup", request.getBloodGroup());
            context.setVariable("emergencyLevel", request.getEmergencyLevel());
            context.setVariable("unitsNeeded", request.getUnitsNeeded());
            context.setVariable("requestLink", baseUrl + "/blood-requests/" + request.getId());
            context.setVariable("baseUrl", baseUrl);

            String htmlContent = templateEngine.process("emails/blood-request-alert", context);

            sendHtmlEmail(
                donorEmail,
                "URGENT: Blood Request Matching Your Type!",
                htmlContent
            );

            log.info("Blood request alert sent to: {}", donorEmail);
        } catch (Exception e) {
            log.error("Failed to send blood request alert to: {}", donorEmail, e);
        }
    }

    /**
     * Send request acceptance confirmation to patient
     */
    public void sendRequestAcceptedEmail(BloodRequest request, User donor) {
        log.info("Sending request accepted email to patient: {}", request.getPatient().getEmail());

        try {
            Context context = new Context();
            context.setVariable("patientName", request.getPatient().getFirstName());
            context.setVariable("donorName", donor.getFirstName() + " " + donor.getLastName());
            context.setVariable("bloodGroup", request.getBloodGroup());
            context.setVariable("unitsNeeded", request.getUnitsNeeded());
            context.setVariable("donorRating", String.format("%.1f", donor.getUserRating()));
            context.setVariable("baseUrl", baseUrl);

            String htmlContent = templateEngine.process("emails/request-accepted", context);

            sendHtmlEmail(
                request.getPatient().getEmail(),
                "Great News! Your Blood Request Has Been Accepted",
                htmlContent
            );

            log.info("Request accepted email sent to: {}", request.getPatient().getEmail());
        } catch (Exception e) {
            log.error("Failed to send request accepted email", e);
        }
    }

    /**
     * Send donation confirmation email
     */
    public void sendDonationConfirmationEmail(User donor, String unitsCollected) {
        log.info("Sending donation confirmation to: {}", donor.getEmail());

        try {
            Context context = new Context();
            context.setVariable("donorName", donor.getFirstName());
            context.setVariable("unitsCollected", unitsCollected);
            context.setVariable("nextEligibleDate", LocalDateTime.now().plusDays(56).format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            context.setVariable("baseUrl", baseUrl);

            String htmlContent = templateEngine.process("emails/donation-confirmation", context);

            sendHtmlEmail(
                donor.getEmail(),
                "Thank You for Your Donation!",
                htmlContent
            );

            log.info("Donation confirmation sent to: {}", donor.getEmail());
        } catch (Exception e) {
            log.error("Failed to send donation confirmation to: {}", donor.getEmail(), e);
        }
    }

    /**
     * Send donor availability status change notification
     */
    public void sendAvailabilityChangeEmail(User donor, boolean isAvailable) {
        log.info("Sending availability change notification to: {}", donor.getEmail());

        try {
            String status = isAvailable ? "AVAILABLE" : "UNAVAILABLE";
            String template = isAvailable ? "emails/available-now" : "emails/unavailable-now";

            Context context = new Context();
            context.setVariable("donorName", donor.getFirstName());
            context.setVariable("status", status);
            context.setVariable("baseUrl", baseUrl);

            String htmlContent = templateEngine.process(template, context);

            sendHtmlEmail(
                donor.getEmail(),
                "Your Availability Status Has Been Updated",
                htmlContent
            );

            log.info("Availability change email sent to: {}", donor.getEmail());
        } catch (Exception e) {
            log.error("Failed to send availability change email to: {}", donor.getEmail(), e);
        }
    }

    /**
     * Send critical blood request notification
     */
    public void sendCriticalBloodRequestEmail(BloodRequest request, String recipientEmail) {
        log.info("Sending critical blood request to: {}", recipientEmail);

        try {
            Context context = new Context();
            context.setVariable("emergencyLevel", request.getEmergencyLevel());
            context.setVariable("bloodGroup", request.getBloodGroup());
            context.setVariable("unitsNeeded", request.getUnitsNeeded());
            context.setVariable("medicalReason", request.getMedicalReason());
            context.setVariable("urgentLink", baseUrl + "/blood-requests/" + request.getId());
            context.setVariable("baseUrl", baseUrl);

            String htmlContent = templateEngine.process("emails/critical-request", context);

            sendHtmlEmail(
                recipientEmail,
                "CRITICAL: Urgent Blood Request - " + request.getBloodGroup(),
                htmlContent
            );

            log.info("Critical blood request email sent to: {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send critical blood request email", e);
        }
    }

    /**
     * Send request expired notification to patient
     */
    public void sendRequestExpiredEmail(BloodRequest request) {
        log.info("Sending request expired notification to: {}", request.getPatient().getEmail());

        try {
            Context context = new Context();
            context.setVariable("patientName", request.getPatient().getFirstName());
            context.setVariable("bloodGroup", request.getBloodGroup());
            context.setVariable("unitsNeeded", request.getUnitsNeeded());
            context.setVariable("requestLink", baseUrl + "/blood-requests/" + request.getId());
            context.setVariable("baseUrl", baseUrl);

            String htmlContent = templateEngine.process("emails/request-expired", context);

            sendHtmlEmail(
                request.getPatient().getEmail(),
                "Blood Request Expired - Action Required",
                htmlContent
            );

            log.info("Request expired email sent to: {}", request.getPatient().getEmail());
        } catch (Exception e) {
            log.error("Failed to send request expired email", e);
        }
    }

    /**
     * Internal method to send HTML email
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        // Add email headers
        message.setHeader("X-Priority", "3");
        message.setHeader("X-Mailer", "BloodLink Application");

        mailSender.send(message);
    }

    /**
     * Internal method to send plain text email
     */
    private void sendPlainEmail(String to, String subject, String plainContent) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(plainContent);

        mailSender.send(message);
        log.debug("Plain text email sent to: {}", to);
    }

    /**
     * Send system notification email to admin
     */
    public void sendAdminNotification(String subject, String message) {
        log.info("Sending admin notification: {}", subject);

        try {
            Context context = new Context();
            context.setVariable("subject", subject);
            context.setVariable("message", message);
            context.setVariable("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            String htmlContent = templateEngine.process("emails/admin-notification", context);

            sendHtmlEmail(
                "admin@bloodlink.com",
                "[BloodLink Admin] " + subject,
                htmlContent
            );
        } catch (Exception e) {
            log.error("Failed to send admin notification", e);
        }
    }
}
