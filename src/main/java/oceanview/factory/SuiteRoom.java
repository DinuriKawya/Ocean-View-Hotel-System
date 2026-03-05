package oceanview.factory;

import oceanview.model.RoomStatus;
import oceanview.model.RoomType;

public class SuiteRoom extends AbstractRoom {

    @Override
    protected void configureRoom() {
        setRoomType(RoomType.SUITE);
        setPricePerNight(9000);
        setFloor(3);
        setStatus(RoomStatus.AVAILABLE);
    }
}