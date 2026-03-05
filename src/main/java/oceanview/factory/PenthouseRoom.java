package oceanview.factory;

import oceanview.model.RoomStatus;
import oceanview.model.RoomType;

public class PenthouseRoom extends AbstractRoom {

    @Override
    protected void configureRoom() {
        setRoomType(RoomType.PENTHOUSE);
        setPricePerNight(20000);
        setFloor(6);
        setStatus(RoomStatus.AVAILABLE);
    }
}