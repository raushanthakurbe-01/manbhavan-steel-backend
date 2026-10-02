package com.manbhavansteel.manbhavan_steel_backend.controller;

import com.manbhavansteel.manbhavan_steel_backend.entity.Category;
import com.manbhavansteel.manbhavan_steel_backend.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://127.0.0.1:5173"
})
@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;

    public AdminCategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(
            @RequestBody CategoryRequest request
    ) {

        Category category = new Category();

        category.setName(request.name());
        category.setDescription(request.description());
        category.setCreatedAt(LocalDateTime.now());

        return ResponseEntity.ok(
                categoryRepository.save(category)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequest request
    ) {

        return categoryRepository.findById(id)
                .map(category -> {

                    category.setName(request.name());
                    category.setDescription(request.description());

                    return ResponseEntity.ok(
                            categoryRepository.save(category)
                    );
                })
                .orElse(
                        ResponseEntity.notFound().build()
                );
    }

    public record CategoryRequest(
            String name,
            String description
    ) {
    }
}