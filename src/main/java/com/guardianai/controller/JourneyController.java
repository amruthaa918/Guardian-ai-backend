package com.guardianai.controller;

import com.guardianai.dto.JourneyRequest;
import com.guardianai.dto.JourneyResponse;
import com.guardianai.security.UserPrincipal;
import com.guardianai.service.JourneyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journeys")
public class JourneyController {

    private final JourneyService journeyService;

    public JourneyController(JourneyService journeyService) {
        this.journeyService = journeyService;
    }

    @PostMapping
    public ResponseEntity<JourneyResponse> createJourney(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody JourneyRequest request
    ) {
        JourneyResponse response = journeyService.createJourney(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<JourneyResponse>> getJourneys(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<JourneyResponse> journeys = journeyService.getJourneys(userPrincipal.getId());
        return ResponseEntity.ok(journeys);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JourneyResponse> getJourneyById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        JourneyResponse response = journeyService.getJourneyById(userPrincipal.getId(), id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<JourneyResponse> completeJourney(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        JourneyResponse response = journeyService.completeJourney(userPrincipal.getId(), id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<JourneyResponse> cancelJourney(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        JourneyResponse response = journeyService.cancelJourney(userPrincipal.getId(), id);
        return ResponseEntity.ok(response);
    }
}
