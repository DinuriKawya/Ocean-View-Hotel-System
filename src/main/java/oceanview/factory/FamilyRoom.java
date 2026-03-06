package oceanview.factory;

import oceanview.model.RoomStatus;

public class FamilyRoom extends AbstractRoom {

    @Override
    protected void configureRoom() {
        room.setPricePerNight(7000);
        room.setFloor(2);
        room.setStatus(RoomStatus.AVAILABLE);
        room.setDescription("Family Room");
    }
}