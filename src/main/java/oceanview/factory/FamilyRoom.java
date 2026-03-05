package oceanview.factory;

import oceanview.model.Room;
import oceanview.model.RoomStatus;
import oceanview.model.RoomType;

public class FamilyRoom extends Room {
    @Override
    public RoomType getRoomType() { return RoomType.FAMILY; }

    public FamilyRoom() {
        setPricePerNight(7000);
        setFloor(2);
        setStatus(RoomStatus.AVAILABLE);
    }
}