package oceanview.factory;

import oceanview.model.Room;
import oceanview.model.RoomStatus;
import oceanview.model.RoomType;

public class RoomFactory {


    public static Room createRoom(RoomType type) {

        AbstractRoom factory;

        switch (type) {
            case STANDARD:
                factory = new StandardRoom();
                break;

            case DELUXE:
                factory = new DeluxeRoom();
                break;

            case SUITE:
                factory = new SuiteRoom();
                break;

            case FAMILY:
                factory = new FamilyRoom();
                break;

            case PENTHOUSE:
                factory = new PenthouseRoom();
                break;

            default:
                throw new IllegalArgumentException("Invalid Room Type");
        }

        return factory.room;
    }

    // Overloaded method 
    public static Room createRoom(int roomNumber, RoomType type, double price,
                                  int floor, RoomStatus status, String description) {

        Room room = createRoom(type);

        room.setRoomNumber(roomNumber);
        room.setPricePerNight(price);
        room.setFloor(floor);
        room.setStatus(status);
        room.setDescription(description);

        return room;
    }
}