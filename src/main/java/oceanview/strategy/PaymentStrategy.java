package oceanview.strategy;

import oceanview.model.Payment;

public interface PaymentStrategy {
    void pay(Payment payment) throws Exception;
}