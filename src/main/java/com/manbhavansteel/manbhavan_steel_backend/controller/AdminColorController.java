package com.manbhavansteel.manbhavan_steel_backend.controller;

import com.manbhavansteel.manbhavan_steel_backend.entity.Color;
import com.manbhavansteel.manbhavan_steel_backend.repository.ColorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://127.0.0.1:5173"
})
@RestController
@RequestMapping("/api/admin/colors")
public class AdminColorController {

    private final ColorRepository colorRepository;

    public AdminColorController(ColorRepository colorRepository) {
        this.colorRepository = colorRepository;
    }

    @GetMapping
    public List<Color> getAllColors() {
        return colorRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Color> createColor(
            @RequestBody ColorRequest request
    ) {

        Color color = new Color();

        color.setName(request.name());
        color.setHexCode(request.hexCode());
        color.setCreatedAt(LocalDateTime.now());

        return ResponseEntity.ok(
                colorRepository.save(color)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateColor(
            @PathVariable Long id,
            @RequestBody ColorRequest request
    ) {

        return colorRepository.findById(id)
                .map(color -> {

                    color.setName(request.name());
                    color.setHexCode(request.hexCode());

                    return ResponseEntity.ok(
                            colorRepository.save(color)
                    );
                })
                .orElse(
                        ResponseEntity.notFound().build()
                );
    }

    public record ColorRequest(
            String name,
            String hexCode
    ) {
    }
}