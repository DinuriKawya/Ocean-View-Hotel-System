package oceanview.model;

public enum RoomType {

    STANDARD   ("Standard Room",    1, 2),
    DELUXE     ("Deluxe Room",      1, 3),
    SUITE      ("Suite",            1, 4),
    FAMILY     ("Family Room",      2, 5),
    PENTHOUSE  ("Penthouse",        1, 6);

    private final String displayName;
    private final int minGuests;
    private final int maxGuests;

    RoomType(String displayName, int minGuests, int maxGuests) {
        this.displayName = displayName;
        this.minGuests   = minGuests;
        this.maxGuests   = maxGuests;
    }

    public String getDisplayName() { return displayName; }
    public int    getMinGuests()   { return minGuests; }
    public int    getMaxGuests()   { return maxGuests; }

    @Override
    public String toString() { return displayName; }
}
