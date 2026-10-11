package com.guardianai.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "journeys")
public class Journey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_location", length = 255)
    private String startLocation;

    @Column(length = 255)
    private String destination;

    @Column(name = "expected_arrival", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime expectedArrival;

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE";

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    public Journey() {
    }

    public Journey(Long id, User user, String startLocation, String destination, OffsetDateTime expectedArrival, String status, OffsetDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.startLocation = startLocation;
        this.destination = destination;
        this.expectedArrival = expectedArrival;
        this.status = status;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = "ACTIVE";
        }
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
