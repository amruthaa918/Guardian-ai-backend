package com.guardianai.dto;

import java.time.OffsetDateTime;

public class LocationResponse {

    private Long id;
    private Double latitude;
    private Double longitude;
    private OffsetDateTime createdAt;

    public LocationResponse() {
    }

    public LocationResponse(Long id, Double latitude, Double longitude, OffsetDateTime createdAt) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
