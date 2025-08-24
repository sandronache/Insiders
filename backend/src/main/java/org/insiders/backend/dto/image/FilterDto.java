package org.insiders.backend.dto.image;

public record FilterDto(int id, String name, String label) {
    public int getId() { return id; }
    public String getName() { return name; }
}

