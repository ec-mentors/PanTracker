package io.everyonecodes.project_module.controllers;

import io.everyonecodes.project_module.dtos.requests.UsageLogRequest;
import io.everyonecodes.project_module.dtos.responses.UsageLogResponse;
import io.everyonecodes.project_module.services.UsageLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UsageLogController {
    private final UsageLogService usageLogService;

    @PostMapping("/products/{productId}/logs")
    public ResponseEntity<UsageLogResponse> logUsage (@PathVariable Long productId, @Valid @RequestBody UsageLogRequest request) {
        UsageLogResponse response = usageLogService.logUsage(productId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/products/{productId}/logs")
    public ResponseEntity<List<UsageLogResponse>> getProductUsageHistory (@PathVariable Long productId) {
        List<UsageLogResponse> responses = usageLogService.getProductUsageHistory(productId);

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/logs/{logId}")
    public ResponseEntity<Void> deleteUsageLog (@PathVariable Long logId) {
        usageLogService.deleteUsageLog(logId);

        return ResponseEntity.noContent().build();
    }
}
