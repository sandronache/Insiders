package org.insiders.backend.controller;

import org.insiders.backend.dto.image.FilterDto;
import org.insiders.backend.entity.Post;
import org.insiders.backend.service.PostManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/filters")
public class ImageFilterController {

    // GET /api/filters  -> return supported filter names
    @GetMapping()
    public ResponseEntity<ResponseApi<List<FilterDto>>> getAvailableFilters() {
        List<FilterDto> filters = List.of(
                new FilterDto(0, "none", "No Filter"),
                new FilterDto(1, "blur", "Blur"),
                new FilterDto(2, "grayscale", "Grayscale"),
                new FilterDto(3, "invert", "Invert"),
                new FilterDto(4, "sepia", "Sepia"),
                new FilterDto(5, "sharpen", "Sharpen"),
                new FilterDto(6, "brightness", "Brightness"),
                new FilterDto(7, "noise", "Noise"),
                new FilterDto(8, "tint", "BlueTint")

        );

        return ResponseEntity.ok(new ResponseApi<>(true, filters));
    }

}
