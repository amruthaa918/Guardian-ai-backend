package com.guardianai.service;

import com.guardianai.dto.ContactRequest;
import com.guardianai.dto.ContactResponse;
import com.guardianai.entity.TrustedContact;
import com.guardianai.entity.User;
import com.guardianai.exception.ResourceNotFoundException;
import com.guardianai.repository.TrustedContactRepository;
import com.guardianai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private TrustedContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContactService contactService;

    private User sampleUser;
    private TrustedContact sampleContact;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Amrutha", "amrutha@example.com", "pass", "9876543210", OffsetDateTime.now());
        sampleContact = new TrustedContact(10L, sampleUser, "Father", "9988776655", "father@example.com", "Father", OffsetDateTime.now());
    }

    @Test
    @DisplayName("Should successfully create a trusted contact for the user")
    void testCreateContact() {
        ContactRequest request = new ContactRequest("Father", "9988776655", "father@example.com", "Father");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(contactRepository.save(any(TrustedContact.class))).thenReturn(sampleContact);

        ContactResponse response = contactService.createContact(1L, request);

        assertNotNull(response);
        assertEquals("Father", response.getName());
        assertEquals("9988776655", response.getPhone());
    }

    @Test
    @DisplayName("Should retrieve contacts for specific user only")
    void testGetContacts() {
        when(contactRepository.findByUserId(1L)).thenReturn(List.of(sampleContact));

        List<ContactResponse> list = contactService.getContacts(1L);

        assertEquals(1, list.size());
        assertEquals("Father", list.get(0).getName());
    }

    @Test
    @DisplayName("Should throw exception when contact not found for user")
    void testContactNotFound() {
        when(contactRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                contactService.updateContact(1L, 999L, new ContactRequest("Mom", "123", "m@ex.com", "Mother"))
        );
    }
}
