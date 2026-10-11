package com.guardianai.controller;

import com.guardianai.dto.ContactRequest;
import com.guardianai.dto.ContactResponse;
import com.guardianai.security.UserPrincipal;
import com.guardianai.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ResponseEntity<List<ContactResponse>> getContacts(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<ContactResponse> contacts = contactService.getContacts(userPrincipal.getId());
        return ResponseEntity.ok(contacts);
    }

    @PostMapping
    public ResponseEntity<ContactResponse> createContact(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ContactRequest request) {
        ContactResponse response = contactService.createContact(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactResponse> updateContact(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody ContactRequest request) {
        ContactResponse response = contactService.updateContact(userPrincipal.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        contactService.deleteContact(userPrincipal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
