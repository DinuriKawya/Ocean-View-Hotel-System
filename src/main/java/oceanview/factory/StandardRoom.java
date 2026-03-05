package oceanview.factory;

import oceanview.model.Room;
import oceanview.model.RoomStatus;
import oceanview.model.RoomType;

public class StandardRoom extends Room {
    @Override
    public RoomType getRoomType() { return RoomType.STANDARD; }

    public StandardRoom() {
        setPricePerNight(3000);
        setFloor(1);
        setStatus(RoomStatus.AVAILABLE);
    }
}