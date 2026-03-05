package oceanview.factory;

import oceanview.model.Room;
import oceanview.model.RoomStatus;
import oceanview.model.RoomType;

public class PenthouseRoom extends Room {
    @Override
    public RoomType getRoomType() { return RoomType.PENTHOUSE; }

    public PenthouseRoom() {
        setPricePerNight(20000);
        setFloor(6);
        setStatus(RoomStatus.AVAILABLE);
    }
}