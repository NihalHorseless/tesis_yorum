package org.example.tesis_yorum.entity;

public enum FacilityType {
    HOTEL("Hotel"),
    RESTAURANT("Restaurant"),
    CAFE("Cafe"),
    SHOPPING_MALL("Shopping Mall"),
    HOSPITAL("Hospital"),
    UNIVERSITY("University"),
    PARK("Park"),
    MUSEUM("Museum"),
    CINEMA("Cinema"),
    SUPERMARKET("Supermarket"),
    OTHER("Other");

    private final String displayName;

    FacilityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
