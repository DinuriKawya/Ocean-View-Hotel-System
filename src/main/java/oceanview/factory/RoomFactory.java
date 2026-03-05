package oceanview.factory;


import oceanview.model.Room;
import oceanview.model.RoomStatus;
import oceanview.model.RoomType;

public class RoomFactory {

    // Factory Method: returns concrete Room subclass
    public static Room createRoom(RoomType type) {
        return switch (type) {
            case STANDARD   -> new StandardRoom();
            case DELUXE     -> new DeluxeRoom();
            case SUITE      -> new SuiteRoom();
            case FAMILY     -> new FamilyRoom();
            case PENTHOUSE  -> new PenthouseRoom();
        };
    }

    // Overloaded method: create room with full details (optional, e.g., from form)
    public static Room createRoom(int roomNumber, RoomType type, double price, int floor,
                                  RoomStatus status, String description) {

        Room room = createRoom(type);  
        room.setRoomNumber(roomNumber);
        room.setPricePerNight(price);   
        room.setFloor(floor);        
        room.setStatus(status);
        room.setDescription(description);
        return room;
    }
}