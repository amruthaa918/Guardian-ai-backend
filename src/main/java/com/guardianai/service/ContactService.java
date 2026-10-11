package com.guardianai.service;

import com.guardianai.dto.ContactRequest;
import com.guardianai.dto.ContactResponse;
import com.guardianai.entity.TrustedContact;
import com.guardianai.entity.User;
import com.guardianai.exception.ResourceNotFoundException;
import com.guardianai.repository.TrustedContactRepository;
import com.guardianai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactService {

    private final TrustedContactRepository contactRepository;
    private final UserRepository userRepository;

    public ContactService(TrustedContactRepository contactRepository, UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> getContacts(Long userId) {
        return contactRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContactResponse createContact(Long userId, ContactRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        TrustedContact contact = new TrustedContact();
        contact.setUser(user);
        contact.setName(request.getName().trim());
        contact.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        contact.setEmail(request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null);
        contact.setRelationship(request.getRelationship() != null ? request.getRelationship().trim() : null);

        TrustedContact saved = contactRepository.save(contact);
        return mapToResponse(saved);
    }

    @Transactional
    public ContactResponse updateContact(Long userId, Long contactId, ContactRequest request) {
        TrustedContact contact = contactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + contactId));

        contact.setName(request.getName().trim());
        contact.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        contact.setEmail(request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null);
        contact.setRelationship(request.getRelationship() != null ? request.getRelationship().trim() : null);

        TrustedContact updated = contactRepository.save(contact);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteContact(Long userId, Long contactId) {
        TrustedContact contact = contactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + contactId));

        contactRepository.delete(contact);
    }

    private ContactResponse mapToResponse(TrustedContact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getName(),
                contact.getPhone(),
                contact.getEmail(),
                contact.getRelationship(),
                contact.getCreatedAt()
        );
    }
}
