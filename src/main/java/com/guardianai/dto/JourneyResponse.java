package com.guardianai.dto;

import java.time.OffsetDateTime;

public class JourneyResponse {

    private Long id;
    private String startLocation;
    private String destination;
    private OffsetDateTime expectedArrival;
    private String status;
    private OffsetDateTime createdAt;

    public JourneyResponse() {
    }

    public JourneyResponse(Long id, String startLocation, String destination, OffsetDateTime expectedArrival, String status, OffsetDateTime createdAt) {
        this.id = id;
        this.startLocation = startLocation;
        this.destination = destination;
        this.expectedArrival = expectedArrival;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
