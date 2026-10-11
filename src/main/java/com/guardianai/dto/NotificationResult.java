package com.guardianai.dto;

import java.util.ArrayList;
import java.util.List;

public class NotificationResult {

    private String summaryMessage;
    private List<ContactDeliveryStatus> contactStatuses;

    public NotificationResult() {
        this.contactStatuses = new ArrayList<>();
    }

    public NotificationResult(String summaryMessage, List<ContactDeliveryStatus> contactStatuses) {
        this.summaryMessage = summaryMessage;
        this.contactStatuses = contactStatuses != null ? contactStatuses : new ArrayList<>();
    }

    public String getSummaryMessage() {
        return summaryMessage;
    }

    public void setSummaryMessage(String summaryMessage) {
        this.summaryMessage = summaryMessage;
    }

    public List<ContactDeliveryStatus> getContactStatuses() {
        return contactStatuses;
    }

    public void setContactStatuses(List<ContactDeliveryStatus> contactStatuses) {
        this.contactStatuses = contactStatuses;
    }
}
