package com.stocksense.inventory.controller;

import com.stocksense.inventory.dto.StockAdjustmentRequest;
import com.stocksense.inventory.dto.StockMovementResponse;
import com.stocksense.inventory.entity.MovementType;
import com.stocksense.inventory.service.StockMovementService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    public StockMovementController(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    @GetMapping("/movements")
    public ResponseEntity<Page<StockMovementResponse>> listMovements(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) MovementType movementType,
            @PageableDefault(size = 20, sort = "movementDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<StockMovementResponse> result;
        if (productId != null) {
            result = stockMovementService.findByProductId(productId, pageable);
        } else if (movementType != null) {
            result = stockMovementService.findByMovementType(movementType, pageable);
        } else {
            result = stockMovementService.findAll(pageable);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/movements/{id}")
    public ResponseEntity<StockMovementResponse> getMovementById(@PathVariable Long id) {
        return ResponseEntity.ok(stockMovementService.getById(id));
    }

    @PostMapping("/adjust")
    public ResponseEntity<StockMovementResponse> adjustStock(@Valid @RequestBody StockAdjustmentRequest request) {
        StockMovementResponse response = stockMovementService.recordAdjustment(request);
        return ResponseEntity.ok(response);
    }
}
