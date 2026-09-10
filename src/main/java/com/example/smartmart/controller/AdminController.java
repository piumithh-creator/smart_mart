package com.example.smartmart.controller;

import com.example.smartmart.constant.CommonResponse;
import com.example.smartmart.dto.response.DashboardResponse;
import com.example.smartmart.dto.response.InventoryResponse;
import com.example.smartmart.service.AdminService;
import com.example.smartmart.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin", description = "Admin dashboard and statistics (ADMIN)")
public class AdminController {

    private final AdminService adminService;
    private final InventoryService inventoryService;

    public AdminController(AdminService adminService, InventoryService inventoryService) {
        this.adminService = adminService;
        this.inventoryService = inventoryService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard statistics")
    public ResponseEntity<CommonResponse> getDashboard() {
        DashboardResponse response = adminService.getDashboard();
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), response, "Dashboard data retrieved"));
    }

    @GetMapping("/inventory-summary")
    @Operation(summary = "Get low-stock inventory summary")
    public ResponseEntity<CommonResponse> getInventorySummary() {
        List<InventoryResponse> response = inventoryService.findLowStockItems();
        return ResponseEntity.ok(new CommonResponse(HttpStatus.OK.value(), response, "Inventory summary retrieved"));
    }
}
