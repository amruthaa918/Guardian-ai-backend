package com.guardianai.dto;

import java.time.OffsetDateTime;

public class CheckInResponse {

    private Long id;
    private OffsetDateTime checkInTime;
    private String status;
    private String message;
    private OffsetDateTime createdAt;

    public CheckInResponse() {
    }

    public CheckInResponse(Long id, OffsetDateTime checkInTime, String status, String message, OffsetDateTime createdAt) {
        this.id = id;
        this.checkInTime = checkInTime;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OffsetDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(OffsetDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
