package oceanview.FilterPattern;

import oceanview.model.Reservation;
import oceanview.model.RoomType;
import java.util.List;
import java.util.stream.Collectors;

public class CriteriaByRoomType implements Criteria {

    private final RoomType roomType;

    public CriteriaByRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    @Override
    public List<Reservation> meetCriteria(List<Reservation> reservations) {
        return reservations.stream()
                .filter(r -> r.getRoomType() == roomType)
                .collect(Collectors.toList());
    }
}