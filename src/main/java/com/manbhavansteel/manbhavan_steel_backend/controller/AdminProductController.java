package com.manbhavansteel.manbhavan_steel_backend.controller;

import com.manbhavansteel.manbhavan_steel_backend.entity.Product;
import com.manbhavansteel.manbhavan_steel_backend.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://127.0.0.1:5173"
})
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ProductRepository productRepository;

    public AdminProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestBody ProductRequest request
    ) {

        Product product = new Product();

        product.setCategoryId(request.categoryId());
        product.setName(request.name());
        product.setModelCode(request.modelCode());
        product.setDescription(request.description());
        product.setHeight(request.height());
        product.setWidth(request.width());
        product.setDepth(request.depth());
        product.setRetailPrice(request.retailPrice());
        product.setDealerPrice(request.dealerPrice());
        product.setImageUrl(request.imageUrl());
        product.setStockQuantity(
                request.stockQuantity() == null
                        ? 0
                        : request.stockQuantity()
        );
        product.setActive(
                request.active() == null
                        ? true
                        : request.active()
        );

        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        return ResponseEntity.ok(
                productRepository.save(product)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest request
    ) {

        return productRepository.findById(id)
                .map(product -> {

                    product.setCategoryId(request.categoryId());
                    product.setName(request.name());
                    product.setModelCode(request.modelCode());
                    product.setDescription(request.description());
                    product.setHeight(request.height());
                    product.setWidth(request.width());
                    product.setDepth(request.depth());
                    product.setRetailPrice(request.retailPrice());
                    product.setDealerPrice(request.dealerPrice());
                    product.setImageUrl(request.imageUrl());
                    product.setStockQuantity(
                            request.stockQuantity() == null
                                    ? 0
                                    : request.stockQuantity()
                    );
                    product.setActive(
                            request.active() == null
                                    ? true
                                    : request.active()
                    );
                    product.setUpdatedAt(LocalDateTime.now());

                    return ResponseEntity.ok(
                            productRepository.save(product)
                    );
                })
                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<?> updateActiveStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request
    ) {

        return productRepository.findById(id)
                .map(product -> {

                    Boolean active = request.get("active");

                    if (active == null) {
                        return ResponseEntity.badRequest()
                                .body(
                                        Map.of(
                                                "message",
                                                "Active value is required"
                                        )
                                );
                    }

                    product.setActive(active);
                    product.setUpdatedAt(LocalDateTime.now());

                    return ResponseEntity.ok(
                            productRepository.save(product)
                    );
                })
                .orElse(
                        ResponseEntity
                                .notFound()
                                .build()
                );
    }

    public record ProductRequest(
            Long categoryId,
            String name,
            String modelCode,
            String description,
            BigDecimal height,
            BigDecimal width,
            BigDecimal depth,
            BigDecimal retailPrice,
            BigDecimal dealerPrice,
            String imageUrl,
            Integer stockQuantity,
            Boolean active
    ) {
    }
}