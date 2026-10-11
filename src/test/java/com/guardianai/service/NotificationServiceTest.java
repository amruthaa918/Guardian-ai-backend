package com.guardianai.service;

import com.guardianai.dto.ContactDeliveryStatus;
import com.guardianai.dto.NotificationResult;
import com.guardianai.entity.EmergencyAlert;
import com.guardianai.entity.TrustedContact;
import com.guardianai.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private RestTemplate restTemplate;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        notificationService = new NotificationService(restTemplate);
    }

    @Test
    @DisplayName("Should return empty list and message when no contacts exist")
    void testNotifyTrustedContacts_noContacts() {
        User user = new User();
        EmergencyAlert alert = new EmergencyAlert();
        NotificationResult result = notificationService.notifyTrustedContacts(user, alert, List.of());

        assertEquals("No trusted contacts configured.", result.getSummaryMessage());
        assertTrue(result.getContactStatuses().isEmpty());
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("Should report NOT_CONFIGURED when Twilio credentials are missing")
    void testNotifyTrustedContacts_providersNotConfigured() {
        User user = new User();
        user.setName("Amrutha");
        EmergencyAlert alert = new EmergencyAlert();
        TrustedContact contact = new TrustedContact();
        contact.setId(10L);
        contact.setName("Friend");
        contact.setPhone("+1234567890");
        contact.setEmail("friend@test.com");

        notificationService.setTwilioAccountSid("");
        notificationService.setTwilioAuthToken("");
        notificationService.setTwilioPhoneNumber("");
        notificationService.setSendgridApiKey("");
        notificationService.setSendgridFromEmail("");

        NotificationResult result = notificationService.notifyTrustedContacts(user, alert, List.of(contact));

        assertEquals("SMS: provider not configured. Email: provider not configured.", result.getSummaryMessage());
        assertEquals(1, result.getContactStatuses().size());

        ContactDeliveryStatus status = result.getContactStatuses().get(0);
        assertEquals(10L, status.getContactId());
        assertEquals("Friend", status.getContactName());
        assertEquals("NOT_CONFIGURED", status.getSmsStatus());
        assertTrue(status.getSmsDetails().contains("TWILIO_ACCOUNT_SID"));
        assertEquals("NOT_CONFIGURED", status.getEmailStatus());

        // Must never make external HTTP calls when credentials are missing
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("Should mock Twilio SMS and succeed with actual coordinates in Google Maps link")
    @SuppressWarnings("unchecked")
    void testNotifyTrustedContacts_providersConfiguredAndSuccessful() {
        User user = new User();
        user.setName("Amrutha");
        EmergencyAlert alert = new EmergencyAlert();
        alert.setLatitude(17.3850);
        alert.setLongitude(78.4867);

        TrustedContact contact = new TrustedContact();
        contact.setId(1L);
        contact.setName("Mom");
        contact.setPhone("+919876543210");
        contact.setEmail("mom@test.com");

        notificationService.setTwilioAccountSid("ACmockaccount123");
        notificationService.setTwilioAuthToken("mocktoken456");
        notificationService.setTwilioPhoneNumber("+18005550199");
        notificationService.setSendgridApiKey("SG.mockkey");
        notificationService.setSendgridFromEmail("alerts@guardianai.org");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{\"sid\": \"SMmock123\"}", HttpStatus.CREATED));

        NotificationResult result = notificationService.notifyTrustedContacts(user, alert, List.of(contact));

        assertEquals("SMS: 1 accepted, 0 failed. Email: 1 accepted, 0 failed.", result.getSummaryMessage());
        assertEquals(1, result.getContactStatuses().size());

        ContactDeliveryStatus status = result.getContactStatuses().get(0);
        assertEquals("SENT", status.getSmsStatus());
        assertTrue(status.getSmsDetails().contains("HTTP 201"));
        assertEquals("ACCEPTED", status.getEmailStatus());
        assertTrue(status.getEmailDetails().contains("Queued for delivery"));

        // Verify Twilio request payload includes Google Maps link with coordinates
        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate, atLeastOnce()).postForEntity(contains("api.twilio.com"), captor.capture(), eq(String.class));

        HttpEntity<MultiValueMap<String, String>> twilioEntity = captor.getValue();
        MultiValueMap<String, String> body = twilioEntity.getBody();
        assertNotNull(body);
        assertEquals("+919876543210", body.getFirst("To"));
        assertEquals("+18005550199", body.getFirst("From"));
        String msg = body.getFirst("Body");
        assertNotNull(msg);
        assertTrue(msg.contains("Amrutha needs help"));
        assertTrue(msg.contains("https://maps.google.com/?q=17.385,78.4867"));

        // Verify SendGrid request payload
        ArgumentCaptor<HttpEntity<String>> emailCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate, atLeastOnce()).postForEntity(contains("api.sendgrid.com"), emailCaptor.capture(), eq(String.class));
        HttpEntity<String> emailEntity = emailCaptor.getValue();
        assertNotNull(emailEntity.getBody());
        assertTrue(emailEntity.getBody().contains("mom@test.com"));
        assertTrue(emailEntity.getBody().contains("alerts@guardianai.org"));
        assertTrue(emailEntity.getBody().contains("https://maps.google.com/?q=17.385,78.4867"));
    }

    @Test
    @DisplayName("Should handle Twilio HTTP failure and report FAILED status without throwing")
    void testNotifyTrustedContacts_twilioFailure() {
        User user = new User();
        user.setName("Amrutha");
        EmergencyAlert alert = new EmergencyAlert();

        TrustedContact contact = new TrustedContact();
        contact.setId(2L);
        contact.setName("Dad");
        contact.setPhone("+919876543211");

        notificationService.setTwilioAccountSid("ACmockaccount123");
        notificationService.setTwilioAuthToken("mocktoken456");
        notificationService.setTwilioPhoneNumber("+18005550199");

        String twilioErrorJson = "{\"code\": 21211, \"message\": \"The 'To' number is not a valid phone number.\"}";
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders.EMPTY,
                twilioErrorJson.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(restTemplate.postForEntity(contains("api.twilio.com"), any(HttpEntity.class), eq(String.class)))
                .thenThrow(exception);

        NotificationResult result = notificationService.notifyTrustedContacts(user, alert, List.of(contact));

        assertEquals("SMS: 0 accepted, 1 failed. Email: provider not configured.", result.getSummaryMessage());
        assertEquals(1, result.getContactStatuses().size());

        ContactDeliveryStatus status = result.getContactStatuses().get(0);
        assertEquals("FAILED", status.getSmsStatus());
        assertTrue(status.getSmsDetails().contains("The 'To' number is not a valid phone number"));
    }

    @Test
    @DisplayName("Should handle SendGrid HTTP failure and report accurate FAILED status")
    void testNotifyTrustedContacts_sendGridFailure() {
        User user = new User();
        user.setName("Amrutha");
        EmergencyAlert alert = new EmergencyAlert();

        TrustedContact contact = new TrustedContact();
        contact.setId(3L);
        contact.setName("Sister");
        contact.setEmail("sister@example.com");

        notificationService.setSendgridApiKey("SG.invalidkey");
        notificationService.setSendgridFromEmail("alerts@guardianai.org");

        String sendgridErrorJson = "{\"errors\":[{\"message\":\"The provided authorization grant is invalid, expired, or revoked\",\"field\":null,\"help\":null}]}";
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                HttpHeaders.EMPTY,
                sendgridErrorJson.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(restTemplate.postForEntity(contains("api.sendgrid.com"), any(HttpEntity.class), eq(String.class)))
                .thenThrow(exception);

        NotificationResult result = notificationService.notifyTrustedContacts(user, alert, List.of(contact));

        assertEquals("SMS: provider not configured. Email: 0 accepted, 1 failed.", result.getSummaryMessage());
        assertEquals(1, result.getContactStatuses().size());

        ContactDeliveryStatus status = result.getContactStatuses().get(0);
        assertEquals("FAILED", status.getEmailStatus());
        assertTrue(status.getEmailDetails().contains("The provided authorization grant is invalid"));
    }

    @Test
    @DisplayName("Should report NO_PHONE and NO_EMAIL when contact fields are missing")
    void testNotifyTrustedContacts_missingContactFields() {
        User user = new User();
        EmergencyAlert alert = new EmergencyAlert();
        TrustedContact contact = new TrustedContact();
        contact.setId(4L);
        contact.setName("Blank Contact");
        contact.setPhone("");
        contact.setEmail("");

        notificationService.setTwilioAccountSid("ACmockaccount123");
        notificationService.setTwilioAuthToken("mocktoken456");
        notificationService.setTwilioPhoneNumber("+18005550199");
        notificationService.setSendgridApiKey("SG.mockkey");
        notificationService.setSendgridFromEmail("alerts@guardianai.org");

        NotificationResult result = notificationService.notifyTrustedContacts(user, alert, List.of(contact));

        ContactDeliveryStatus status = result.getContactStatuses().get(0);
        assertEquals("NO_PHONE", status.getSmsStatus());
        assertEquals("NO_EMAIL", status.getEmailStatus());
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("Should prevent duplicate notification dispatches for the same alert ID")
    void testNotifyTrustedContacts_duplicateAlertPrevention() {
        User user = new User();
        EmergencyAlert alert = new EmergencyAlert();
        alert.setId(999L);

        TrustedContact contact = new TrustedContact();
        contact.setId(5L);
        contact.setName("Friend");
        contact.setEmail("friend@example.com");

        notificationService.setSendgridApiKey("SG.mockkey");
        notificationService.setSendgridFromEmail("alerts@guardianai.org");

        when(restTemplate.postForEntity(contains("api.sendgrid.com"), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("", HttpStatus.ACCEPTED));

        // First dispatch
        NotificationResult firstResult = notificationService.notifyTrustedContacts(user, alert, List.of(contact));
        assertEquals(1, firstResult.getContactStatuses().size());

        // Second dispatch for the same alert ID must be prevented
        NotificationResult duplicateResult = notificationService.notifyTrustedContacts(user, alert, List.of(contact));
        assertTrue(duplicateResult.getSummaryMessage().contains("Duplicate dispatch prevented"));
        assertTrue(duplicateResult.getContactStatuses().isEmpty());

        // External API must only be called once, not twice
        verify(restTemplate, times(1)).postForEntity(contains("api.sendgrid.com"), any(HttpEntity.class), eq(String.class));
    }
}
