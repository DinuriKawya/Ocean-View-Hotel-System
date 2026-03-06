package oceanview.FilterPattern;

import oceanview.model.Reservation;
import java.util.List;

public class OrCriteria implements Criteria {

    private final Criteria criteriaA;
    private final Criteria criteriaB;

    public OrCriteria(Criteria criteriaA, Criteria criteriaB) {
        this.criteriaA = criteriaA;
        this.criteriaB = criteriaB;
    }

    @Override
    public List<Reservation> meetCriteria(List<Reservation> reservations) {
        List<Reservation> resultA = criteriaA.meetCriteria(reservations);
        List<Reservation> resultB = criteriaB.meetCriteria(reservations);

        for (Reservation r : resultB) {
            boolean alreadyIn = resultA.stream()
                    .anyMatch(a -> a.getReservationId() == r.getReservationId());
            if (!alreadyIn) resultA.add(r);
        }
        return resultA;
    }
}