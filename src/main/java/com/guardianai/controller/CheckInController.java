package com.guardianai.controller;

import com.guardianai.dto.CheckInRequest;
import com.guardianai.dto.CheckInResponse;
import com.guardianai.security.UserPrincipal;
import com.guardianai.service.CheckInService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/check-ins")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @PostMapping
    public ResponseEntity<CheckInResponse> createCheckIn(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CheckInRequest request
    ) {
        CheckInResponse response = checkInService.createCheckIn(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CheckInResponse>> getCheckIns(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<CheckInResponse> response = checkInService.getCheckIns(userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<CheckInResponse> completeCheckIn(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id
    ) {
        CheckInResponse response = checkInService.completeCheckIn(userPrincipal.getId(), id);
        return ResponseEntity.ok(response);
    }
}
