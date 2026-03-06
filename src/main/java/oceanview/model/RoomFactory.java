package oceanview.model;


public class RoomFactory {

  
    public static Room createRoom(RoomType type) {
        Room room = new Room();
        room.setRoomType(type);
        room.setStatus(RoomStatus.AVAILABLE);  

       
        switch (type) {
            case STANDARD   -> { room.setPricePerNight(3000); room.setFloor(1); }
            case DELUXE     -> { room.setPricePerNight(5500); room.setFloor(2); }
            case SUITE      -> { room.setPricePerNight(9000); room.setFloor(3); }
            case FAMILY     -> { room.setPricePerNight(7000); room.setFloor(2); }
            case PENTHOUSE  -> { room.setPricePerNight(20000); room.setFloor(6); }
            default         -> throw new IllegalArgumentException("Unknown room type: " + type);
        }
        return room;
    }

 
    public static Room createRoom(int roomNumber, RoomType type, double price, int floor, RoomStatus status, String description) {
        Room room = new Room();
        room.setRoomNumber(roomNumber);
        room.setRoomType(type);
        room.setPricePerNight(price);
        room.setFloor(floor);
        room.setStatus(status);
        room.setDescription(description);
        return room;
    }
}