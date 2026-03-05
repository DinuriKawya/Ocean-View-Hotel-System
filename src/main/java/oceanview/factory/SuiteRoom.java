package oceanview.factory;

import oceanview.model.Room;
import oceanview.model.RoomStatus;
import oceanview.model.RoomType;

public class SuiteRoom extends Room {
    @Override
    public RoomType getRoomType() { return RoomType.SUITE; }

    public SuiteRoom() {
        setPricePerNight(9000);
        setFloor(3);
        setStatus(RoomStatus.AVAILABLE);
    }
}