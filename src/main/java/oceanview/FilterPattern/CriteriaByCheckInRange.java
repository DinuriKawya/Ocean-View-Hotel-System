package oceanview.FilterPattern;

import oceanview.model.Reservation;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class CriteriaByCheckInRange implements Criteria {

    private final LocalDate from;
    private final LocalDate to;

    public CriteriaByCheckInRange(LocalDate from, LocalDate to) {
        this.from = from;
        this.to   = to;
    }

    @Override
    public List<Reservation> meetCriteria(List<Reservation> reservations) {
        return reservations.stream()
                .filter(r -> r.getCheckInDate() != null
                          && !r.getCheckInDate().isBefore(from)
                          && !r.getCheckInDate().isAfter(to))
                .collect(Collectors.toList());
    }
}