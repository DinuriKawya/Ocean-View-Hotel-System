package oceanview.factory;

import oceanview.model.Room;
import oceanview.model.RoomStatus;
import oceanview.model.RoomType;

public class DeluxeRoom extends Room {
    @Override
    public RoomType getRoomType() { return RoomType.DELUXE; }

    public DeluxeRoom() {
        setPricePerNight(5500);
        setFloor(2);
        setStatus(RoomStatus.AVAILABLE);
    }
}