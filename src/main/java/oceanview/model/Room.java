package oceanview.model;

/**
 * Represents a physical hotel room.
 * Three-tier role: MODEL (entity layer).
 */
public class Room {

    private int        roomId;
    private int        roomNumber;
    private RoomType   roomType;
    private double     pricePerNight;
    private RoomStatus status;
    private int        floor;
    private String     description;

    public Room() {}

    public Room(int roomId, int roomNumber, RoomType roomType,
                double pricePerNight, RoomStatus status, int floor, String description) {
        this.roomId        = roomId;
        this.roomNumber    = roomNumber;
        this.roomType      = roomType;
        this.pricePerNight = pricePerNight;
        this.status        = status;
        this.floor         = floor;
        this.description   = description;
    }

    // Getters
    public int        getRoomId()        { return roomId; }
    public int        getRoomNumber()    { return roomNumber; }
    public RoomType   getRoomType()      { return roomType; }
    public double     getPricePerNight() { return pricePerNight; }
    public RoomStatus getStatus()        { return status; }
    public int        getFloor()         { return floor; }
    public String     getDescription()   { return description; }

    // Setters
    public void setRoomId(int roomId)               { this.roomId        = roomId; }
    public void setRoomNumber(int roomNumber)        { this.roomNumber    = roomNumber; }
    public void setRoomType(RoomType roomType)       { this.roomType      = roomType; }
    public void setPricePerNight(double price)       { this.pricePerNight = price; }
    public void setStatus(RoomStatus status)         { this.status        = status; }
    public void setFloor(int floor)                  { this.floor         = floor; }
    public void setDescription(String description)   { this.description   = description; }

    public boolean isAvailable() { return RoomStatus.AVAILABLE == this.status; }

    /** Label shown in dropdowns: "101 — Standard Room | Floor 1 | LKR 3,000/night" */
    public String getDropdownLabel() {
        return String.format("Room %d — %s | Floor %d | %s %,.2f/night",
                roomNumber,
                roomType != null ? roomType.getDisplayName() : "—",
                floor,
                AppSettings.getCurrency(),
                pricePerNight);
    }

    @Override
    public String toString() {
        return "Room{#" + roomNumber + ", " + roomType + ", " + AppSettings.getCurrency() + " " +
               String.format("%.2f", pricePerNight) + "/night, " + status + "}";
    }
}
