package org.insiders.backend.controller;

import org.insiders.backend.dto.image.FilterDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.insiders.backend.logger.AsyncLogManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/filters")
public class ImageFilterController {
    private final AsyncLogManager logger = AsyncLogManager.getInstance();
    private static final List<FilterDto> filters = List.of(
            new FilterDto(1, "none", "No Filter"),
            new FilterDto(2, "BlurFilter", "Blur"),
            new FilterDto(3, "BrightnessFilter", "Brightness"),
            new FilterDto(4, "GrayscaleFilter", "Grayscale"),
            new FilterDto(5, "InvertFilter", "Invert"),
            new FilterDto(6, "NoiseFilter", "Noise"),
            new FilterDto(7, "SepiaFilter", "Sepia"),
            new FilterDto(8, "SharpenFilter", "Sharpen"),
            new FilterDto(9, "TintFilter", "Blue Tint")
    );

    private static final Map<Integer, String> idToName = new HashMap<>();
    static {
        for (FilterDto filter : filters) {
            idToName.put(filter.getId(), filter.getName());
        }
    }

    public static String getNameById(Integer id) {
        return idToName.get(id);
    }

    public static int getIdByName(String name) {
        int found = 0;
        for (FilterDto filter : filters) {
            if (filter.getName().equals(name)) {
                found = filter.getId();
                break;
            }
        }
        return found;
    }


    public ImageFilterController() {
        logger.log("INFO", "- GET /filters");
    }

    // GET /api/filters  -> return supported filter names
    @GetMapping()
    public ResponseEntity<ResponseApi<List<FilterDto>>> getAvailableFilters() {
        try {
            logger.log("INFO", "Successfully retrieved " + filters.size() + " available filters");
            return ResponseEntity.ok(new ResponseApi<>(true, filters));
        } catch (Exception e) {
            logger.log("ERROR", "Failed to retrieve available filters: " + e.getMessage());
            throw e;
        }
    }
}
