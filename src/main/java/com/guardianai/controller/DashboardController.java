package com.guardianai.controller;

import com.guardianai.dto.DashboardResponse;
import com.guardianai.security.UserPrincipal;
import com.guardianai.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        DashboardResponse response = dashboardService.getDashboardSummary(userPrincipal.getId());
        return ResponseEntity.ok(response);
    }
}
