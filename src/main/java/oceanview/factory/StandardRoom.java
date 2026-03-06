package oceanview.factory;

import oceanview.model.RoomStatus;

public class StandardRoom extends AbstractRoom {

    @Override
    protected void configureRoom() {
        room.setPricePerNight(3500);
        room.setFloor(1);
        room.setStatus(RoomStatus.AVAILABLE);
        room.setDescription("Suite Room");
    }
}