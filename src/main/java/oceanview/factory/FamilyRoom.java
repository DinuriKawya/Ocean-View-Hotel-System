package oceanview.factory;

import oceanview.model.RoomStatus;
import oceanview.model.RoomType;

public class FamilyRoom extends AbstractRoom {

    @Override
    protected void configureRoom() {
        setRoomType(RoomType.FAMILY);
        setPricePerNight(7000);
        setFloor(2);
        setStatus(RoomStatus.AVAILABLE);
    }
}