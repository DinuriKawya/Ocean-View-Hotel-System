package oceanview.strategy;

import oceanview.model.Payment;

public class PaymentContext {

    private final PaymentStrategy strategy;

    public PaymentContext(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public void execute(Payment payment) throws Exception {
        strategy.pay(payment); 
    }
}

