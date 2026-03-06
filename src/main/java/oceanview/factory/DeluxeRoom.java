package oceanview.factory;

import oceanview.model.RoomStatus;

public class DeluxeRoom extends AbstractRoom {

    @Override
    protected void configureRoom() {
        room.setPricePerNight(5500);
        room.setFloor(2);
        room.setStatus(RoomStatus.AVAILABLE);
        room.setDescription("Deluxe Room");
    }
}