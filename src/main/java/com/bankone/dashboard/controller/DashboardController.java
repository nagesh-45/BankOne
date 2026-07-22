package com.bankone.dashboard.controller;

import com.bankone.dashboard.dto.DashboardResponse;
import com.bankone.dashboard.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public DashboardResponse getDashboardSummary() {
        return dashboardService.getDashboardSummary();
    }
}