package oceanview.strategy;

import oceanview.dao.PaymentDAO;
import oceanview.model.Payment;

public class CashPaymentStrategy implements PaymentStrategy {

    private final PaymentDAO paymentDAO = new PaymentDAO();

    public void pay(Payment payment) throws Exception {
        paymentDAO.insert(payment);
    }
}