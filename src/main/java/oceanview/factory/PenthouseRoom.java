package oceanview.factory;

import oceanview.model.RoomStatus;

public class PenthouseRoom extends AbstractRoom {

    @Override
    protected void configureRoom() {
        room.setPricePerNight(15000);
        room.setFloor(5);
        room.setStatus(RoomStatus.AVAILABLE);
        room.setDescription("Penthouse Room");
    }
}