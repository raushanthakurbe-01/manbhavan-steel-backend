package com.manbhavansteel.manbhavan_steel_backend.controller;

import com.manbhavansteel.manbhavan_steel_backend.entity.Color;
import com.manbhavansteel.manbhavan_steel_backend.repository.ColorRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/colors")
public class ColorController {

    private final ColorRepository colorRepository;

    public ColorController(ColorRepository colorRepository) {
        this.colorRepository = colorRepository;
    }

    @GetMapping
    public List<Color> getAllColors() {
        return colorRepository.findAll();
    }

    @GetMapping("/product/{productId}")
    public List<Color> getColorsByProductId(@PathVariable Long productId) {
        return colorRepository.findColorsByProductId(productId);
    }
}