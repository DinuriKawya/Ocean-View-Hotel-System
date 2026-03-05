package oceanview.strategy;

import oceanview.dao.PaymentDAO;
import oceanview.model.Payment;

public class BankTransferStrategy implements PaymentStrategy {

	private final PaymentDAO paymentDAO = new PaymentDAO();

    @Override
    public void pay(Payment payment) throws Exception {
        paymentDAO.insert(payment);
    }
}
