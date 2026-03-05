package oceanview.model;

import oceanview.factory.DeluxeRoom;
import oceanview.factory.FamilyRoom;
import oceanview.factory.PenthouseRoom;
import oceanview.factory.StandardRoom;
import oceanview.factory.SuiteRoom;


public class RoomFactory {

    // Factory method
    public static Room createRoom(RoomType type) {
        return switch (type) {
            case STANDARD  -> new StandardRoom();
            case DELUXE    -> new DeluxeRoom();
            case SUITE     -> new SuiteRoom();      
            case FAMILY    -> new FamilyRoom();      
            case PENTHOUSE -> new PenthouseRoom();    
        };
    }

    public static Room createRoom(int roomNumber, RoomType type, double price, int floor, RoomStatus status, String description) {
        Room room = createRoom(type);
        room.setRoomNumber(roomNumber);
        room.setPricePerNight(price);
        room.setFloor(floor);
        room.setStatus(status);
        room.setDescription(description);
        return room;
    }
}