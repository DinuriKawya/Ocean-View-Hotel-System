package oceanview.FilterPattern;
import oceanview.model.Reservation;
import java.util.List;

public interface Criteria {
    List<Reservation> meetCriteria(List<Reservation> reservations);
}