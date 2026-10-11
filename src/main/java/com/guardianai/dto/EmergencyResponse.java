package com.guardianai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmergencyResponse {

    private String message;
    private Long alertId;
    private String status;
    private String alertType;
    private Double latitude;
    private Double longitude;
    private OffsetDateTime createdAt;
    private List<ContactDeliveryStatus> contactDeliveryStatuses;

    public EmergencyResponse() {
    }

    public EmergencyResponse(String message, Long alertId, String status) {
        this.message = message;
        this.alertId = alertId;
        this.status = status;
    }

    public EmergencyResponse(String message, Long alertId, String status, List<ContactDeliveryStatus> contactDeliveryStatuses) {
        this.message = message;
        this.alertId = alertId;
        this.status = status;
        this.contactDeliveryStatuses = contactDeliveryStatuses;
    }

    public EmergencyResponse(Long alertId, String alertType, Double latitude, Double longitude, String status, OffsetDateTime createdAt) {
        this.alertId = alertId;
        this.alertType = alertType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.createdAt = createdAt;
    }

    public EmergencyResponse(String message, Long alertId, String alertType, Double latitude, Double longitude, String status, OffsetDateTime createdAt) {
        this.message = message;
        this.alertId = alertId;
        this.alertType = alertType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ContactDeliveryStatus> getContactDeliveryStatuses() {
        return contactDeliveryStatuses;
    }

    public void setContactDeliveryStatuses(List<ContactDeliveryStatus> contactDeliveryStatuses) {
        this.contactDeliveryStatuses = contactDeliveryStatuses;
    }
}
