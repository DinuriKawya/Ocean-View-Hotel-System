package oceanview.service;

import oceanview.dao.BankDAO;
import oceanview.model.Bank;

import java.sql.SQLException;
import java.util.List;

public class BankService {

    private final BankDAO dao = new BankDAO();

    public List<Bank> getAllBanks() throws BankException {
        try { return dao.findAll(); }
        catch (SQLException e) { throw new BankException("DB error: " + e.getMessage()); }
    }

    public List<Bank> getActiveBanks() throws BankException {
        try { return dao.findActive(); }
        catch (SQLException e) { throw new BankException("DB error: " + e.getMessage()); }
    }

    public Bank getById(int id) throws BankException {
        try {
            Bank b = dao.findById(id);
            if (b == null) throw new BankException("Bank #" + id + " not found.");
            return b;
        } catch (SQLException e) { throw new BankException("DB error: " + e.getMessage()); }
    }

    public Bank createBank(Bank b) throws BankException {
        validate(b);
        try {
            int id = dao.insert(b);
            b.setBankId(id);
            return b;
        } catch (SQLException e) { throw new BankException("DB error: " + e.getMessage()); }
    }

    public Bank updateBank(Bank b) throws BankException {
        validate(b);
        try {
            dao.update(b);
            return b;
        } catch (SQLException e) { throw new BankException("DB error: " + e.getMessage()); }
    }

    public void deleteBank(int id) throws BankException {
        try {
            if (!dao.delete(id)) throw new BankException("Bank #" + id + " not found.");
        } catch (SQLException e) { throw new BankException("DB error: " + e.getMessage()); }
    }

    private void validate(Bank b) throws BankException {
        if (b.getName() == null || b.getName().isBlank())
            throw new BankException("Bank name is required.");
    }

    public static class BankException extends Exception {
        public BankException(String message) { super(message); }
    }
}
