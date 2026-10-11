package com.guardianai.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public class CheckInRequest {

    @NotNull(message = "Check-in time is required")
    private OffsetDateTime checkInTime;

    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;

    public CheckInRequest() {
    }

    public CheckInRequest(OffsetDateTime checkInTime, String message) {
        this.checkInTime = checkInTime;
        this.message = message;
    }

    public OffsetDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(OffsetDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
