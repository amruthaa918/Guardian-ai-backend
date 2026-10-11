package com.guardianai.service;

import com.guardianai.dto.ContactDeliveryStatus;
import com.guardianai.dto.NotificationResult;
import com.guardianai.entity.EmergencyAlert;
import com.guardianai.entity.TrustedContact;
import com.guardianai.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Value("${twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${twilio.phone-number:}")
    private String twilioPhoneNumber;

    @Value("${sendgrid.api-key:}")
    private String sendgridApiKey;

    @Value("${sendgrid.from-email:}")
    private String sendgridFromEmail;

    private final RestTemplate restTemplate;
    private final java.util.Set<Long> notifiedAlertIds = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public NotificationService() {
        this.restTemplate = new RestTemplate();
    }

    public NotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean isTwilioConfigured() {
        return isNonEmpty(twilioAccountSid) &&
               isNonEmpty(twilioAuthToken) &&
               isNonEmpty(twilioPhoneNumber);
    }

    public boolean isSendgridConfigured() {
        return isNonEmpty(sendgridApiKey) &&
               isNonEmpty(sendgridFromEmail);
    }

    private boolean isNonEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public NotificationResult notifyTrustedContacts(User user, EmergencyAlert alert, List<TrustedContact> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            return new NotificationResult("No trusted contacts configured.", Collections.emptyList());
        }

        // Deduplication safeguard: Ensure notifications are only sent once per alert ID
        if (alert != null && alert.getId() != null) {
            if (!notifiedAlertIds.add(alert.getId())) {
                log.warn("Notifications already processed for alert ID: {}. Skipping duplicate dispatch.", alert.getId());
                return new NotificationResult("Alert notifications already processed. Duplicate dispatch prevented.", Collections.emptyList());
            }
        }

        String locationUrl;
        if (alert != null && alert.getLatitude() != null && alert.getLongitude() != null) {
            locationUrl = "Location: https://maps.google.com/?q="
                    + alert.getLatitude() + "," + alert.getLongitude();
        } else {
            locationUrl = "Location unavailable.";
        }

        String senderName = (user != null && user.getName() != null && !user.getName().isBlank())
                ? user.getName()
                : "A user";
        String messageBody = "EMERGENCY SOS! " + senderName + " needs help. " + locationUrl;

        boolean twilioConfigured = isTwilioConfigured();
        boolean sendgridConfigured = isSendgridConfigured();

        int smsSuccess = 0;
        int smsFailed = 0;
        int emailSuccess = 0;
        int emailFailed = 0;

        List<ContactDeliveryStatus> deliveryStatuses = new ArrayList<>();

        for (TrustedContact contact : contacts) {
            ContactDeliveryStatus status = new ContactDeliveryStatus();
            status.setContactId(contact.getId());
            status.setContactName(contact.getName() != null ? contact.getName() : "Unknown");
            status.setPhone(contact.getPhone());
            status.setEmail(contact.getEmail());

            // Handle SMS delivery
            if (contact.getPhone() == null || contact.getPhone().isBlank()) {
                status.setSmsStatus("NO_PHONE");
                status.setSmsDetails("No phone number configured for contact");
            } else if (!twilioConfigured) {
                status.setSmsStatus("NOT_CONFIGURED");
                status.setSmsDetails("Twilio credentials (TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_PHONE_NUMBER) not configured in backend .env");
            } else {
                SmsResult smsResult = sendTwilioSms(contact.getPhone(), messageBody);
                if (smsResult.success()) {
                    status.setSmsStatus("SENT");
                    status.setSmsDetails(smsResult.message());
                    smsSuccess++;
                } else {
                    status.setSmsStatus("FAILED");
                    status.setSmsDetails(smsResult.message());
                    smsFailed++;
                }
            }

            // Handle Email delivery
            if (contact.getEmail() == null || contact.getEmail().isBlank()) {
                status.setEmailStatus("NO_EMAIL");
                status.setEmailDetails("No email address configured for contact");
            } else if (!sendgridConfigured) {
                status.setEmailStatus("NOT_CONFIGURED");
                status.setEmailDetails("SendGrid credentials (SENDGRID_API_KEY, SENDGRID_FROM_EMAIL) not configured in backend .env");
            } else {
                EmailResult emailResult = sendSendGridEmail(contact.getEmail(), senderName + " Emergency SOS Alert", messageBody);
                if (emailResult.success()) {
                    status.setEmailStatus("ACCEPTED");
                    status.setEmailDetails(emailResult.message());
                    emailSuccess++;
                } else {
                    status.setEmailStatus("FAILED");
                    status.setEmailDetails(emailResult.message());
                    emailFailed++;
                }
            }

            deliveryStatuses.add(status);
        }

        StringBuilder summary = new StringBuilder();
        if (twilioConfigured) {
            summary.append("SMS: ").append(smsSuccess).append(" accepted, ").append(smsFailed).append(" failed. ");
        } else {
            summary.append("SMS: provider not configured. ");
        }

        if (sendgridConfigured) {
            summary.append("Email: ").append(emailSuccess).append(" accepted, ").append(emailFailed).append(" failed.");
        } else {
            summary.append("Email: provider not configured.");
        }

        return new NotificationResult(summary.toString().trim(), deliveryStatuses);
    }

    private SmsResult sendTwilioSms(String toPhone, String body) {
        try {
            String url = "https://api.twilio.com/2010-04-01/Accounts/" + twilioAccountSid.trim() + "/Messages.json";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(twilioAccountSid.trim(), twilioAuthToken.trim());

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("To", toPhone.trim());
            map.add("From", twilioPhoneNumber.trim());
            map.add("Body", body);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return new SmsResult(true, "SMS accepted by Twilio (HTTP " + response.getStatusCode().value() + ")");
            } else {
                return new SmsResult(false, "Twilio rejected message with HTTP " + response.getStatusCode().value());
            }
        } catch (HttpStatusCodeException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("Twilio HTTP error {} sending to {}: {}", e.getStatusCode(), toPhone, responseBody);
            String errorMsg = extractTwilioErrorMessage(responseBody, e.getMessage());
            return new SmsResult(false, "Twilio error (HTTP " + e.getStatusCode().value() + "): " + errorMsg);
        } catch (Exception e) {
            log.error("Failed to send Twilio SMS to {}: {}", toPhone, e.getMessage());
            return new SmsResult(false, "Failed to send SMS: " + e.getMessage());
        }
    }

    private String extractTwilioErrorMessage(String responseBody, String fallback) {
        if (responseBody != null && !responseBody.isBlank()) {
            int msgIndex = responseBody.indexOf("\"message\":");
            if (msgIndex != -1) {
                int start = responseBody.indexOf("\"", msgIndex + 10);
                if (start != -1) {
                    int end = responseBody.indexOf("\"", start + 1);
                    if (end != -1) {
                        return responseBody.substring(start + 1, end);
                    }
                }
            }
            if (responseBody.length() <= 120) {
                return responseBody;
            }
        }
        return fallback != null ? fallback : "Unknown error";
    }

    private EmailResult sendSendGridEmail(String toEmail, String subject, String bodyText) {
        try {
            String url = "https://api.sendgrid.com/v3/mail/send";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(sendgridApiKey.trim());

            String escapedTo = escapeJson(toEmail.trim());
            String escapedFrom = escapeJson(sendgridFromEmail.trim());
            String escapedSubject = escapeJson(subject);
            String escapedBody = escapeJson(bodyText);

            String jsonPayload = String.format("""
            {
              "personalizations": [
                {
                  "to": [
                    { "email": "%s" }
                  ]
                }
              ],
              "from": { "email": "%s" },
              "subject": "%s",
              "content": [
                {
                  "type": "text/plain",
                  "value": "%s"
                }
              ]
            }
            """, escapedTo, escapedFrom, escapedSubject, escapedBody);

            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                // SendGrid v3 mail/send returns HTTP 202 Accepted when queued for delivery
                return new EmailResult(true, "Email accepted by SendGrid (HTTP " + response.getStatusCode().value() + " - Queued for delivery)");
            } else {
                return new EmailResult(false, "SendGrid rejected request with HTTP " + response.getStatusCode().value());
            }
        } catch (HttpStatusCodeException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("SendGrid HTTP error {} sending to {}: {}", e.getStatusCode(), toEmail, responseBody);
            String errorMsg = extractSendGridErrorMessage(responseBody, e.getMessage());
            return new EmailResult(false, "SendGrid error (HTTP " + e.getStatusCode().value() + "): " + errorMsg);
        } catch (Exception e) {
            log.error("Failed to send SendGrid email to {}: {}", toEmail, e.getMessage());
            return new EmailResult(false, "Failed to send email: " + e.getMessage());
        }
    }

    private String extractSendGridErrorMessage(String responseBody, String fallback) {
        if (responseBody != null && !responseBody.isBlank()) {
            // SendGrid error format: {"errors":[{"message":"...","field":"...","help":"..."}]}
            int msgIndex = responseBody.indexOf("\"message\":");
            if (msgIndex != -1) {
                int start = responseBody.indexOf("\"", msgIndex + 10);
                if (start != -1) {
                    int end = responseBody.indexOf("\"", start + 1);
                    if (end != -1) {
                        return responseBody.substring(start + 1, end);
                    }
                }
            }
            if (responseBody.length() <= 150) {
                return responseBody;
            }
        }
        return fallback != null ? fallback : "Unknown SendGrid error";
    }

    private String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    // Setters for testing and configuration
    public void setTwilioAccountSid(String sid) { this.twilioAccountSid = sid; }
    public void setTwilioAuthToken(String token) { this.twilioAuthToken = token; }
    public void setTwilioPhoneNumber(String phone) { this.twilioPhoneNumber = phone; }
    public void setSendgridApiKey(String key) { this.sendgridApiKey = key; }
    public void setSendgridFromEmail(String email) { this.sendgridFromEmail = email; }

    // Helper for testing deduplication
    public void clearNotifiedAlertHistory() {
        this.notifiedAlertIds.clear();
    }

    private record SmsResult(boolean success, String message) {}
    private record EmailResult(boolean success, String message) {}
}
