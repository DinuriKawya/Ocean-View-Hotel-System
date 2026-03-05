package oceanview.factory;

import oceanview.model.RoomStatus;
import oceanview.model.RoomType;

public class StandardRoom extends AbstractRoom {

    @Override
    protected void configureRoom() {
        setRoomType(RoomType.STANDARD);
        setPricePerNight(3000);
        setFloor(1);
        setStatus(RoomStatus.AVAILABLE);
    }
}