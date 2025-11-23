package com.example.orderpay.store.controller;

import com.example.orderpay.auth.LoginUser;
import com.example.orderpay.member.User;
import com.example.orderpay.store.dto.StoreDashboardResponse;
import com.example.orderpay.store.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // 예: /api/v1/stores/10/dashboard?range=5m
    @GetMapping("/{id}/dashboard")
    public StoreDashboardResponse getDashboard(@LoginUser User loginUser, @PathVariable("id") Long storeId,
                                               @RequestParam(value = "range", required = false) String range) {
        return dashboardService.getDashboard(loginUser,storeId, range);
    }
}
