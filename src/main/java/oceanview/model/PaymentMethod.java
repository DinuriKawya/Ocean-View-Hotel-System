package oceanview.model;

public enum PaymentMethod {

    CASH     ("Cash",                   "success"),
    CARD     ("Credit / Debit Card",    "primary"),
    TRANSFER ("Bank Transfer",          "info");

    private final String displayName;
    private final String badgeColor;

    PaymentMethod(String displayName, String badgeColor) {
        this.displayName = displayName;
        this.badgeColor  = badgeColor;
    }

    public String getDisplayName() { return displayName; }
    public String getBadgeColor()  { return badgeColor; }

    @Override
    public String toString() { return displayName; }
}
