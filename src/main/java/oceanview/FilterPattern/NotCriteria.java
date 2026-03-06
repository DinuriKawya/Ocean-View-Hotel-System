package oceanview.FilterPattern;

import oceanview.model.Reservation;
import java.util.List;
import java.util.stream.Collectors;

public class NotCriteria implements Criteria {

    private final Criteria criteria;

    public NotCriteria(Criteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public List<Reservation> meetCriteria(List<Reservation> reservations) {
        List<Reservation> matched = criteria.meetCriteria(reservations);
        return reservations.stream()
                .filter(r -> matched.stream()
                        .noneMatch(m -> m.getReservationId() == r.getReservationId()))
                .collect(Collectors.toList());
    }
}