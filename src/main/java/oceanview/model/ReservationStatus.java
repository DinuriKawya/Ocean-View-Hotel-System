package oceanview.model;

public enum ReservationStatus {

    PENDING    ("Pending",     "Awaiting confirmation"),
    CONFIRMED  ("Confirmed",   "Reservation is confirmed"),
    CHECKED_IN ("Checked In",  "Guest has checked in"),
    CHECKED_OUT("Checked Out", "Guest has checked out"),
    CANCELLED  ("Cancelled",   "Reservation was cancelled"),
    NO_SHOW    ("No Show",     "Guest did not arrive");

    private final String displayName;
    private final String description;

    ReservationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    @Override
    public String toString() { return displayName; }
}
