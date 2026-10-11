package com.guardianai.controller;

import com.guardianai.dto.EmergencyRequest;
import com.guardianai.dto.EmergencyResponse;
import com.guardianai.security.UserPrincipal;
import com.guardianai.service.EmergencyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emergency")
public class EmergencyController {

    private final EmergencyService emergencyService;

    public EmergencyController(EmergencyService emergencyService) {
        this.emergencyService = emergencyService;
    }

    @PostMapping("/sos")
    public ResponseEntity<EmergencyResponse> triggerSos(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody EmergencyRequest request
    ) {
        EmergencyResponse response = emergencyService.triggerSos(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<EmergencyResponse>> getEmergencyHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<EmergencyResponse> history = emergencyService.getEmergencyHistory(userPrincipal.getId());
        return ResponseEntity.ok(history);
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<EmergencyResponse> resolveAlert(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        EmergencyResponse response = emergencyService.resolveAlert(userPrincipal.getId(), id);
        return ResponseEntity.ok(response);
    }
}
