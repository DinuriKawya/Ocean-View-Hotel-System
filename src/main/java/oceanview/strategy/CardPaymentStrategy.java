package oceanview.strategy;

import oceanview.dao.PaymentDAO;
import oceanview.model.Payment;

public class CardPaymentStrategy implements PaymentStrategy {

	  private final PaymentDAO paymentDAO = new PaymentDAO();

	    @Override
	    public void pay(Payment payment) throws Exception {
	        paymentDAO.insert(payment);
	    }
}