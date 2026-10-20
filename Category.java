package dev.vapemace.client.module;

public enum Category {
    COMBAT("Combat"),
    UTILITY("Utility"),
    RENDER("Render & HUD");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
