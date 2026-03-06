package oceanview.service;

import oceanview.dao.AuditLogDAO;
import oceanview.model.AuditLog;

import java.sql.SQLException;
import java.util.List;

public class AuditLogService {

    private final AuditLogDAO auditLogDAO;

    // production constructor
    public AuditLogService() { 
    	this.auditLogDAO = new AuditLogDAO();
    }

    // test constructor 
    public AuditLogService(AuditLogDAO auditLogDAO) { 
    	this.auditLogDAO = auditLogDAO;
    }

    public static final int PAGE_SIZE = 20;

    public List<AuditLog> getLogs(String action, String performedBy, String dateFrom, String dateTo, String search, int page) throws SQLException {
        return auditLogDAO.findFiltered(action, performedBy, dateFrom, dateTo, search, page, PAGE_SIZE);
    }

    public int getTotalPages(String action, String performedBy, String dateFrom, String dateTo, String search) throws SQLException {
        int total = auditLogDAO.countFiltered(action, performedBy, dateFrom, dateTo, search);
        return (int) Math.ceil((double) total / PAGE_SIZE);
    }

    public int getTotalCount(String action, String performedBy, String dateFrom, String dateTo, String search) throws SQLException {
        return auditLogDAO.countFiltered(action, performedBy, dateFrom, dateTo, search);
    }

    public List<String> getDistinctActions() throws SQLException {
        return auditLogDAO.getDistinctActions();
    }

    public List<String> getDistinctPerformedBy() throws SQLException {
        return auditLogDAO.getDistinctPerformedBy();
    }
}
