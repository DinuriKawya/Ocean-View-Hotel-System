package oceanview.factory;

import oceanview.model.Room;
import oceanview.model.RoomStatus;
import oceanview.model.RoomType;

public abstract class AbstractRoom {

    protected Room room;  

    public AbstractRoom() {
        this.room = new Room();
        configureRoom();
    }

    protected abstract void configureRoom();

    public Room getRoom() { return room; }

    public int getRoomNumber() { return room.getRoomNumber(); }
    public void setRoomNumber(int number) { room.setRoomNumber(number); }

    public double getPricePerNight() { return room.getPricePerNight(); }
    public void setPricePerNight(double price) { room.setPricePerNight(price); }

    public int getFloor() { return room.getFloor(); }
    public void setFloor(int floor) { room.setFloor(floor); }

    public RoomStatus getStatus() { return room.getStatus(); }
    public void setStatus(RoomStatus status) { room.setStatus(status); }

    public RoomType getRoomType() { return room.getRoomType(); }
}