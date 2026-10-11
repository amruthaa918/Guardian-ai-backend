package com.guardianai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactDeliveryStatus {

    private Long contactId;
    private String contactName;
    private String phone;
    private String email;
    private String smsStatus;     // "SENT", "FAILED", "NOT_CONFIGURED", "NO_PHONE"
    private String smsDetails;    // Detailed delivery explanation or error
    private String emailStatus;   // "SENT", "FAILED", "NOT_CONFIGURED", "NO_EMAIL"
    private String emailDetails;

    public ContactDeliveryStatus() {
    }

    public ContactDeliveryStatus(Long contactId, String contactName, String phone, String email,
                                 String smsStatus, String smsDetails,
                                 String emailStatus, String emailDetails) {
        this.contactId = contactId;
        this.contactName = contactName;
        this.phone = phone;
        this.email = email;
        this.smsStatus = smsStatus;
        this.smsDetails = smsDetails;
        this.emailStatus = emailStatus;
        this.emailDetails = emailDetails;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSmsStatus() {
        return smsStatus;
    }

    public void setSmsStatus(String smsStatus) {
        this.smsStatus = smsStatus;
    }

    public String getSmsDetails() {
        return smsDetails;
    }

    public void setSmsDetails(String smsDetails) {
        this.smsDetails = smsDetails;
    }

    public String getEmailStatus() {
        return emailStatus;
    }

    public void setEmailStatus(String emailStatus) {
        this.emailStatus = emailStatus;
    }

    public String getEmailDetails() {
        return emailDetails;
    }

    public void setEmailDetails(String emailDetails) {
        this.emailDetails = emailDetails;
    }
}
