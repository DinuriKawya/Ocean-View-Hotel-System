package oceanview.factory;

import oceanview.model.RoomStatus;

public class SuiteRoom extends AbstractRoom {

    @Override
    protected void configureRoom() {
        room.setPricePerNight(9000);
        room.setFloor(3);
        room.setStatus(RoomStatus.AVAILABLE);
        room.setDescription("Suite Room");
    }
}

