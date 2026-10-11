package com.guardianai.controller;

import com.guardianai.dto.LocationRequest;
import com.guardianai.dto.LocationResponse;
import com.guardianai.security.UserPrincipal;
import com.guardianai.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    public ResponseEntity<LocationResponse> saveLocation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody LocationRequest request
    ) {
        LocationResponse response = locationService.saveLocation(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/latest")
    public ResponseEntity<LocationResponse> getLatestLocation(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        LocationResponse response = locationService.getLatestLocation(userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<LocationResponse>> getLocationHistory(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<LocationResponse> history = locationService.getLocationHistory(userPrincipal.getId());
        return ResponseEntity.ok(history);
    }
}
