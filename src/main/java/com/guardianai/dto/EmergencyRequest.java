package com.guardianai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EmergencyRequest {

    @NotBlank(message = "Alert type is required")
    @Size(max = 50, message = "Alert type must not exceed 50 characters")
    private String alertType;

    private Double latitude;

    private Double longitude;

    public EmergencyRequest() {
    }

    public EmergencyRequest(String alertType, Double latitude, Double longitude) {
        this.alertType = alertType;
        this.latitude = latitude;
        this.longitude = longitude;
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
}
