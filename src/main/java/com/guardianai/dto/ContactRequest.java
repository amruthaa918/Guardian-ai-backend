package com.guardianai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ContactRequest {

    @NotBlank(message = "Contact name is required")
    @Size(max = 100, message = "Contact name must not exceed 100 characters")
    private String name;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone must be a valid E.164 number")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Email(message = "Please provide a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @Size(max = 50, message = "Relationship must not exceed 50 characters")
    private String relationship;

    public ContactRequest() {
    }

    public ContactRequest(String name, String phone, String email, String relationship) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.relationship = relationship;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
}
