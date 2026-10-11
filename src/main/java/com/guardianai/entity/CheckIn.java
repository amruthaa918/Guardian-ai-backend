package com.guardianai.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "check_ins")
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "check_in_time", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime checkInTime;

    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    @Column(length = 500)
    private String message;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    public CheckIn() {
    }

    public CheckIn(Long id, User user, OffsetDateTime checkInTime, String status, String message, OffsetDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.checkInTime = checkInTime;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = "PENDING";
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
