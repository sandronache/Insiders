package org.insiders.backend.controller;

import org.insiders.backend.dto.image.FilterDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.insiders.backend.logger.AsyncLogManager;

import java.util.List;

@RestController
@RequestMapping("/filters")
public class ImageFilterController {
    private final AsyncLogManager logger = AsyncLogManager.getInstance();

    public ImageFilterController() {
        logger.log("INFO", "- GET /filters");
    }

    // GET /api/filters  -> return supported filter names
    @GetMapping()
    public ResponseEntity<ResponseApi<List<FilterDto>>> getAvailableFilters() {
        logger.log("INFO", "GET request received for available image filters");
        try {
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

            logger.log("INFO", "Successfully retrieved " + filters.size() + " available filters");
            return ResponseEntity.ok(new ResponseApi<>(true, filters));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to retrieve available filters: " + e.getMessage());
            throw e;
        }
    }
}