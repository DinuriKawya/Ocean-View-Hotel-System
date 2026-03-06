package oceanview.FilterPattern;
import oceanview.model.Reservation;
import java.util.List;
import java.util.stream.Collectors;

public class CriteriaByGuestName implements Criteria {

    private final String keyword;

    public CriteriaByGuestName(String keyword) {
        this.keyword = keyword.toLowerCase();
    }

    @Override
    public List<Reservation> meetCriteria(List<Reservation> reservations) {
        return reservations.stream()
                .filter(r -> r.getGuestName() != null
                          && r.getGuestName().toLowerCase().contains(keyword))
                .collect(Collectors.toList());
    }
}