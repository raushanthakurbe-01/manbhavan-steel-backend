package com.manbhavansteel.manbhavan_steel_backend.controller;

import com.manbhavansteel.manbhavan_steel_backend.repository.ColorRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://127.0.0.1:5173"
})
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductColorController {

    private final ColorRepository colorRepository;

    public AdminProductColorController(ColorRepository colorRepository) {
        this.colorRepository = colorRepository;
    }

    @PutMapping("/{productId}/colors")
    @Transactional
    public ResponseEntity<?> updateProductColors(
            @PathVariable Long productId,
            @RequestBody ColorRequest request
    ) {

        colorRepository.deleteAllProductColors(productId);

        if (request.colorIds() != null) {
            for (Long colorId : request.colorIds()) {

                if (colorId != null) {
                    colorRepository.addProductColor(
                            productId,
                            colorId
                    );
                }
            }
        }

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Product colors updated successfully"
                )
        );
    }

    public record ColorRequest(
            List<Long> colorIds
    ) {
    }
}