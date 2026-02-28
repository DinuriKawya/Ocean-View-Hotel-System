package oceanview.model;

public enum RoomStatus {

    AVAILABLE    ("Available",         "success"),
    OCCUPIED     ("Occupied",          "danger"),
    MAINTENANCE  ("Under Maintenance", "warning"),
    OUT_OF_ORDER ("Out of Order",      "secondary");

    private final String displayName;
    private final String badgeColor;   // Bootstrap color name

    RoomStatus(String displayName, String badgeColor) {
        this.displayName = displayName;
        this.badgeColor  = badgeColor;
    }

    public String getDisplayName() { return displayName; }
    public String getBadgeColor()  { return badgeColor; }

    @Override
    public String toString() { return displayName; }
}
