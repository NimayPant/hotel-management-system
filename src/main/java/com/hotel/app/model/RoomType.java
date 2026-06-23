package com.hotel.app.model;

// Enumeration
public enum RoomType {
    SINGLE("Single Room"),
    DOUBLE("Double Room"),
    SUITE("Luxury Suite");

    private String displayName;

    RoomType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
