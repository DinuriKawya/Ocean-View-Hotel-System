package oceanview.FilterPattern;

import oceanview.model.Reservation;
import java.util.List;

public class AndCriteria implements Criteria {

    private final Criteria criteriaA;
    private final Criteria criteriaB;

    public AndCriteria(Criteria criteriaA, Criteria criteriaB) {
        this.criteriaA = criteriaA;
        this.criteriaB = criteriaB;
    }

    @Override
    public List<Reservation> meetCriteria(List<Reservation> reservations) {
        List<Reservation> afterA = criteriaA.meetCriteria(reservations);
        return criteriaB.meetCriteria(afterA);
    }
}