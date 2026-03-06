package oceanview.FilterPattern;
import oceanview.model.Reservation;
import oceanview.model.ReservationStatus;
import java.util.List;
import java.util.stream.Collectors;

public class CriteriaByStatus implements Criteria {

    private final ReservationStatus status;

    public CriteriaByStatus(ReservationStatus status) {
        this.status = status;
    }

    @Override
    public List<Reservation> meetCriteria(List<Reservation> reservations) {
        return reservations.stream()
                .filter(r -> r.getStatus() == status)
                .collect(Collectors.toList());
    }
}