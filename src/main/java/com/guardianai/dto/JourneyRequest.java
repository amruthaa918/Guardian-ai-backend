package com.guardianai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public class JourneyRequest {

    @NotBlank(message = "Start location is required")
    @Size(max = 255, message = "Start location must not exceed 255 characters")
    private String startLocation;

    @NotBlank(message = "Destination is required")
    @Size(max = 255, message = "Destination must not exceed 255 characters")
    private String destination;

    private OffsetDateTime expectedArrival;

    public JourneyRequest() {
    }

    public JourneyRequest(String startLocation, String destination, OffsetDateTime expectedArrival) {
        this.startLocation = startLocation;
        this.destination = destination;
        this.expectedArrival = expectedArrival;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public OffsetDateTime getExpectedArrival() {
        return expectedArrival;
    }

    public void setExpectedArrival(OffsetDateTime expectedArrival) {
        this.expectedArrival = expectedArrival;
    }
}
